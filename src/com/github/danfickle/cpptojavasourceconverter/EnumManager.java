package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

import com.github.danfickle.cpptojavasourceconverter.GlobalCtx.ITypeName;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.NameType;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;

class EnumManager
{
	final TranslationUnitContext ctx;
	
	public EnumManager(TranslationUnitContext con) {
		ctx = con;
	}
	
	String findEnumCompleteCppName(IType type)
	{
		// Iterate the types list to find the complete name
		// we have given this enum.
		for (ITypeName ent : ctx.global.types)
		{
			if (ent.tp.isSameType(type))
			{
				return ent.nm;
			}
		}

		// Not found.
		return null;
	}
	
	boolean alreadyExists(IASTName enumeration) throws DOMException
	{
		IType type = ctx.converter.evalBindingReturnType(enumeration.resolveBinding());

		// Try to find this enum in the types list.
		String find = findEnumCompleteCppName(type);
			
		// We found it, so return.
		return (find != null);
	}
	
	void evalDeclEnum(IASTEnumerationSpecifier enumerationSpecifier) throws DOMException
	{
		IASTEnumerator[] enumerators = enumerationSpecifier.getEnumerators();
		
		if (enumerators == null || enumerators.length == 0)
			return;

		if (alreadyExists(enumerationSpecifier.getName()))
			return;
		
		// Not found, so create a name, if needed, and register it.
		
		String complete = TypeManager.getCompleteName(enumerationSpecifier.getName());
		String simple = TypeManager.getSimpleType(complete);

		IType type = ctx.converter.evalBindingReturnType(enumerationSpecifier.getName().resolveBinding());
		
		if (simple == null || simple.isEmpty())
		{
			simple = "AnonEnum" + ctx.global.anonEnumCount++;
			complete = complete + simple;
		}

		CppEnum enumModel = new CppEnum();

		enumModel.simpleName = TypeManager.cppNameToJavaName(simple, NameType.CAPITALIZED);
		enumModel.completeCppName = complete;
		
		ctx.global.declarations.put(complete, enumModel);
		ctx.global.types.add(new ITypeName(type, complete));
		
		int nextValue = 0;
		int sinceLastValue = 1;
		MExpression lastValue = null;

		for (IASTEnumerator e : enumerators)
		{
			CppEnumerator enumerator = new CppEnumerator();
			enumerator.name = TypeManager.cppNameToJavaName(e.getName().toString(), NameType.ALL_CAPS);

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
	}
	
	CppEnum getEnumerationDeclModel(IEnumerator enumerator) throws DOMException
	{
		IType parentType = (enumerator.getType());
		// Lookup the name in the type to cpp name table.
		String nm = findEnumCompleteCppName(parentType);
		// Lookup the declaration model in the name to model table.
		return (CppEnum) ctx.global.declarations.get(nm);
	}
}
