package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

class JASTHelper
{
	private AST ast;
	
	JASTHelper(AST mast)
	{
		ast = mast;
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
	
	NumberLiteral newNumber(int num)
	{
		return ast.newNumberLiteral(String.valueOf(num));
	}
	
	SimpleType newType(String tp)
	{
		return ast.newSimpleType(ast.newSimpleName(tp));
	}
}
