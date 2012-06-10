package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

class JASTHelper
{
	private AST ast;
	
	JASTHelper(AST mast)
	{
		ast = mast;
	}

	class ClassCreate
	{
		ClassInstanceCreation create = ast.newClassInstanceCreation();
		
		ClassCreate type(Type tp)
		{
			create.setType(tp);
			return this;
		}

		ClassCreate withAll(List<Expression> exprs)
		{
			create.arguments().addAll(exprs);
			return this;
		}

		ClassCreate with(Expression expr)
		{
			create.arguments().add(expr);
			return this;
		}
		
		ClassInstanceCreation toAST()
		{
			return create;
		}
	}
	
	class AssignExpr
	{
		Assignment ass = ast.newAssignment();
		
		AssignExpr left(Expression expr)
		{
			ass.setLeftHandSide(expr);
			return this;
		}

		AssignExpr right(Expression expr)
		{
			ass.setRightHandSide(expr);
			return this;
		}
		
		AssignExpr op(Assignment.Operator op)
		{
			ass.setOperator(op);
			return this;
		}
		
		Assignment toAST()
		{
			return ass;
		}
	}
	class InfixExpr
	{
		InfixExpression infix = ast.newInfixExpression();
		
		InfixExpr left(Expression expr)
		{
			infix.setLeftOperand(expr);
			return this;
		}

		InfixExpr right(Expression expr)
		{
			infix.setRightOperand(expr);
			return this;
		}
		
		InfixExpr op(InfixExpression.Operator op)
		{
			infix.setOperator(op);
			return this;
		}
		
		InfixExpression toAST()
		{
			return infix;
		}
	}
	
	class MethodDecl 
	{
		MethodDeclaration methodDef = ast.newMethodDeclaration();
	
		MethodDecl name(String nm)
		{
			methodDef.setName(ast.newSimpleName(nm));
			return this;
		}

		MethodDecl returnType(Type tp)
		{
			methodDef.setReturnType2(tp);
			return this;
		}
		
		MethodDecl setStatic(boolean set)
		{
			if (set)
				methodDef.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
			return this;
		}
		
		MethodDecl setCtor(boolean set)
		{
			if (set)
				methodDef.setConstructor(true);
			return this;
		}
		
		MethodDeclaration toAST()
		{
			return methodDef;
		}
	}
	
	/**
	 * A class to encapsulate a JDT method.
	 * eg. h.newMethod().on("StackHelper").call("cleanup"); 
	 * @author DanFickle
	 */
	class Method
	{
		MethodInvocation method = ast.newMethodInvocation();

		Method on(Expression object)
		{
			return on(object, false);
		}
		
		Method on(Expression object, boolean addBrackets)
		{
			if (!addBrackets)
			{
				method.setExpression(object);
			}
			else
			{
				ParenthesizedExpression paren = ast.newParenthesizedExpression();
				paren.setExpression(object);
				method.setExpression(paren);
			}
			return this;
		}
		
		Method on(String object)
		{
			return on(ast.newSimpleName(object), false);
		}

		Method on(String object, boolean addBrackets)
		{
			return on(ast.newSimpleName(object), addBrackets);
		}
		
		Method call(String methName)
		{
			method.setName(ast.newSimpleName(methName));
			return this;
		}
		
		MethodInvocation toAST()
		{
			return method;
		}
		
		Method with(Expression arg)
		{
			method.arguments().add(arg);
			return this;
		}
		
		Method with(String arg)
		{
			method.arguments().add(ast.newSimpleName(arg));
			return this;
		}
		
		Method with(int arg)
		{
			method.arguments().add(newNumber(arg));
			return this;
		}
		
		
		Method withArguments(List<Expression> args)
		{
			method.arguments().addAll(args);
			return this;
		}
	}
	
	class ArrayCreate
	{
		ArrayCreation arrayCreate = ast.newArrayCreation();
		ArrayCreate onType(String tp)
		{
			arrayCreate.setType(ast.newArrayType(newType(tp)));
			return this;
		}
		
		ArrayCreate dim(int num)
		{
			arrayCreate.dimensions().add(newNumber(num));
			return this;
		}

		ArrayCreation toAST()
		{
			return arrayCreate;
		}
	}
	
	class VariableDeclarationStmt
	{
		VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
		VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(frag);
		
		VariableDeclarationStmt name(String nm)
		{
			frag.setName(ast.newSimpleName(nm));
			return this;
		}

		VariableDeclarationStmt init(Expression expr)
		{
			frag.setInitializer(expr);
			return this;
		}
		
		VariableDeclarationStmt type(Type tp)
		{
			stmt.setType(tp);
			return this;
		}
		
		VariableDeclarationStatement toAST()
		{
			return stmt;
		}
	}
	
	void setAST(AST mast)
	{
		ast = mast;
	}
	
	Method newMethod()
	{
		return new Method();
	}
	
	ArrayCreate newArray()
	{
		return new ArrayCreate();
	}
	
	VariableDeclarationStmt newVarDeclStmt()
	{
		return new VariableDeclarationStmt();
	}
	
	MethodDecl newMethodDecl()
	{
		return new MethodDecl();
	}
	
	InfixExpr newInfix()
	{
		return new InfixExpr();
	}
	
	AssignExpr newAssign()
	{
		return new AssignExpr();
	}
	
	ClassCreate newClassCreate()
	{
		return new ClassCreate();
	}
	
	NumberLiteral newNumber(int num)
	{
		return ast.newNumberLiteral(String.valueOf(num));
	}
	
	SimpleType newType(String tp)
	{
		return ast.newSimpleType(ast.newSimpleName(tp));
	}
	
	ReturnStatement newReturn(Expression ret)
	{
		ReturnStatement retStmt = ast.newReturnStatement();
		
		if (ret != null)
			retStmt.setExpression(ret);
		
		return retStmt;
	}
	
	ParenthesizedExpression newParen(Expression enclose)
	{
		ParenthesizedExpression paren = ast.newParenthesizedExpression();
		paren.setExpression(enclose);
		return paren;
	}
}
