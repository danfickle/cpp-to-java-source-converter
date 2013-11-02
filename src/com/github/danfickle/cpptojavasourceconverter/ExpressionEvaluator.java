package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.core.dom.ast.gnu.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppFunction;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.InitializationManager.InitType;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.TypeType;

class ExpressionEvaluator
{
	private final TranslationUnitContext ctx;
	
	ExpressionEvaluator(TranslationUnitContext con)
	{
		ctx = con;
	}
	
	MExpression eval1Expr(IASTExpression expr) throws DOMException
	{
		List<MExpression> exprs = evalExpr(expr);
		assert(exprs.size() == 1);
		return exprs.get(0);
	}
	
	/**
	 * Given a C++ expression, attempts to convert it into one or more Java expressions.
	 */
	List<MExpression> evalExpr(IASTExpression expression) throws DOMException
	{
		List<MExpression> ret = new ArrayList<MExpression>();
		
		if (expression instanceof IASTLiteralExpression)
		{
			evalExprLiteral((IASTLiteralExpression) expression, ret);
		}
		else if (expression instanceof IASTIdExpression)
		{
			evalExprId((IASTIdExpression) expression, ret);
		}
		else if (expression instanceof IASTFieldReference)
		{
			evalExprFieldReference((IASTFieldReference) expression, ret);
		}
		else if (expression instanceof IASTUnaryExpression)
		{
			evalExprUnary((IASTUnaryExpression) expression, ret);
		}
		else if (expression instanceof IASTConditionalExpression)
		{
			evalExprConditional((IASTConditionalExpression) expression, ret);
		}
		else if (expression instanceof IASTArraySubscriptExpression)
		{
			evalExprArraySubscript((IASTArraySubscriptExpression) expression, ret);
		}
		else if (expression instanceof IASTBinaryExpression)
		{
			evalExprBinary((IASTBinaryExpression) expression, ret);
		}
		else if (expression instanceof ICPPASTDeleteExpression)
		{
			evalExprDelete((ICPPASTDeleteExpression) expression, ret);
		}
		else if (expression instanceof ICPPASTNewExpression)
		{
			evalExprNew((ICPPASTNewExpression) expression, ret);
		}
		else if (expression instanceof IASTFunctionCallExpression)
		{
			evalExprFuncCall((IASTFunctionCallExpression) expression, ret);
		}
		else if (expression instanceof IASTCastExpression)
		{
			evalCastExpression((IASTCastExpression) expression, ret);
		}
		else if (expression instanceof IASTTypeIdExpression)
		{
			//evalTypeIdExpression((IASTTypeIdExpression) expression, ret, flags);
		}
		else if (expression instanceof ICASTTypeIdInitializerExpression)
		{
			ICASTTypeIdInitializerExpression typeIdInitializerExpression = (ICASTTypeIdInitializerExpression)expression;

			ctx.converter.evalTypeId(typeIdInitializerExpression.getTypeId());
			//evaluate(typeIdInitializerExpression.getInitializer());
		}
		else if (expression instanceof ICPPASTSimpleTypeConstructorExpression)
		{
			ICPPASTSimpleTypeConstructorExpression simpleTypeConstructorExpression = (ICPPASTSimpleTypeConstructorExpression)expression;
			evalExpr(simpleTypeConstructorExpression.getInitialValue());
		}
		else if (expression instanceof IGNUASTCompoundStatementExpression)
		{
			IGNUASTCompoundStatementExpression compoundStatementExpression = (IGNUASTCompoundStatementExpression)expression;
			ctx.stmtEvaluator.evalStmt(compoundStatementExpression.getCompoundStatement());
		}
		else if (expression instanceof IASTExpressionList)
		{
			IASTExpressionList list = (IASTExpressionList) expression;

			for (IASTExpression childExpression : list.getExpressions())
				ret.addAll(evalExpr(childExpression));
		}
		else if (expression == null)
		{
			ret.add(new MEmptyExpression());
		}

		if (ret.isEmpty())
			MyLogger.logImportant(expression.getClass().getCanonicalName());

		if (expression != null)
			MyLogger.log(expression.getClass().getCanonicalName());

		return ret;
	}
	
