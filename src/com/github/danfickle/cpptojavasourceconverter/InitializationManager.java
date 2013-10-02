package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.TypeHelpers.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.TypeHelpers.TypeType;

class InitializationManager
{
	private final GlobalContext ctx;
	
	InitializationManager(GlobalContext con)
	{
		ctx = con;
	}
	
	MExpression eval1Init(IASTInitializer initializer, IType typeRequired, IASTName name) throws DOMException 
	{
		if (initializer == null)
		{
			if (TypeHelpers.isBasicType(typeRequired) &&
				!ctx.bitfieldMngr.isBitfield(name))
			{
				// MInteger.valueOf(0);
				MValueOfExpressionNumber expr = new MValueOfExpressionNumber();
				
				expr.type = TypeHelpers.cppToJavaType(typeRequired, TypeType.IMPLEMENTATION);

				if (TypeHelpers.getTypeEnum(typeRequired) == TypeEnum.BOOLEAN)
					expr.operand = ModelCreation.createLiteral("false");
				else
					expr.operand = ModelCreation.createLiteral("0");

				return expr;
			}
			else if (TypeHelpers.isOneOf(typeRequired, TypeEnum.BASIC_ARRAY))
			{
				// MIntegerMulti.create(4);
				MValueOfExpressionArray expr = new MValueOfExpressionArray();
				expr.type = TypeHelpers.cppToJavaType(typeRequired, TypeType.IMPLEMENTATION);
				expr.operands = ctx.exprEvaluator.getArraySizeExpressions(typeRequired);
				return expr;
			}
			else if (TypeHelpers.isOneOf(typeRequired, TypeEnum.OBJECT))
			{
				// new FooBar();
				MNewExpressionObject expr = new MNewExpressionObject();
				expr.type = TypeHelpers.cppToJavaType(typeRequired, TypeType.IMPLEMENTATION);
				return expr;
			}
			else if (TypeHelpers.isOneOf(typeRequired, TypeEnum.OBJECT_ARRAY))
			{
				// TODO
				return null;
			}
			else
			{
				return null;
			}
		}
		else if (initializer instanceof IASTEqualsInitializer)
		{
			if (ctx.bitfieldMngr.isBitfield(name))
			{
				return ctx.exprEvaluator.eval1Expr((IASTExpression) ((IASTEqualsInitializer) initializer).getInitializerClause());
			}
			else if (TypeHelpers.isOneOf(typeRequired, TypeEnum.BASIC_POINTER, TypeEnum.OBJECT_POINTER))
			{
				MExpression expr = ctx.exprEvaluator.eval1Expr((IASTExpression) ((IASTEqualsInitializer) initializer).getInitializerClause());
				ctx.exprEvaluator.modifyLiteralToPtr(expr);
				return expr;
			}
			else
			{
				return ctx.exprEvaluator.wrapIfNeeded((IASTExpression) ((IASTEqualsInitializer) initializer).getInitializerClause(), typeRequired);
			}
		}
		else if (initializer instanceof ICPPASTConstructorInitializer)
		{
			ICPPASTConstructorInitializer inti = (ICPPASTConstructorInitializer) initializer;

			MMultiExpression multi = new MMultiExpression();
			
			for (IASTInitializerClause cls : inti.getArguments())
			{
				IASTExpression expr = (IASTExpression) cls;
				MExpression create;

				if (ctx.bitfieldMngr.isBitfield(name))
				{
					create = ctx.exprEvaluator.eval1Expr(expr);
				}
				else if (TypeHelpers.isOneOf(typeRequired, TypeEnum.BASIC_POINTER, TypeEnum.OBJECT_POINTER))
				{
					create = ctx.exprEvaluator.eval1Expr(expr);
					ctx.exprEvaluator.modifyLiteralToPtr(create);
				}
				else
				{
					create = ctx.exprEvaluator.wrapIfNeeded(expr, typeRequired);
				}
				multi.exprs.add(create);
			}

			return multi;
		}
		
		return null;
	}
}
