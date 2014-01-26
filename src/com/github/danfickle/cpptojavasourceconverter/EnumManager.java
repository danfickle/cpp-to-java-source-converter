package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

import com.github.danfickle.cpptojavasourceconverter.TypeManager.NameType;
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

		IType type = ctx.converter.evalBindingReturnType(enumerationSpecifier.getName().resolveBinding());

		CppEnum enumModel = (CppEnum) ctx.typeMngr.getDeclFromTypeName(type, enumerationSpecifier.getName());
		
		if (enumModel != null)
			return;
		
		enumModel = ctx.declModels.new CppEnum();

		ctx.typeMngr.registerDecl(enumModel, type, enumerationSpecifier.getName(),
				NameType.CAPITALIZED, enumerationSpecifier.getContainingFilename(),
				enumerationSpecifier.getFileLocation().getStartingLineNumber());
	
		int nextValue = 0;
		int sinceLastValue = 1;
		MExpression lastValue = null;

		for (IASTEnumerator e : enumerators)
		{
			CppEnumerator enumerator = ctx.declModels.new CppEnumerator();
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
		return (CppEnum) ctx.typeMngr.getDeclFromType(parentType);
	}
}
