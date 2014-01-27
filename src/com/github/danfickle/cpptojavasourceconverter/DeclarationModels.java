package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MCompoundStmt;

class DeclarationModels 
{
	private int tabLevel;
	
	private String tabOut()
	{
		switch (tabLevel)
		{
		case 0:
			return "";
		case 1:
			return "    ";
		case 2:
			return "        ";
		default:
		{
			StringBuilder sb = new StringBuilder("        ");

			for (int i = 3; i <= tabLevel; i++)
			{
				sb.append("    ");
			}

			return sb.toString();
		}
		}
	}
	
	private static String join(List<?> args)
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < args.size(); i++)
		{
			sb.append(args.get(i).toString());
			
			if (i != args.size() - 1)
				sb.append(", ");
		}
		
		return sb.toString();
	}
	
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
		public MCompoundStmt body;
		public boolean hasSuper;
		
		@Override
		public String toString() 
		{
			return String.format("\n%s@Override" +
			                     "\n%spublic void destruct()" +
			                     "\n%s%s", tabOut(), tabOut(), tabOut(), this.body);
		}
	}

	class CppAssign extends CppDeclaration
	{
		public MCompoundStmt body;
		public boolean hasSuper;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("\n%s@Override" +
			                     "\n%spublic %s opAssign(%s right)" +
			                     "\n%s%s", tabOut(), tabOut(), this.type, this.type, tabOut(), this.body);
		}
	}
	
	class CppCtor extends CppDeclaration
	{
		public MCompoundStmt body;
		public String type;
		public boolean hasSuper;
		
		@Override
		public String toString() 
		{
			return String.format("\n%spublic %s()" +
			                     "\n%s%s", tabOut(), this.type, tabOut(), this.body);
		}
	}
	
	class CppFunction extends CppDeclaration
	{
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
		
		@Override
		public String toString() 
		{
			String str = this.isOverride ? String.format("\n%s@Override", tabOut()) : "";
			str += String.format("\n%spublic %s %s(%s)", tabOut(), this.retType, this.name, join(this.args));

			if (this.body != null)
				str += String.format("\n%s%s", tabOut(), this.body);
			else
				str += "\n" + tabOut() + (this.isUsed ? "{ /* TODO FUNCTION BODY */ }" : "{ /* NOT USED */ }");

			return str;
		}
	}
	
	class CppClass extends CppDeclaration
	{
		public boolean isNested;
		public boolean isUnion;
		public String superclass;
		public List<String> additionalSupers = new ArrayList<String>(0);
		public List<CppDeclaration> declarations = new ArrayList<CppDeclaration>();
		
		@Override
		public String toString() 
		{
			String str = this.isUnion ? String.format("\n%s/* TODO union */", tabOut()) : "";
			
			str += String.format("\n%spublic%s class %s %s implements CppType<%s>",
					tabOut(),
					this.isNested ? " static" : "",
					this.name, 
					this.superclass != null ? "extends " + this.superclass : "",
					this.name);

			str += "\n" + tabOut() + '{';
			str += join(this.declarations);
			str += "\n" + tabOut() + '}';
			
			return str;
		}
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
	
	class MSimpleDecl extends CppDeclaration
	{
		public boolean isStatic;
		public String type;
		public MExpression initExpr;
		public boolean isPublic;
		
		@Override
		public String toString() 
		{
			// [public] type name [= expr;]
			return String.format("\n%s%s%s %s %s%s%s%s",
					tabOut(), this.isPublic ? " public" : "",
					this.isStatic ? " static" : "",
					this.type, this.name,
					this.initExpr != null ? " = " : "",
					this.initExpr != null ? this.initExpr.toString() : "",
					this.isPublic ? ";" : "");
		}
	}
}
