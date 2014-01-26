package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MCompoundStmt;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.MSimpleDecl;

class DeclarationModels 
{
	abstract static class CppDeclaration
	{ 
		// The raw qualified (if needed) C++ name.
		public String completeCppName;

		// The simple Java name.
		public String name;
		
		public CppClass parent;
		public IType cppType;
		public IASTName nm;
		public String file;
		public int line;
	}
	
	class CppDtor extends CppDeclaration
	{
		public boolean isGeneratedDtor = true;
		public MCompoundStmt body;
		public boolean hasSuper;
	}

	class CppAssign extends CppDeclaration
	{
		public boolean isGeneratedAssign = true;
		public MCompoundStmt body;
		public boolean hasSuper;
		public String type;
	}
	
	class CppCtor extends CppDeclaration
	{
		public boolean isGeneratedCtor = true;
		public MCompoundStmt body;
		public String type;
		public boolean hasSuper;
	}
	
	class CppFunction extends CppDeclaration
	{
		public boolean isFunctionDeclaration = true;
		public String retType;
		public List<MSimpleDecl> args = new ArrayList<MSimpleDecl>();
		public MCompoundStmt body;
		public boolean isStatic;
		public boolean isCtor;
		public boolean isDtor;
		public boolean isOverride;
		public boolean isUsed;
		public boolean isOriginallyGlobal;
		
		boolean isCastOperator;
		IASTTypeId castType;
	}
	
	class CppClass extends CppDeclaration
	{
		public boolean isClassDeclaration = true;
		public boolean isNested;
		public boolean isUnion;
		public String superclass;
		public List<String> additionalSupers = new ArrayList<String>(0);
		public List<CppDeclaration> declarations = new ArrayList<CppDeclaration>();
	}
	
	class CppEnumerator extends CppDeclaration
	{
		public boolean isEnumerator = true;
		public MExpression value;
	}
	
	class CppEnum extends CppDeclaration
	{
		public boolean isEnum = true;
		public boolean isNested;
		public List<CppEnumerator> enumerators = new ArrayList<CppEnumerator>();
	}
	
	class CppBitfield extends CppDeclaration
	{
		public boolean isBitfield = true;
		public String qualified;
		public String type;
		public MExpression bits;
	}
}
