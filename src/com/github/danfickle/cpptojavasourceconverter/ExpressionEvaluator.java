package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.core.dom.ast.gnu.*;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppFunction;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppDeclaration;
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
	
	/**
	 * Given a C++ expression, attempts to convert it to one Java expression.
	 */
	MExpression eval1Expr(IASTExpression expression) throws DOMException
	{
		if (expression instanceof IASTLiteralExpression)
		{
			return evalExprLiteral((IASTLiteralExpression) expression);
		}
		else if (expression instanceof IASTIdExpression)
		{
			return evalExprId((IASTIdExpression) expression);
		}
		else if (expression instanceof IASTFieldReference)
		{
			return evalExprFieldReference((IASTFieldReference) expression);
		}
		else if (expression instanceof IASTUnaryExpression)
		{
			return evalExprUnary((IASTUnaryExpression) expression);
		}
		else if (expression instanceof IASTConditionalExpression)
		{
			return evalExprConditional((IASTConditionalExpression) expression);
		}
		else if (expression instanceof IASTArraySubscriptExpression)
		{
			return evalExprArraySubscript((IASTArraySubscriptExpression) expression);
		}
		else if (expression instanceof IASTBinaryExpression)
		{
			return evalExprBinary((IASTBinaryExpression) expression);
		}
		else if (expression instanceof ICPPASTDeleteExpression)
		{
			return evalExprDelete((ICPPASTDeleteExpression) expression);
		}
		else if (expression instanceof ICPPASTNewExpression)
		{
			return evalExprNew((ICPPASTNewExpression) expression);
		}
		else if (expression instanceof IASTFunctionCallExpression)
		{
			return evalExprFuncCall((IASTFunctionCallExpression) expression);
		}
		else if (expression instanceof IASTCastExpression)
		{
			return evalCastExpression((IASTCastExpression) expression);
		}
		else if (expression instanceof IASTTypeIdExpression)
		{
			//evalTypeIdExpression((IASTTypeIdExpression) expression, ret, flags);
		}
		else if (expression instanceof ICASTTypeIdInitializerExpression)
		{
			ICASTTypeIdInitializerExpression typeIdInitializerExpression = (ICASTTypeIdInitializerExpression)expression;

			ctx.converter.eval1TypeIdReturnBinding(typeIdInitializerExpression.getTypeId());
			//evaluate(typeIdInitializerExpression.getInitializer());
		}
		else if (expression instanceof ICPPASTSimpleTypeConstructorExpression)
		{
			ICPPASTSimpleTypeConstructorExpression simpleTypeConstructorExpression = (ICPPASTSimpleTypeConstructorExpression)expression;
			//return evalExpr(simpleTypeConstructorExpression.getInitialValue());
		}
		else if (expression instanceof IGNUASTCompoundStatementExpression)
		{
			IGNUASTCompoundStatementExpression compoundStatementExpression = (IGNUASTCompoundStatementExpression)expression;
			ctx.stmtEvaluator.evalStmt(compoundStatementExpression.getCompoundStatement());
		}
		else if (expression instanceof IASTExpressionList)
		{
			IASTExpressionList list = (IASTExpressionList) expression;

//			for (IASTExpression childExpression : list.getExpressions())
//				ret.addAll(evalExpr(childExpression));
		}
		else if (expression == null)
		{
			return new MEmptyExpression();
		}

		MyLogger.logImportant("Unrecognized expression type: " + expression.getClass().getCanonicalName());
		return null;
	}
	
	@SuppressWarnings("deprecation")
	// TODO: getNewTypeIdArrayExpressions() is deprecated. Find another
	// way to get array count.
	private MExpression evalExprNew(ICPPASTNewExpression expr) throws DOMException
	{
		// NOTE: we don't handle operator new or operator new[]
		// overloads as manual memory management in Java is not desirable.
		
		if (expr.isArrayAllocation() && !TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			// Array of basic types.
			//   new short[400]
			//   MShort.create(400)
			MNewArrayExpression ptr = new MNewArrayExpression();
			
			for (IASTExpression arraySize : expr.getNewTypeIdArrayExpressions())
				ptr.sizes.add(eval1Expr(arraySize));
			
			ptr.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.IMPLEMENTATION);
			return ptr;
		}
		else if (expr.isArrayAllocation() && TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			// Array of objects.
			//   new object[500]
			//   PtrObjectMulti.create(object.class, 500)
			MNewArrayExpressionObject ptr = new MNewArrayExpressionObject();
			
			// Mark the constructor as used.
			ICPPConstructor ctor = (ICPPConstructor) ctx.converter.eval1TypeIdReturnBinding(expr.getTypeId());
			IASTName nm = ctx.converter.evalTypeIdReturnName(expr.getTypeId());
			funcDep(ctor, nm);
			
			for (IASTExpression arraySize : expr.getNewTypeIdArrayExpressions())
				ptr.sizes.add(eval1Expr(arraySize));
			
			ptr.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.RAW);
			return ptr;
		}
		else if (TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			// Single new object:
			//   new object(arg1, arg2)
			//   PtrObject.valueOf(new object(arg1, arg2))
			MNewExpressionObject ptr = new MNewExpressionObject();
			ptr.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.RAW);

			IBinding binding = ctx.converter.eval1TypeIdReturnBinding(expr.getTypeId());
			ICPPConstructor con = (ICPPConstructor) binding;
			ICPPParameter[] params = con.getParameters();
			IASTInitializer init = expr.getInitializer();

			// Mark the constructor as used.
			IASTName nm = ctx.converter.evalTypeIdReturnName(expr.getTypeId());
			funcDep(con, nm);
			
			if (init != null)
			{
				assert(init instanceof ICPPASTConstructorInitializer);
				
				ICPPASTConstructorInitializer con2 = (ICPPASTConstructorInitializer) init;
				IASTInitializerClause[] clss = con2.getArguments();
				
				for (int i = 0; i < clss.length; i++)
				{
					IASTExpression expr2 = (IASTExpression) clss[i];
					ptr.arguments.add(wrapIfNeeded(expr2, params[i].getType()));
				}
			}
			
			MValueOfExpressionPtr ptrExpr = new MValueOfExpressionPtr();
			ptrExpr.type = "PtrObject";
			ptrExpr.operand = ptr;
			return ptrExpr;
		}
		else if (TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.BASIC_POINTER) &&
				 TypeManager.getPointerIndirectionCount(expr.getExpressionType()) == 1)
		{
			// Single new number:
			//  new int(101)
			//  MInteger.valueOf(101)
			return ctx.initMngr.eval1Init(expr.getInitializer(), TypeManager.getPointerBaseType(expr.getExpressionType()), null, InitType.WRAPPED);
		}
		else
		{
			assert(false);
			return null;
		}
	}

	private MExpression evalExprDeleteDestructor(IASTName nm, IBinding binding, ICPPASTDeleteExpression expr) throws DOMException
	{
		assert (binding instanceof ICPPMethod);
		
		funcDep(binding, nm);

		if (!expr.isVectored())
		{
			MDeleteObjectSingle del = new MDeleteObjectSingle();
			del.operand = eval1Expr(expr.getOperand());
			return del;
		}
		else
		{
			MDeleteObjectMultiple del = new MDeleteObjectMultiple();
			del.operand = eval1Expr(expr.getOperand());
			return del;
		}
	}
	
	private MExpression evalExprDelete(ICPPASTDeleteExpression expr) throws DOMException
	{
		/**
		 * ICPPASTDeleteExpression => Methods
		 *   isVectored
		 *   getOperand
		 *   
		 * IASTImplicitNameOwner => Methods
		 *   getImplicitNames
		 */
		
		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) expr;

		if (owner.getImplicitNames().length != 0)
		{
			IASTName nm = (IASTName) owner.getImplicitNames()[0];
			IBinding binding = nm.resolveBinding();

			// NOTE: we don't handle operator delete or operator delete[]
			// overloads as manual memory management in Java is not desirable.
			//
			// TODO: Should we place a note next to each use of delete/
			// delete[] to tell the user that operator overloading is not 
			// supported. For now we just have a note in the README.
			//
			// NOTE: We used to support operator overloading for delete.
			// It was removed in a commit with message:
			// 'Removed handling of operator delete/delete[]' on 2014-01-23. 

			if (binding instanceof ICPPMethod && ((ICPPMethod) binding).isDestructor())
			{
				// For single object delete an implicit name is generated for the destructor.
				return evalExprDeleteDestructor(nm, binding, expr);
			}
			else if (TypeManager.isOneOf(expr.getOperand().getExpressionType(), TypeEnum.OBJECT_POINTER))
			{
				// Otherwise, for object arrays, we must generate the implicit
				// destructor calls (as an implicit name is not generated).
				// TODO: Is this a bug in CDT?
				MDeleteObjectMultiple del = new MDeleteObjectMultiple();
				del.operand = eval1Expr(expr.getOperand());
				return del;
			}
		}
		else if (TypeManager.isOneOf(expr.getOperand().getExpressionType(), TypeEnum.OBJECT_POINTER))
		{
			if (expr.isVectored())
			{
				MDeleteObjectMultiple del = new MDeleteObjectMultiple();
				del.operand = eval1Expr(expr.getOperand());
				return del;
			}
			else
			{
				MDeleteObjectSingle del = new MDeleteObjectSingle();
				del.operand = eval1Expr(expr.getOperand());
				return del;
			}
		}

		// Basic objects don't do anything when deleted.
		MEmptyExpression emp = new MEmptyExpression();
		return emp;
	}
	
	private MExpression evalExprId(IASTIdExpression expr) throws DOMException
	{
		/*
		 * IASTIdExpression => Methods
		 *   getName
		 */
		
		if (ctx.bitfieldMngr.isBitfield(expr.getName()))
		{
			MIdentityExpressionBitfield ident = new MIdentityExpressionBitfield();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			return ident;
		}
		else if (expr.getName().resolveBinding() instanceof IEnumerator)
		{
			MIdentityExpressionEnumerator ident = new MIdentityExpressionEnumerator();
			ident.enumName = ctx.enumMngr.getEnumerationDeclModel((IEnumerator) expr.getName().resolveBinding()).name;
			ident.ident = TypeManager.getSimpleName(expr.getName());
			return ident;
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getExpressionType()))
		{
			MIdentityExpressionPtr ident = new MIdentityExpressionPtr();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			return ident;
		}
		else if (ExpressionHelpers.isBasicExpression(expr))
		{
			MIdentityExpressionNumber ident = new MIdentityExpressionNumber();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			return ident;
		}
		else
		{
			MIdentityExpressionPlain ident = new MIdentityExpressionPlain();
			ident.ident = TypeManager.getSimpleName(expr.getName());
			return ident;
		}
	}

	private MExpression evalExprFieldReference(IASTFieldReference expr) throws DOMException
	{
		/*
		 * IASTFieldReference => Methods
		 *   getFieldName
		 *   getFieldOwner
		 *   isPointerDereference
		 *   
		 * IASTNameOwner => Methods
		 *   getRoleForName
		 */
		
		if (ctx.bitfieldMngr.isBitfield(expr.getFieldName()))
		{
			MFieldReferenceExpressionBitfield field = new MFieldReferenceExpressionBitfield();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			return field;
		}
		else if (expr.getFieldName().resolveBinding() instanceof IEnumerator)
		{
			MFieldReferenceExpressionEnumerator field = new MFieldReferenceExpressionEnumerator();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			return field;
		}
		else if (ExpressionHelpers.isBasicExpression(expr))
		{
			MFieldReferenceExpressionNumber field = new MFieldReferenceExpressionNumber();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			return field;
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getExpressionType()) && expr.isPointerDereference())
		{
			MFieldReferenceExpressionDeref field = new MFieldReferenceExpressionDeref();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			return field;
		}
		else
		{
			MFieldReferenceExpressionPlain field = new MFieldReferenceExpressionPlain();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = TypeManager.getSimpleName(expr.getFieldName());
			return field;
		}
	}


	private MExpression evalExprConditional(IASTConditionalExpression expr) throws DOMException 
	{
		/*
		 * IASTConditionalExpression => Methods
		 *   getLogicalConditionExpression
		 *   getNegativeResultExpression
		 *   getPositiveResultExpression
		 */

		MTernaryExpression ternary = new MTernaryExpression();
		
		ternary.condition = eval1Expr(expr.getLogicalConditionExpression());
		ternary.condition = ExpressionHelpers.makeExpressionBoolean(ternary.condition, expr.getLogicalConditionExpression());
		ternary.positive = eval1Expr(expr.getPositiveResultExpression());
		ternary.negative = eval1Expr(expr.getNegativeResultExpression());
		
		return ternary;
	}

	private MExpression evalCastExpression(IASTCastExpression expr) throws DOMException
	{
		if (ExpressionHelpers.isBasicExpression(expr))
		{
			MCastExpression cast = new MCastExpression();
			cast.operand = eval1Expr(expr.getOperand());
			cast.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.RAW);
			return cast;
		}
		else if (TypeManager.isOneOf(expr.getExpressionType(), TypeEnum.ENUMERATION))
		{
			MCastExpressionToEnum cast = new MCastExpressionToEnum();
			cast.operand = eval1Expr(expr.getOperand());
			cast.type = ctx.typeMngr.cppToJavaType(expr.getExpressionType(), TypeType.INTERFACE);
			return cast;
		}
		else
		{
			MyLogger.logImportant("No cast available: " + expr.getRawSignature());
			return null;
		}
	}

	private MExpression evalExprArraySubscript(IASTArraySubscriptExpression expr) throws DOMException
	{
		/**
		 * IASTArraySubscriptExpression => Methods
		 *   getArgument
		 *   getArrayExpression
		 * 
		 * IASTImplicitNameOwner => Methods
		 *   getImplicitNames
		 */
		
		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) expr;

		if (owner.getImplicitNames().length != 0)
		{
			IASTName nm = (IASTName) owner.getImplicitNames()[0];
			IBinding binding = nm.resolveBinding();

			funcDep(binding, nm);
			
			assert(binding instanceof ICPPMethod);
			
			MOverloadedMethodSubscript sub = new MOverloadedMethodSubscript();
			sub.object = eval1Expr(expr.getArrayExpression());
			sub.subscript = wrapIfNeeded((IASTExpression) expr.getArgument(), ((ICPPMethod) binding).getParameters()[0].getType());
			return sub;
		}
		else
		{
			MArrayExpressionPtr ptr = new MArrayExpressionPtr();
			ptr.operand = eval1Expr(expr.getArrayExpression());
			ptr.subscript = Arrays.asList(eval1Expr((IASTExpression) expr.getArgument()));
			return ptr;
		}
	}

	private MExpression evalExprLiteral(IASTLiteralExpression lit) throws DOMException 
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
		
		return out;
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
			assert(false);
	}
	
	private MExpression evalExprUnary(IASTUnaryExpression expr) throws DOMException
	{
		/*
		 * IASTUnaryExpression => Methods
		 *   getOperand
		 *   getOperator
		 *
		 * IASTUnaryExpression => Operators 
		 *   op_alignOf
		 *   op_amper
		 *   op_bracketedPrimary
		 *   op_minus
		 *   op_noexcept
		 *   op_not
		 *   op_plus
		 *   op_postFixDecr
		 *   op_postFixIncr
		 *   op_prefixDecr
		 *   op_prefixIncr
		 *   op_sizeof
		 *   op_sizeofParameterPack
		 *   op_star
		 *   op_throw
		 *   op_tilde
		 *   op_typeid
		 *   op_typeof
		 * 
		 * ICPPASTUnaryExpression => Operators
		 *   op_typeid
		 *   op_throw
		 * 
		 * ICPPASTUnaryExpression => Methods
		 *   getOverload
		 * 
		 * IASTImplicitNameOwner => Methods
		 *   getImplicitNames
		 */

		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) expr;

		if (owner.getImplicitNames().length != 0)
		{
			IASTName nm = (IASTName) owner.getImplicitNames()[0];
			IBinding binding = nm.resolveBinding();

			funcDep(binding, nm);
			
			if (binding instanceof ICPPMethod)
			{
				MOverloadedMethodUnary unary = new MOverloadedMethodUnary();
				unary.object = eval1Expr(expr.getOperand());
				
				// In C++, post increment is distinguished from pre increment
				// by a dummy int param.
				if (((ICPPMethod) binding).getParameters().length == 1)
				{
					if (binding.getName().equals("operator ++"))
					{
						unary.method = "opPostIncrement";
					}
					else
					{
						assert(binding.getName().equals("operator --"));
						unary.method = "opPostDecrement";
					}

					unary.withNullArgForPostIncAndDec = true;
				}
				else
				{
					assert(((ICPPMethod) binding).getParameters().length == 0);
					unary.method = TypeManager.normalizeName(binding.getName());
				}

				return unary;
			}
			else
			{
				assert(binding instanceof ICPPFunction);
				
				MOverloadedFunctionUnary unary = new MOverloadedFunctionUnary();
				unary.function = reparentFunctionCall(binding, nm);
				
				if (((ICPPFunction) binding).getParameters().length == 2)
				{
					if (binding.getName().equals("operator ++"))
					{
						unary.function = unary.function.replace("opPreIncrement", "opPostIncrement");
					}
					else
					{
						assert(binding.getName().equals("operator --"));
						unary.function = unary.function.replace("opPreDecrement", "opPostDecrement");
					}

					unary.withNullArgForPostIncAndDec = true;
				}
				else
				{
					assert(((ICPPFunction) binding).getParameters().length == 1);
				}

				unary.object = eval1Expr(expr.getOperand());
				return unary;
			}
		}
		else if (expr.getOperator() == IASTUnaryExpression.op_bracketedPrimary)
		{
			MBracketExpression bra = new MBracketExpression();
			bra.operand = eval1Expr(expr.getOperand());
			return bra;
		}
		else if (expr.getOperator() == IASTUnaryExpression.op_amper)
		{
			if (ExpressionHelpers.isEventualPtrDeref(expr.getOperand()))
			{
				MAddressOfExpressionArrayItem add = new MAddressOfExpressionArrayItem();
				add.operand = eval1Expr(expr.getOperand());
				return add;
			}
			else if (TypeManager.isOneOf(expr.getOperand().getExpressionType(), TypeEnum.BASIC_POINTER))
			{
				MAddressOfExpressionPtr add = new MAddressOfExpressionPtr();
				add.operand = eval1Expr(expr.getOperand());
				return add;
			}
			else
			{
				MAddressOfExpression add = new MAddressOfExpression();
				add.operand = eval1Expr(expr.getOperand());
				return add;
			}
		}
		else if (expr.getOperator() == IASTUnaryExpression.op_star)
		{
			MPrefixExpressionPointerStar pre = new MPrefixExpressionPointerStar();
			pre.operand = eval1Expr(expr.getOperand());

			if (pre.operand instanceof MIdentityExpression)
			{
				MIdentityExpression e = (MIdentityExpression) pre.operand;
				MIdentityExpressionDeref deref = new MIdentityExpressionDeref();
				
				deref.ident = e.ident;
				pre.operand = deref;
			}
			else if (pre.operand instanceof MFieldReferenceExpression)
			{
				MFieldReferenceExpression fr = (MFieldReferenceExpression) pre.operand;
				MFieldReferenceExpressionDeref deref = new MFieldReferenceExpressionDeref();
				
				deref.field = fr.field;
				deref.object = fr.object;
				pre.operand = deref;
			}
			
			return pre;
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getExpressionType()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionPointerInc post = new MPostfixExpressionPointerInc();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionPointerDec post = new MPostfixExpressionPointerDec();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionPointerDec pre = new MPrefixExpressionPointerDec();
				pre.operand = eval1Expr(expr.getOperand());
				return pre;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionPointerInc pre = new MPrefixExpressionPointerInc();
				pre.operand = eval1Expr(expr.getOperand());
				return pre;
			}
			else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
			{
				// TODO: Shouldn't get here!
				MPrefixExpressionPointer pre = new MPrefixExpressionPointer();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
				return pre;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_amper)
			{
				MAddressOfExpression add = new MAddressOfExpression();
				add.operand = eval1Expr(expr.getOperand());
				return add;
			}
		}
		else if (ctx.bitfieldMngr.isBitfield(expr.getOperand()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionBitfieldInc post = new MPostfixExpressionBitfieldInc();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionBitfieldDec post = new MPostfixExpressionBitfieldDec();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionBitfieldInc post = new MPrefixExpressionBitfieldInc();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionBitfieldDec post = new MPrefixExpressionBitfieldDec();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionBitfield pre = new MPrefixExpressionBitfield();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
				return pre;
			}
		}
		else if (ExpressionHelpers.isBasicExpression(expr.getOperand()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionNumberInc post = new MPostfixExpressionNumberInc();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionNumberDec post = new MPostfixExpressionNumberDec();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionNumberInc post = new MPrefixExpressionNumberInc();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionNumberDec post = new MPrefixExpressionNumberDec();
				post.operand = eval1Expr(expr.getOperand());
				return post;
			}
			else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionPlain pre = new MPrefixExpressionPlain();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
				return pre;
			}
		}
		// TODO else if (isEnumerator())
		else if (ExpressionHelpers.isPostfixExpression(expr.getOperator()))
		{
			MPostfixExpressionPlain postfix = new MPostfixExpressionPlain();
			postfix.operand = eval1Expr(expr.getOperand());
			postfix.operator = ExpressionHelpers.evalUnaryPostfixOperator(expr.getOperator());
			return postfix;
		}
		else if (ExpressionHelpers.isPrefixExpression(expr.getOperator()))
		{
			MPrefixExpressionPlain pre = new MPrefixExpressionPlain();
			pre.operand = eval1Expr(expr.getOperand());
			pre.operator = ExpressionHelpers.evalUnaryPrefixOperator(expr.getOperator());
			return pre;
		}

		assert(false);
		return null;
	}

	private IASTName getAstNameFromFuncCallExpr(IASTFunctionCallExpression expr)
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
	
	private void evalExprFuncCallArgs(IASTFunctionCallExpression expr, List<MExpression> args, IBinding binding) throws DOMException
	{
		IParameter[] params;
		
		if (binding instanceof IFunction)
		{
			params = ((IFunction) binding).getParameters();
		}
		else if (binding instanceof ICPPConstructor)
		{
			params = ((ICPPConstructor) binding).getParameters();
		}
		else
		{
			assert(binding instanceof ICPPMethod);
			params = ((ICPPMethod) binding).getParameters();
		}
	
		for (int i = 0; i < params.length; i++)
		{
			IASTExpression argExpr = (IASTExpression) expr.getArguments()[i];
			args.add(wrapIfNeeded(argExpr, params[i].getType()));
		}
	}
	
	private MExpression evalExprFuncCall(IASTFunctionCallExpression expr) throws DOMException
	{
		/**
		 * IASTFunctionCallExpression => Methods
		 *   getArguments
		 *   getFunctionNameExpression
		 *   
		 * IASTImplicitNameOwner => Methods
		 *   getImplicitNames
		 */
		
		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) expr;

		if (owner.getImplicitNames().length != 0)
		{
			IASTName nm = (IASTName) owner.getImplicitNames()[0];
			IBinding binding = nm.resolveBinding();

			funcDep(binding, nm);
			
			if (binding instanceof ICPPConstructor)
			{
				MClassInstanceCreation func = new MClassInstanceCreation();
				func.name = eval1Expr(expr.getFunctionNameExpression());
				evalExprFuncCallArgs(expr, func.args, binding);
				return func;
			}
			else if (binding instanceof ICPPMethod)
			{
				MOverloadedMethodFuncCall fcall = new MOverloadedMethodFuncCall();
				fcall.object = eval1Expr(expr.getFunctionNameExpression());
				evalExprFuncCallArgs(expr, fcall.args, binding);
				return fcall;
			}
			else
			{
				assert(false);
				return null;
			}
		}
		else
		{
			IASTName funcNm = getAstNameFromFuncCallExpr(expr);
			IBinding funcb = funcNm.resolveBinding();

			funcDep(funcb, funcNm);
			
			MFunctionCallExpression func = new MFunctionCallExpression();
			func.name = eval1Expr(expr.getFunctionNameExpression());
			reparentFunctionCall(funcb, funcNm, func.name);
			evalExprFuncCallArgs(expr, func.args, funcb);
			return func;
		}
	}

	/**
	 * Java doesn't have global functions, so chuck it in a class.
	 */
	private String reparentFunctionCall(IBinding binding, IASTName nm) throws DOMException
	{
		CppFunction funcDecl = (CppFunction) ctx.typeMngr.getDeclFromTypeName(ctx.converter.evalBindingReturnType(binding), nm);		
		assert(funcDecl.isOriginallyGlobal);
		return funcDecl.parent.name + '.' + funcDecl.name;
	}
	
	/**
	 * Mark a function as used.
	 */
	private void funcDep(IBinding binding, IASTName nm) throws DOMException
	{
		CppDeclaration decl = ctx.typeMngr.getDeclFromTypeName(ctx.converter.evalBindingReturnType(binding), nm);
		
		if (decl instanceof CppFunction)
		{
			((CppFunction) decl).isUsed = true;
		}
	}
	
	private void reparentFunctionCall(IBinding binding, IASTName nm, MExpression funcName) throws DOMException
	{
		if (ctx.typeMngr.getDeclFromTypeName(ctx.converter.evalBindingReturnType(binding), nm) instanceof CppFunction)
		{
			CppFunction funcDecl = (CppFunction) ctx.typeMngr.getDeclFromTypeName(ctx.converter.evalBindingReturnType(binding), nm);
		
			if (funcName != null && funcDecl.isOriginallyGlobal)
			{
				((MIdentityExpression) funcName).ident = funcDecl.parent.name + '.' + funcDecl.name;
			}
		}
	}
	
	private MExpression evalExprBinary(IASTBinaryExpression expr) throws DOMException 
	{
		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) expr;

		if (owner.getImplicitNames().length != 0)
		{
			IASTName nm = (IASTName) owner.getImplicitNames()[0];
			IBinding binding = nm.resolveBinding();

			funcDep(binding, nm);
			
			if (binding instanceof ICPPMethod)
			{
				MOverloadedMethodInfix infix = new MOverloadedMethodInfix();

				IParameter[] params = ((ICPPMethod) binding).getParameters();
				assert(params.length == 1);
				
				infix.object = eval1Expr(expr.getOperand1());
				infix.right = wrapIfNeeded(expr.getOperand2(), params[0].getType());
				infix.method = TypeManager.normalizeName(binding.getName());
				return infix;
			}
			else
			{
				assert(binding instanceof ICPPFunction);
				
				MOverloadedFunctionInfix infix = new MOverloadedFunctionInfix();

				IParameter[] params = ((ICPPFunction) binding).getParameters();
				assert(params.length == 2);

				infix.function = reparentFunctionCall(binding, nm);
				infix.left = wrapIfNeeded(expr.getOperand1(), params[0].getType());
				infix.right = wrapIfNeeded(expr.getOperand2(), params[1].getType());
				return infix;
			}
		}
		else if (ctx.bitfieldMngr.isBitfield(expr.getOperand1()))
		{
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				MInfixAssignmentWithBitfieldOnLeft infix = new MInfixAssignmentWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				return infix;
			}
			else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()))
			{
				MCompoundWithBitfieldOnLeft infix = new MCompoundWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
				return infix;
			}
			else
			{
				MInfixExpressionWithBitfieldOnLeft infix = new MInfixExpressionWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
				return infix;
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
			}
			else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()))
			{
				infix = new MCompoundWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
			}
			else
			{
				infix = new MInfixWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
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
			
			return infix;
		}
		else if(ExpressionHelpers.isEventualPtrDeref(expr.getOperand1()))
		{
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				MInfixAssignmentWithDerefOnLeft infix = new MInfixAssignmentWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				return infix;
			}
			else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()))
			{
				MCompoundWithDerefOnLeft infix = new MCompoundWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
				return infix;
			}
			else
			{
				MInfixExpressionWithDerefOnLeft infix = new MInfixExpressionWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
				return infix;
			}
		}
		else if (expr.getOperator() == IASTBinaryExpression.op_assign &&
				TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MInfixAssignmentWithPtrOnLeft infix = new MInfixAssignmentWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			modifyLiteralToPtr(infix.right);
			
			return infix;
		}
		else if (ExpressionHelpers.isAssignmentExpression(expr.getOperator()) &&
				TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MCompoundWithPtrOnLeft infix = new MCompoundWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.compoundAssignmentToInfixOperator(expr.getOperator());
			return infix;
		}
		else if ((expr.getOperator() == IASTBinaryExpression.op_minus ||
				 expr.getOperator() == IASTBinaryExpression.op_plus) &&
				 TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MInfixExpressionWithPtrOnLeft infix = new MInfixExpressionWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			return infix;
		}
		else if ((expr.getOperator() == IASTBinaryExpression.op_minus ||
				 expr.getOperator() == IASTBinaryExpression.op_plus) &&
				 TypeManager.isPtrOrArrayBasic(expr.getOperand2().getExpressionType()))
		{
			MInfixExpressionWithPtrOnRight infix = new MInfixExpressionWithPtrOnRight();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			return infix;
		}
		else if (TypeManager.isPtrOrArrayBasic(expr.getOperand1().getExpressionType()))
		{
			MInfixExpressionPtrComparison infix = new MInfixExpressionPtrComparison();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			
			modifyLiteralToPtr(infix.left);
			modifyLiteralToPtr(infix.right);
			
			return infix;
		}
		else
		{
			MInfixExpressionPlain infix = new MInfixExpressionPlain();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = ExpressionHelpers.evaluateBinaryOperator(expr.getOperator());
			return infix;
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
