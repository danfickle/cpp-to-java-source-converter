package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;

class EnumManager
{
	final TranslationUnitContext ctx;
	
	public EnumManager(TranslationUnitContext con) {
		ctx = con;
	}
	
	void evalDeclEnum(IASTEnumerationSpecifier enumerationSpecifier) throws DOMException
	{
		IASTEnumerator[] enumerators = enumerationSpecifier.getEnumerators();
		
		if (enumerators == null || enumerators.length == 0)
			return;

		CppEnum enumModel = new CppEnum();
		enumModel.simpleName = TypeManager.getSimpleName(enumerationSpecifier.getName());

		if (enumModel.simpleName.equals("MISSING"))
		{
			enumModel.simpleName = "AnonEnum" + ctx.global.anonEnumCount++;
		}
		
		enumModel.qualified = TypeManager.getQualifiedPart(enumerationSpecifier.getName()); 

		String first = enumerators[0].getName().toString();		
		ctx.global.anonEnumMap.put(first, enumModel.simpleName);
		
		int nextValue = 0;
		int sinceLastValue = 1;
		MExpression lastValue = null;

		for (IASTEnumerator e : enumerators)
		{
			CppEnumerator enumerator = new CppEnumerator();
			enumerator.name = TypeManager.getSimpleName(e.getName());

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
		String enumeration = TypeManager.getSimpleType(((IEnumeration) enumerator.getType()).getName());

		if (enumeration.equals("MISSING"))
		{
			String first = ((IEnumeration) enumerator.getOwner()).getEnumerators()[0].getName();
			String enumName = ctx.global.anonEnumMap.get(first);
			
			if (enumName == null)
				MyLogger.exitOnError();
			
			return enumName;
		}
		
		return enumeration;
	}
	
}
