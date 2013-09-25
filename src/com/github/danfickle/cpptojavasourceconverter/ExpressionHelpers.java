package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.*;

import com.github.danfickle.cpptojavasourceconverter.TypeHelpers.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.*;

class ExpressionHelpers
{
	/**
	 * Given a C++ binary operator,
	 * returns whether we need boolean expression with it.
	 */
	static boolean needBooleanExpressions(int operator)
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_logicalAnd:
		case IASTBinaryExpression.op_logicalOr:
			return true;
		}
		return false;
	}

	/**
	 * Converts a C++ postfix operator to a Java one.
	 */
	static String evalUnaryPostfixOperator(int operator) 
	{
		switch(operator)
		{
		case IASTUnaryExpression.op_postFixDecr:
			return "--";
		case IASTUnaryExpression.op_postFixIncr:
			return "++";
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Converts a C++ prefix operator to a Java one.
	 */
	static String evalUnaryPrefixOperator(int operator) 
	{
		switch(operator)
		{
		case IASTUnaryExpression.op_prefixDecr:
			return "--";
		case IASTUnaryExpression.op_prefixIncr:
			return "++";
		case IASTUnaryExpression.op_not:
			return "!";
		case IASTUnaryExpression.op_plus:
			return "+";
		case IASTUnaryExpression.op_minus:
			return "-";
		case IASTUnaryExpression.op_tilde:
			return "~";
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Whether a C++ unary expression operator is prefix or postfix.
	 */
	static boolean isPrefixExpression(int operator)
	{
		switch (operator)
		{
		case IASTUnaryExpression.op_prefixDecr:
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_not:
		case IASTUnaryExpression.op_plus:
		case IASTUnaryExpression.op_minus:
		case IASTUnaryExpression.op_tilde:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Whether a C++ unary expression operator is prefix or postfix.
	 */
	static boolean isPostfixExpression(int operator)
	{
		switch (operator)
		{
		case IASTUnaryExpression.op_postFixDecr:
		case IASTUnaryExpression.op_postFixIncr:
			return true;
		default:
			return false;
		}
	}
	

	
	/**
	 * Get rid of enclosing brackets.
	 */
	static IASTExpression unwrap(IASTExpression expr)
	{
		while (expr instanceof IASTUnaryExpression &&
				((IASTUnaryExpression) expr).getOperator() == IASTUnaryExpression.op_bracketedPrimary)
		{
			expr = ((IASTUnaryExpression) expr).getOperand();
		}
		
		return expr;
	}
	
	/**
	 * Surrounds a Java expression with brackets.
	 */
	static MExpression bracket(MExpression expr)
	{
		MBracketExpression bra = new MBracketExpression();
		bra.operand = expr;
		return bra;
	}
	
	/**
	 * Converts a C++ binary assignment operator to a Java one.
	 */
	static String evaluateBinaryAssignmentOperator(int operator) 
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_assign:
			return "=";
		case IASTBinaryExpression.op_binaryAndAssign:
			return "&=";
		case IASTBinaryExpression.op_binaryOrAssign:
			return "|=";
		case IASTBinaryExpression.op_binaryXorAssign:
			return "^=";
		case IASTBinaryExpression.op_divideAssign:
			return "/=";
		case IASTBinaryExpression.op_plusAssign:
			return "+=";
		case IASTBinaryExpression.op_minusAssign:
			return "-=";
		case IASTBinaryExpression.op_multiplyAssign:
			return "*=";
		case IASTBinaryExpression.op_shiftLeftAssign:
			return "<<=";
		case IASTBinaryExpression.op_shiftRightAssign:
			return ">>="; // VERIFY
		default:
			return null;
		}
	}

	/**
	 * Returns whether a C++ binary expression operator is an assignment operator.
	 */
	static boolean isAssignmentExpression(int operator)
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_assign:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXorAssign:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_plusAssign:
		case IASTBinaryExpression.op_minusAssign:
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_shiftLeftAssign:
		case IASTBinaryExpression.op_shiftRightAssign:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Converts a C++ compound assignment operator to a Java infix.
	 */
	static String compoundAssignmentToInfixOperator(int op)
	{
		switch (op)
		{
		case IASTBinaryExpression.op_binaryAndAssign:
			return "&";
		case IASTBinaryExpression.op_binaryOrAssign:
			return "|";
		case IASTBinaryExpression.op_binaryXorAssign:
			return "^"; 
		case IASTBinaryExpression.op_divideAssign:
			return "/";
		case IASTBinaryExpression.op_plusAssign:
			return "+";
		case IASTBinaryExpression.op_minusAssign:
			return "-";
		case IASTBinaryExpression.op_multiplyAssign:
			return "*";
		case IASTBinaryExpression.op_shiftLeftAssign:
			return "<<";
		case IASTBinaryExpression.op_shiftRightAssign:
			return ">>"; // VERIFY
		default:
			return null;
		}
	}
	
	/**
	 * Converts the CDT binary operator to a Java binary operator.
	 */
	static String evaluateBinaryOperator(int operator)
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_assign:
			return "=";
		case IASTBinaryExpression.op_binaryAnd:
			return "&";
		case IASTBinaryExpression.op_binaryOr:
			return "|";
		case IASTBinaryExpression.op_binaryXor:
			return "^";
		case IASTBinaryExpression.op_divide:
			return "/";
		case IASTBinaryExpression.op_equals:
			return "==";
		case IASTBinaryExpression.op_plus:
			return "+";
		case IASTBinaryExpression.op_minus:
			return "-";
		case IASTBinaryExpression.op_multiply:
			return "*";
		case IASTBinaryExpression.op_notequals:
			return "!=";
		case IASTBinaryExpression.op_greaterEqual:
			return ">=";
		case IASTBinaryExpression.op_greaterThan:
			return ">";
		case IASTBinaryExpression.op_lessEqual:
			return "<=";
		case IASTBinaryExpression.op_lessThan:
			return "<";
		case IASTBinaryExpression.op_logicalAnd:
			return "&&";
		case IASTBinaryExpression.op_logicalOr:
			return "||";
		case IASTBinaryExpression.op_modulo:
			return "%";
		case IASTBinaryExpression.op_shiftLeft:
			return "<<";
		case IASTBinaryExpression.op_shiftRight:
			return ">>";
		default:
			return evaluateBinaryAssignmentOperator(operator);
		}
	}
	
	static TypeEnum expressionGetType(IASTExpression expr) throws DOMException
	{
		if (expr == null)
		{
			MyLogger.logImportant("Null expression");
			MyLogger.exitOnError();
		}
		else if (expr.getExpressionType() == null)
		{
			MyLogger.logImportant("Null expression type: " + expr.getRawSignature());
			MyLogger.exitOnError();
		}

		return TypeHelpers.getTypeEnum(expr.getExpressionType());
	}
	
	static boolean isBooleanExpression(IASTExpression expr) throws DOMException
	{
		TypeEnum te = TypeHelpers.getTypeEnum(expr.getExpressionType());
		
		if (te == TypeEnum.BOOLEAN)
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if the C++ expression is a pointer deref such as (*), (->) or array
	 * access.
	 * @throws DOMException 
	 */
	static boolean isEventualPtrDeref(IASTExpression expr) throws DOMException
	{
		expr = unwrap(expr);

		// Now check if it is the de-reference operator...
		if (expr instanceof IASTUnaryExpression &&
			((IASTUnaryExpression) expr).getOperator() == IASTUnaryExpression.op_star)
			return true;
		
		if (expr instanceof IASTFieldReference &&
			((IASTFieldReference) expr).isPointerDereference())
			return true;
		
		// Finally check for the array access operator on a pointer...
		if (expr instanceof IASTArraySubscriptExpression &&
			TypeHelpers.isEventualPtrBasic(((IASTArraySubscriptExpression) expr).getArrayExpression().getExpressionType()))
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if the C++ expression type is a number or boolean.
	 */
	static boolean isNumberExpression(IASTExpression expr) throws DOMException
	{
		TypeEnum te = TypeHelpers.getTypeEnum(expr.getExpressionType());
		
		if (te == TypeEnum.BOOLEAN ||
			te == TypeEnum.CHAR ||
			te == TypeEnum.NUMBER)
			return true;
		
		return false;
	}

	/**
	 * Attempts to make a Java boolean expression. Eg. Adds != null, != 0, etc.
	 */
	static MExpression makeExpressionBoolean(MExpression exp, IASTExpression expcpp) throws DOMException
	{
		return makeExpressionBoolean(exp, expressionGetType(expcpp));
	}
	
	/**
	 * Attempts to make a Java boolean expression. Eg. Adds != null, != 0, etc.
	 */
	static MExpression makeExpressionBoolean(MExpression exp, TypeEnum expType) throws DOMException
	{
		if (expType != TypeEnum.BOOLEAN)
		{
			MExpression r = null;
			
			if (expType == TypeEnum.OBJECT ||
				expType == TypeEnum.OBJECT_POINTER ||
				expType == TypeEnum.BASIC_POINTER)
			{
				r = ModelCreation.createLiteral("null");
			}
			else if (expType == TypeEnum.NUMBER ||
					 expType == TypeEnum.BASIC_REFERENCE ||
					 expType == TypeEnum.CHAR)
			{
				r = ModelCreation.createLiteral("0");
			}

			return bracket(ModelCreation.createInfixExpr(bracket(exp), r, "!="));
		}
		return bracket(exp);
	}


	
	/**
	 * Given a type, creates a new expression.
	 * eg. 'int' becomes 'new MInteger()'
	 */
	static MExpression makeSimpleCreationExpression(IType tp) throws DOMException
	{
		MClassInstanceCreation create = new MClassInstanceCreation();
		create.name = ModelCreation.createLiteral(TypeHelpers.cppToJavaType(tp));
		return create;
	}
}
