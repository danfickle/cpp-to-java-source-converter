package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import com.github.danfickle.cpptojavasourceconverter.Models.CppBitfield;


///**
//* Generates getters and setters for bit fields.
//* For example:
//*   int test_with_bit_field : 1;
//* would generate:
//* 	 int get__test_with_bit_field()
//*   {
//*	   return __bitfields & 1;
//*   }
//*   int set__test_with_bit_field(int val)
//*   {
//*     __bitfields &= ~1;
//*	   __bitfields |= (val << 0) & 1;
//*     return val;
//*   }
//*   int postInc__test_with_bit_field()
//*   {
//*     int ret = get__test_with_bit_field();
//*     set__test_with_bit_field(ret + 1);
//*     return ret;
//*   }
//*   int postDec__test_with_bit_field()
//*   {
//*     int ret = get__test_with_bit_field();
//*     set__test_with_bit_field(ret - 1);
//*     return ret;
//*   }
//*/
class BitfieldView
{
	AST ast;
	JASTHelper jast;

	static class JBitfieldDeclaration
	{
		MethodDeclaration get;
		MethodDeclaration set;
		MethodDeclaration inc;
		MethodDeclaration dec;
	}

	JBitfieldDeclaration generateBitField(CppBitfield bitfield)
	{
		JBitfieldDeclaration decl = new JBitfieldDeclaration();
		
		decl.get = generateBitFieldGetter(bitfield);
		decl.set = generateBitFieldSetter(bitfield);
		decl.inc = generateBitFieldIncDec(bitfield, true);
		decl.dec = generateBitFieldIncDec(bitfield, false);
		
		return decl;
	}

	/**
	 * Generates a bit field post-increment or decrement method.
	 */
	@SuppressWarnings("unchecked")
	private MethodDeclaration generateBitFieldIncDec(CppBitfield field, boolean inc)
	{
		JASTHelper.MethodDecl methodInc = jast.newMethodDecl()
				.returnType(field.m_type)
				.name((inc ? "postInc__" : "postDec__") + field.m_simpleName);
	
		MethodInvocation methodGetCall = jast.newMethod()
				.call("get__" + field.m_simpleName).toAST();
		
		VariableDeclarationStatement decl = jast.newVarDeclStmt()
				.name("ret")
				.type(field.m_type)
				.init(methodGetCall)
				.toAST();
		
		InfixExpression infix = jast.newInfix()
				.left(ast.newSimpleName("ret"))
				.right(jast.newNumber(1))
				.op(inc ? InfixExpression.Operator.PLUS : InfixExpression.Operator.MINUS)
				.toAST();
		
		MethodInvocation methodSetCall = jast.newMethod()
				.call("set__" + field.m_simpleName)
				.with(infix).toAST();
		
		ExpressionStatement stmt = ast.newExpressionStatement(methodSetCall);
		ReturnStatement retStmt = jast.newReturn(ast.newSimpleName("ret"));
		
		Block blk = ast.newBlock();
		blk.statements().add(decl);
		blk.statements().add(stmt);
		blk.statements().add(retStmt);
		methodInc.toAST().setBody(blk);

		return methodInc.toAST();
	}
	
	/**
	 * Generates a bitfield set method.
	 */
	@SuppressWarnings("unchecked")
	private MethodDeclaration generateBitFieldSetter(CppBitfield field)
	{
		JASTHelper.MethodDecl methodSet = jast.newMethodDecl()
				.returnType(field.m_type)
				.name("set__" + field.m_simpleName);
		
		SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
		var.setType(field.m_type);
		var.setName(ast.newSimpleName("val"));
		methodSet.toAST().parameters().add(var);

		PrefixExpression prefix = ast.newPrefixExpression();
		prefix.setOperator(PrefixExpression.Operator.COMPLEMENT);
		prefix.setOperand(ast.newNumberLiteral(String.valueOf(1)));

		Assignment assign = jast.newAssign()
				.left(ast.newSimpleName("__bitfields"))
				.right(prefix)
				.op(Assignment.Operator.BIT_AND_ASSIGN).toAST();
				
		ExpressionStatement exprStmt = ast.newExpressionStatement(assign);

		InfixExpression shift = jast.newInfix()
				.left(ast.newSimpleName("val"))
				.right(jast.newNumber(0))
				.op(InfixExpression.Operator.LEFT_SHIFT).toAST();

		ParenthesizedExpression paren = jast.newParen(shift);

		InfixExpression mask = jast.newInfix()
				.left(paren)
				.right(jast.newNumber(1))
				.op(InfixExpression.Operator.AND).toAST();

		Assignment orequal = jast.newAssign()
				.left(ast.newSimpleName("__bitfields"))
				.right(mask)
				.op(Assignment.Operator.BIT_OR_ASSIGN).toAST();

		Block funcBlockSet = ast.newBlock();
		funcBlockSet.statements().add(exprStmt);
		funcBlockSet.statements().add(ast.newExpressionStatement(orequal));
		funcBlockSet.statements().add(jast.newReturn(ast.newSimpleName("val")));
		methodSet.toAST().setBody(funcBlockSet);

		return methodSet.toAST();
	}
	
	/**
	 * Generates a bit field get method.
	 */
	@SuppressWarnings("unchecked")
	private MethodDeclaration generateBitFieldGetter(CppBitfield field)
	{
		JASTHelper.MethodDecl methodBit = jast.newMethodDecl()
				.returnType(field.m_type)
				.name("get__" + field.m_simpleName);

		InfixExpression infix = jast.newInfix()
				.left(ast.newSimpleName("__bitfields"))
				.right(jast.newNumber(1))
				.op(InfixExpression.Operator.AND).toAST();
		
		ReturnStatement ret = jast.newReturn(infix);

		Block funcBlock = ast.newBlock();
		funcBlock.statements().add(ret);
		methodBit.toAST().setBody(funcBlock);

		return methodBit.toAST();
	}

}