	private void evalExprNew(ICPPASTNewExpression expr, List<MExpression> ret) throws DOMException
	{
		if (expr.isArrayAllocation() && !TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			MNewArrayExpression ptr = new MNewArrayExpression();
			
			for (IASTExpression arraySize : expr.getNewTypeIdArrayExpressions())
				ptr.sizes.add(eval1Expr(arraySize));
			
			ptr.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.IMPLEMENTATION);
			ret.add(ptr);
		}
		else if (expr.isArrayAllocation() && TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			MNewArrayExpressionObject ptr = new MNewArrayExpressionObject();
			
			for (IASTExpression arraySize : expr.getNewTypeIdArrayExpressions())
				ptr.sizes.add(eval1Expr(arraySize));
			
			ptr.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.IMPLEMENTATION);
			ret.add(ptr);
		}
		else if (TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			// PtrObject.valueOf(new object())
			MNewExpressionObject ptr = new MNewExpressionObject();
			ptr.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.RAW);
			// TODO: expr.getExpressionType is not good enough here.
			// ptr.arguments.add(ctx.initMngr.eval1Init(expr.getInitializer(), expr.getExpressionType(), null));

			MValueOfExpressionPtr ptrExpr = new MValueOfExpressionPtr();
			ptrExpr.type = "PtrObject";
			ptrExpr.operand = ptr;
			ret.add(ptrExpr);
		}
		else if (TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.BASIC_POINTER) &&
				 TypeManager.getPointerIndirectionCount(expr.getExpressionType()) == 1)
		{
			// MInteger.valueOf(101)
			ret.add(ctx.initMngr.eval1Init(expr.getInitializer(), TypeManager.getPointerBaseType(expr.getExpressionType()), null, InitType.WRAPPED));
		}
		else if (TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.BASIC_POINTER))
		{
			// TODO: Multiple indirection to basic type.
		}
	}

	private void evalExprDelete(ICPPASTDeleteExpression expr, List<MExpression> ret) throws DOMException
	{
		if (TypeManager.isOneOf(expr.getOperand().getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			if (expr.isVectored())
			{
				MDeleteObjectMultiple del = new MDeleteObjectMultiple();
				del.operand = eval1Expr(expr.getOperand());
				ret.add(del);
			}
			else
			{
				MDeleteObjectSingle del = new MDeleteObjectSingle();
				del.operand = eval1Expr(expr.getOperand());
				ret.add(del);
			}
		}
		else
		{
			MEmptyExpression emp = new MEmptyExpression();
			ret.add(emp);
		}
	}
	
	private void evalExprId(IASTIdExpression expr, List<MExpression> ret) throws DOMException
	{
		if (ctx.bitfieldMngr.isBitfield(expr.getName()))
		{
			MIdentityExpressionBitfield ident = new MIdentityExpressionBitfield();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			ret.add(ident);
		}
		else if (expr.getName().resolveBinding() instanceof IEnumerator)
		{
			MIdentityExpressionEnumerator ident = new MIdentityExpressionEnumerator();
			ident.enumName = ctx.enumMngr.getEnumerationDeclModel((IEnumerator) expr.getName().resolveBinding()).name;
			ident.ident = TypeManager.getSimpleName(expr.getName());
			ret.add(ident);
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getExpressionType()))
		{
			MIdentityExpressionPtr ident = new MIdentityExpressionPtr();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			ret.add(ident);
		}
		else if (ExpressionHelpers.isBasicExpression(expr))
		{
			MIdentityExpressionNumber ident = new MIdentityExpressionNumber();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			ret.add(ident);
		}
		else
		{
			MIdentityExpressionPlain ident = new MIdentityExpressionPlain();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			ret.add(ident);
		}
	}

	private void evalExprFieldReference(IASTFieldReference expr, List<MExpression> ret) throws DOMException
	{
		if (ctx.bitfieldMngr.isBitfield(expr.getFieldName()))
		{
			MFieldReferenceExpressionBitfield field = new MFieldReferenceExpressionBitfield();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			ret.add(field);
		}
		else if (expr.getFieldName().resolveBinding() instanceof IEnumerator)
		{
			MFieldReferenceExpressionEnumerator field = new MFieldReferenceExpressionEnumerator();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			ret.add(field);
		}
		else if (ExpressionHelpers.isBasicExpression(expr))
		{
			MFieldReferenceExpressionNumber field = new MFieldReferenceExpressionNumber();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			ret.add(field);
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getExpressionType()) && expr.isPointerDereference())
		{
			MFieldReferenceExpressionPtr field = new MFieldReferenceExpressionPtr();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			ret.add(field);
		}
		else
		{
			MFieldReferenceExpressionPlain field = new MFieldReferenceExpressionPlain();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			ret.add(field);
		}
	}


	private void evalExprConditional(IASTConditionalExpression expr, List<MExpression> ret) throws DOMException 
	{
		MTernaryExpression ternary = new MTernaryExpression();
		
		ternary.condition = eval1Expr(expr.getLogicalConditionExpression());
		ternary.condition = ExpressionHelpers.makeExpressionBoolean(ternary.condition, expr.getLogicalConditionExpression());
		ternary.positive = eval1Expr(expr.getPositiveResultExpression());
		ternary.negative = eval1Expr(expr.getNegativeResultExpression());
		
		ret.add(ternary);
	}

	private void evalCastExpression(IASTCastExpression expr, List<MExpression> ret) throws DOMException
	{
		MCastExpression cast = new MCastExpression();
		cast.operand = eval1Expr(expr.getOperand());
		// TODO cast.setType(evalTypeId(castExpression.getTypeId()));
	}

	private void evalExprArraySubscript(IASTArraySubscriptExpression expr, List<MExpression> ret) throws DOMException
	{
		//if (isEventualPtr(expr.getArrayExpression().getExpressionType()))
		{
			MArrayExpressionPtr ptr = new MArrayExpressionPtr();
			ptr.operand = eval1Expr(expr.getArrayExpression());
			ptr.subscript.addAll(evalExpr(expr.getSubscriptExpression()));
			ret.add(ptr);
		}
	}

	private void evalExprLiteral(IASTLiteralExpression lit, List<MExpression> ret) throws DOMException 
	{
		MLiteralExpression out = new MLiteralExpression();
		
		switch (lit.getKind())
		{
		case IASTLiteralExpression.lk_false:
			out.literal = "false";
			break;
		case IASTLiteralExpression.lk_true:
			out.literal = "true";
			break;
		case IASTLiteralExpression.lk_char_constant:
		case IASTLiteralExpression.lk_float_constant:
		case IASTLiteralExpression.lk_string_literal:
		case IASTLiteralExpression.lk_integer_constant:
			out.literal = String.valueOf(lit.getValue());
			break;
		case IASTLiteralExpression.lk_this:
			out.literal = "this";
			break;
		}
		
		ret.add(out);
	}
	
	void modifyLiteralToPtr(MExpression lit) throws DOMException 
	{
		if (!(lit instanceof MLiteralExpression))
			return;
		
		MLiteralExpression expr = (MLiteralExpression) lit;
		
		if (expr.literal.equals("0"))
			expr.literal = "PtrObjNull.instance()";
		else if (expr.literal.equals("this"))
			/* Do nothing. */;
		else
		{
			MyLogger.logImportant("Not a ptr literal: " + expr.literal);
			MyLogger.exitOnError();
		}
	}
	
	private void evalExprUnary(IASTUnaryExpression expr, List<MExpression> ret) throws DOMException
	{
		if (expr.getOperator() == IASTUnaryExpression.op_bracketedPrimary)
		{
			MBracketExpression bra = new MBracketExpression();
			bra.operand = eval1Expr(expr.getOperand());
			ret.add(bra);
		}
		else if (expr.getOperator() == IASTUnaryExpression.op_amper)
		{
			if (ExpressionHelpers.isEventualPtrDeref(expr.getOperand()))
			{
				MAddressOfExpressionArrayItem add = new MAddressOfExpressionArrayItem();
				add.operand = eval1Expr(expr.getOperand());
				ret.add(add);
			}
			else if (TypeManager.isOneOf(expr.getOperand().getExpressionType(), TypeEnum.BASIC_POINTER))
			{
				MAddressOfExpressionPtr add = new MAddressOfExpressionPtr();
				add.operand = eval1Expr(expr.getOperand());
				ret.add(add);
			}
			else
			{
				MAddressOfExpression add = new MAddressOfExpression();
				add.operand = eval1Expr(expr.getOperand());
				ret.add(add);
			}
		}
		else if (expr.getOperator() == IASTUnaryExpression.op_star)
		{
			MPrefixExpressionPointerStar pre = new MPrefixExpressionPointerStar();
			pre.operand = eval1Expr(expr.getOperand());
			ret.add(pre);
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getExpressionType()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionPointerInc post = new MPostfixExpressionPointerInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionPointerDec post = new MPostfixExpressionPointerDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionPointerDec pre = new MPrefixExpressionPointerDec();
				pre.operand = eval1Expr(expr.getOperand());
				ret.add(pre);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionPointerInc pre = new MPrefixExpressionPointerInc();
				pre.operand = eval1Expr(expr.getOperand());
				ret.add(pre);
			}
			else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionPointer pre = new MPrefixExpressionPointer();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
				ret.add(pre);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_amper)
			{
				MAddressOfExpression add = new MAddressOfExpression();
				add.operand = eval1Expr(expr.getOperand());
				ret.add(add);
			}
		}
		else if (ctx.bitfieldMngr.isBitfield(expr.getOperand()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionBitfieldInc post = new MPostfixExpressionBitfieldInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionBitfieldDec post = new MPostfixExpressionBitfieldDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionBitfieldInc post = new MPrefixExpressionBitfieldInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionBitfieldDec post = new MPrefixExpressionBitfieldDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionBitfield pre = new MPrefixExpressionBitfield();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
				ret.add(pre);
			}
		}
		else if (ExpressionHelpers.isBasicExpression(expr.getOperand()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionNumberInc post = new MPostfixExpressionNumberInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionNumberDec post = new MPostfixExpressionNumberDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionNumberInc post = new MPrefixExpressionNumberInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionNumberDec post = new MPrefixExpressionNumberDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionPlain pre = new MPrefixExpressionPlain();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
				ret.add(pre);
			}
		}
		// TODO else if (isEnumerator())
		else if (ExpressionHelpers.isPostfixExpression(expr.getOperator()))
		{
			MPostfixExpressionPlain postfix = new MPostfixExpressionPlain();
			postfix.operand = eval1Expr(expr.getOperand());
			postfix.operator = ExpressionHelpers.evalUnaryPostfixOperator(expr.getOperator());
			ret.add(postfix);
		}
		else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
		{
			MPrefixExpressionPlain pre = new MPrefixExpressionPlain();
			pre.operand = eval1Expr(expr.getOperand());
			pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
			ret.add(pre);
		}
	}

	private IASTName getIFunctionFromFuncCallExpr(IASTFunctionCallExpression expr)
	{
		if (expr.getFunctionNameExpression() instanceof IASTIdExpression)
		{
			return ((IASTIdExpression) expr.getFunctionNameExpression()).getName();
		}
		else // if (expr.getFunctionNameExpression() instanceof IASTFieldReference)
		{
			return ((IASTFieldReference) expr.getFunctionNameExpression()).getFieldName();
		}		
	}
	
	private void evalExprFuncCallArgs(IASTFunctionCallExpression expr, List<MExpression> args) throws DOMException
	{
		IFunction funcb = (IFunction) getIFunctionFromFuncCallExpr(expr).resolveBinding();
		IParameter[] params = funcb.getParameters();
		
		for (int i = 0; i < params.length; i++)
		{
			IASTExpression argExpr = (IASTExpression) expr.getArguments()[i];
			args.add(wrapIfNeeded(argExpr, params[i].getType()));
		}
	}
	
	private void evalExprFuncCall(IASTFunctionCallExpression expr, List<MExpression> ret) throws DOMException
	{
		if (expr.getFunctionNameExpression() instanceof IASTIdExpression &&
			((IASTIdExpression) expr.getFunctionNameExpression()).getName().resolveBinding() instanceof ICPPClassType)
		{
			// TODO: Redirect function call to correct location.
			
			MClassInstanceCreation func = new MClassInstanceCreation();
			func.name = eval1Expr(expr.getFunctionNameExpression());
			
			for (IASTInitializerClause cls : expr.getArguments())
			{
				IASTExpression argExpr = (IASTExpression) cls;
				// TODO: Correct func arg type.
				func.args.add(wrapIfNeeded(argExpr, argExpr.getExpressionType()));
			}

			ret.add(func);
		}
		else
		{
			IASTName funcNm = getIFunctionFromFuncCallExpr(expr);
			IFunction funcb = (IFunction) funcNm.resolveBinding();

			CppFunction funcDecl = (CppFunction) ctx.typeMngr.getDeclFromTypeName(ctx.converter.evalBindingReturnType(funcb), funcNm);
			funcDecl.isUsed = true;

			MFunctionCallExpression func = new MFunctionCallExpression();
			func.name = eval1Expr(expr.getFunctionNameExpression());
			
			if (func.name instanceof MIdentityExpression)
			{
				if (funcDecl.isOriginallyGlobal)
					((MIdentityExpression) func.name).ident = funcDecl.parent.name + '.' + funcDecl.name;
				else
					((MIdentityExpression) func.name).ident = funcDecl.name;
			}
			else if (func.name instanceof MFieldReferenceExpression)
			{
				((MFieldReferenceExpression) func.name).field = funcDecl.name;
			}

			evalExprFuncCallArgs(expr, func.args);
			ret.add(func);
		}
	}

	private void evalExprBinary(IASTBinaryExpression expr, List<MExpression> ret) throws DOMException 
	{
		if (ctx.bitfieldMngr.isBitfield(expr.getOperand1()))
		{
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				MInfixAssignmentWithBitfieldOnLeft infix = new MInfixAssignmentWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				ret.add(infix);
			}
			else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()))
			{
				MCompoundWithBitfieldOnLeft infix = new MCompoundWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
				ret.add(infix);
			}
			else
			{
				MInfixExpressionWithBitfieldOnLeft infix = new MInfixExpressionWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
				ret.add(infix);
			}
		}
		else if (ExpressionHelpers.isBasicExpression(expr.getOperand1()))
		{
			MInfixExpression infix = null;
			
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				infix = new MInfixAssignmentWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				ret.add(infix);
			}
			else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()))
			{
				infix = new MCompoundWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
				ret.add(infix);
			}
			else
			{
				infix = new MInfixWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
				ret.add(infix);
			}
			
			if (ExpressionHelpers.needBooleanExpressions(expr.getOperator()))
			{
				infix.left = ExpressionHelpers.makeExpressionBoolean(infix.left, expr.getOperand1());
				infix.right = ExpressionHelpers.makeExpressionBoolean(infix.right, expr.getOperand2());
			}
			else if (ExpressionHelpers.isBooleanExpression(expr.getOperand1()) && expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				infix.right = ExpressionHelpers.makeExpressionBoolean(infix.right, expr.getOperand2());
			}
		}
		else if(ExpressionHelpers.isEventualPtrDeref(expr.getOperand1()))
		{
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				MInfixAssignmentWithDerefOnLeft infix = new MInfixAssignmentWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				ret.add(infix);
			}
			else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()))
			{
				MCompoundWithDerefOnLeft infix = new MCompoundWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
				ret.add(infix);
			}
			else
			{
				MInfixExpressionWithDerefOnLeft infix = new MInfixExpressionWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
				ret.add(infix);
			}
		}
		else if (expr.getOperator() == IASTBinaryExpression.op_assign &&
				TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MInfixAssignmentWithPtrOnLeft infix = new MInfixAssignmentWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			modifyLiteralToPtr(infix.right);
			
			ret.add(infix);
		}
		else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()) &&
				TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MCompoundWithPtrOnLeft infix = new MCompoundWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
			ret.add(infix);
		}
		else if ((expr.getOperator() == IASTBinaryExpression.op_minus ||
				 expr.getOperator() == IASTBinaryExpression.op_plus) &&
				 TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MInfixExpressionWithPtrOnLeft infix = new MInfixExpressionWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			ret.add(infix);
		}
		else if ((expr.getOperator() == IASTBinaryExpression.op_minus ||
				 expr.getOperator() == IASTBinaryExpression.op_plus) &&
				 TypeManager.isPtrOrArrayBasic(expr.getOperand2().getExpressionType()))
		{
			MInfixExpressionWithPtrOnRight infix = new MInfixExpressionWithPtrOnRight();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			ret.add(infix);
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MInfixExpressionPtrComparison infix = new MInfixExpressionPtrComparison();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			
			modifyLiteralToPtr(infix.left);
			modifyLiteralToPtr(infix.right);
			
			ret.add(infix);
		}
		else
		{
			MInfixExpressionPlain infix = new MInfixExpressionPlain();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			ret.add(infix);
		}
	}
	
	/**
	 * Gets the expressions for the array sizes.
	 * Eg. int a[1][2 + 5] returns a list containing expressions
	 * [1, 2 + 5].
	 */
	List<MExpression> getArraySizeExpressions(IType type) throws DOMException
	{
		List<MExpression> ret = new ArrayList<MExpression>();

		IArrayType arr = (IArrayType) type;
		ret.add(eval1Expr(arr.getArraySizeExpression()));

		while (arr.getType() instanceof IArrayType)
		{
			IArrayType arr2 = (IArrayType) arr.getType();
			ret.add(eval1Expr(arr2.getArraySizeExpression()));
			arr = arr2;
		}

		return ret;
	}
	
	/**
	 * This function should be called for return values, function arguments and rhs of direct
	 * assignment. It will wrap the expression, if required, so it is copied. 
	 */
	MExpression wrapIfNeeded(IASTExpression cppExpr, IType tpRequired) throws DOMException
	{
		if (TypeManager.isOneOf(tpRequired, TypeEnum.BASIC_REFERENCE))
		{
			// Prevents operand being copied
			MRefWrapper wrap = new MRefWrapper();
			wrap.operand = eval1Expr(cppExpr);
			return wrap;
		}
		else if (TypeManager.isBasicType(tpRequired))
		{
			// MInteger.valueOf(101)
			MValueOfExpressionNumber expr = new MValueOfExpressionNumber();
			expr.type = ctx.typeMngr.cppToJavaType(tpRequired, TypeType.IMPLEMENTATION);
			expr.operand = eval1Expr(cppExpr);
			
			if (TypeManager.isOneOf(tpRequired, TypeEnum.BOOLEAN))
				expr.operand = ExpressionHelpers.makeExpressionBoolean(expr.operand, cppExpr);

			return expr;
		}
		else
		{
			return eval1Expr(cppExpr);
		}
	}

	/**
	 * Given a type, creates a factory create expression.
	 * eg. 'int' becomes 'MInteger.valueOf(0)'
	 */
	MExpression makeSimpleCreationExpression(IType tp) throws DOMException
	{
		if (TypeManager.isBasicType(tp) || 
			(TypeManager.isOneOf(tp, TypeEnum.BASIC_POINTER) &&
			 TypeManager.getPointerIndirectionCount(tp) == 1))
		{
			// MInteger.valueOf(0)
			String literal = "0";
		
			// MBoolean.valueOf(false)
			if (TypeManager.isOneOf(tp, TypeEnum.BOOLEAN))
				literal = "false";
		
			MValueOfExpressionNumber expr = new MValueOfExpressionNumber();
			expr.operand = ModelCreation.createLiteral(literal);
			expr.type = ctx.typeMngr.cppToJavaType(tp, TypeType.IMPLEMENTATION);
			return expr;
		}
		else if (TypeManager.isOneOf(tp, TypeEnum.BASIC_ARRAY))
		{
			// MIntegerMulti.create(4);
			MValueOfExpressionArray expr = new MValueOfExpressionArray();
			expr.type = ctx.typeMngr.cppToJavaType(tp, TypeType.IMPLEMENTATION);
			expr.operands = ctx.exprEvaluator.getArraySizeExpressions(tp);
			return expr;
		}
		else if (TypeManager.isOneOf(tp, TypeEnum.BASIC_POINTER))
		{
			// int ** a; becomes:
			// IPtrObject<IInteger> a = PtrObject.valueOf(null);
			// short *** b; becomes:
			// IPtrObject<IPtrObject<IInteger>> b = PtrObject.valueOf(PtrObject.valueOf(null));
			int cnt = TypeManager.getPointerIndirectionCount(tp) - 1;

			MExpression wrap = ModelCreation.createLiteral("null");
			
			while (cnt-- > 0)
			{
				wrap = ModelCreation.createFuncCall("PtrObject.valueOf", wrap);
			}

			return wrap;
		}
		else if (TypeManager.isOneOf(tp, TypeEnum.OBJECT_POINTER))
		{
			// foo * a; becomes:
			// IPtrObject<foo> a = PtrObject.valueOf(null);
			// foo ** b; becomes:
			// IPtrObject<IPtrObject<foo>> b = PtrObject.valueOf(PtrObject.valueOf(null));
			int cnt = TypeManager.getPointerIndirectionCount(tp);

			MExpression wrap = ModelCreation.createLiteral("null");
			
			while (cnt-- > 0)
			{
				wrap = ModelCreation.createFuncCall("PtrObject.valueOf", wrap);
			}
			
			return wrap;
		}
		else
		{
			return null;
		}
	}
}
