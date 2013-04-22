package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MCompoundStmt;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.MSimpleDecl;

class DeclarationModels 
{
	abstract static class CppDeclaration
	{ 
		public List<CppDeclaration> declarations = new ArrayList<CppDeclaration>();
	}
	
	static class CppDtor extends CppDeclaration
	{
		public boolean isGeneratedDtor = true;
		public MCompoundStmt body;
		public boolean hasSuper;
	}

	static class CppAssign extends CppDeclaration
	{
		public boolean isGeneratedAssign = true;
		public MCompoundStmt body;
		public boolean hasSuper;
		public String type;
	}
	
	static class CppCtor extends CppDeclaration
	{
		public boolean isGeneratedCtor = true;
		public MCompoundStmt body;
		public String type;
		public boolean hasSuper;
	}
	
	
	static class CppFunction extends CppDeclaration
	{
		public boolean isFunctionDeclaration = true;
		
		public String name;
		public String retType;
		public List<MSimpleDecl> args = new ArrayList<MSimpleDecl>();
		public MCompoundStmt body;
		public boolean isStatic;
		public boolean isCtor;
		public boolean isDtor;
	}
	
	static class CppClass extends CppDeclaration
	{
		public boolean isClassDeclaration = true;
		
		public String name;
		public boolean isNested;
		public boolean isUnion;
		public String superclass;
		public List<String> additionalSupers = new ArrayList<String>(0);
	}
	
	static class CppEnumerator
	{
		public boolean isEnumerator = true;
		public String name;
		public MExpression value;
	}
	
	static class CppEnum extends CppDeclaration
	{
		public boolean isEnum = true;
		public String simpleName;
		public String qualified;
		public List<CppEnumerator> enumerators = new ArrayList<CppEnumerator>();
	}
	
	static class CppBitfield extends CppDeclaration
	{
		public boolean isBitfield = true;
		public String name;
		public String qualified;
		public String type;
		public MExpression bits;
	}
}
