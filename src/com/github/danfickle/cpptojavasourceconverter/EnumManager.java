package com.github.danfickle.cpptojavasourceconverter;

import java.util.HashMap;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;

class EnumManager
{
	final GlobalContext ctx;
	
	public EnumManager(GlobalContext con) {
		ctx = con;
	}
	
	private HashMap<String, String> anonEnumMap = new HashMap<String, String>();

	// TODO: Persist over multiple translation units.
	private int anonEnumCount = 0;
	
	void evalDeclEnum(IASTEnumerationSpecifier enumerationSpecifier) throws DOMException
	{
		IASTEnumerator[] enumerators = enumerationSpecifier.getEnumerators();
		
		if (enumerators == null || enumerators.length == 0)
			return;

		CppEnum enumModel = new CppEnum();
		enumModel.simpleName = TypeHelpers.getSimpleName(enumerationSpecifier.getName());

		if (enumModel.simpleName.equals("MISSING"))
		{
			enumModel.simpleName = "AnonEnum" + anonEnumCount++;
		}
		
		enumModel.qualified = TypeHelpers.getQualifiedPart(enumerationSpecifier.getName()); 

		String first = enumerators[0].getName().toString();		
		anonEnumMap.put(first, enumModel.simpleName);
		
		int nextValue = 0;
		int sinceLastValue = 1;
		MExpression lastValue = null;

		for (IASTEnumerator e : enumerators)
		{
			CppEnumerator enumerator = new CppEnumerator();
			enumerator.name = TypeHelpers.getSimpleName(e.getName());

			if (e.getValue() != null)
			{
				enumerator.value = ctx.exprEvaluator.eval1Expr(e.getValue());
				lastValue = enumerator.value;
				sinceLastValue = 1;
			}
			else if (lastValue != null)
			{
				enumerator.value = ModelCreation.createInfixExpr(
						lastValue,
						ModelCreation.createLiteral(String.valueOf(sinceLastValue++)),
						"+");
			}
			else
			{
				enumerator.value = ModelCreation.createLiteral(String.valueOf(nextValue++));
			}
			
			enumModel.enumerators.add(enumerator);
		}
		
		ctx.converter.addDeclaration(enumModel);
		ctx.converter.popDeclaration();
	}
	
	String getEnumerationName(IEnumerator enumerator) throws DOMException
	{
		String enumeration = TypeHelpers.getSimpleType(((IEnumeration) enumerator.getType()).getName());

		if (enumeration.equals("MISSING"))
		{
			String first = ((IEnumeration) enumerator.getOwner()).getEnumerators()[0].getName();
			String enumName = anonEnumMap.get(first);
			
			if (enumName == null)
				MyLogger.exitOnError();
			
			return enumName;
		}
		
		return enumeration;
	}
	
}
