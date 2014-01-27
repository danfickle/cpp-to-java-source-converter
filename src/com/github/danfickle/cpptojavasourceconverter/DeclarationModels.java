package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MCompoundStmt;

class DeclarationModels 
{
	private int tabLevel;
	
	private String tabOut(int addTabs)
	{
		tabLevel += addTabs;
		String ret = tabOut();
		tabLevel -= addTabs;
		return ret;
	}
	
	private String tabOut()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < tabLevel; i++)
		{
			sb.append("    ");
		}

		return sb.toString();
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
			tabLevel++;
			  str += join(this.declarations);
			tabLevel--;
			str += "\n" + tabOut() + '}';
			
			return str;
		}
	}
	
	class CppEnumerator extends CppDeclaration
	{
		public MExpression value;
	}
	
	class CppEnum extends CppDeclaration
	{
		public boolean isNested;
		public List<CppEnumerator> enumerators = new ArrayList<CppEnumerator>();
		
		@Override
		public String toString() 
		{
			/*
			enum AnonEnum0
			{
				value1(0), 
				value2(1);

				final int val;

				AnonEnum0(final int enumVal)
				{
    				val = enumVal
				}

				static AnonEnum0 fromValue(final int enumVal)
				{
					switch (enumVal)
					{
					case (0): return value1;
					case (1): return value2;
					default: throw new ClassCastException();
					}
				}
			}
			*/
			
			StringBuilder sb = new StringBuilder();

			sb.append(String.format("\n%senum %s", tabOut(), this.name));
			sb.append(String.format("\n%s{", tabOut()));

			for (int i = 0; i < enumerators.size(); i++)
			{
				sb.append(String.format("\n%s%s(%s)", tabOut(1),
						enumerators.get(i).name, enumerators.get(i).value));
				
				if (i != enumerators.size() - 1)
					sb.append(", ");
				
			}
			
			sb.append(";\n");
			sb.append(String.format("\n%sfinal int val;", tabOut(1)));
			sb.append(String.format("\n\n%s%s(final int enumVal)", tabOut(1), this.name));
			sb.append(String.format("\n%s{", tabOut(1)));
			sb.append(String.format("\n%sval = enumVal", tabOut(2)));
			sb.append(String.format("\n%s}", tabOut(1)));
			
			sb.append(String.format("\n\n%sstatic %s fromValue(final int enumVal)", tabOut(1), this.name));
			sb.append(String.format("\n%s{", tabOut(1)));
			sb.append(String.format("\n%sswitch (enumVal)", tabOut(2)));
			sb.append(String.format("\n%s{", tabOut(2)));			
			
			for (int i = 0; i < enumerators.size(); i++)
			{
				sb.append(String.format("\n%scase (%s): return %s;", tabOut(2),
						enumerators.get(i).value, enumerators.get(i).name));
			}
			
			sb.append(String.format("\n%sdefault: throw new ClassCastException();", tabOut(2)));
			sb.append(String.format("\n%s}", tabOut(2)));
			sb.append(String.format("\n%s}", tabOut(1)));
			sb.append(String.format("\n%s}", tabOut()));
			
			return sb.toString();
		}
	}
	
	class CppBitfield extends CppDeclaration
	{
		public String qualified;
		public String type;
		public MExpression bits;
		
		@Override
		public String toString() 
		{
			/*
			int gettest_with_bit_field()
			{
    			return bitfields & 1
			}

			int settest_with_bit_field(final int val)
			{
    			bitfields &= ~1;
    			bitfields |= (val << 0) & 1;
    			return val;
			}

			int postInctest_with_bit_field()
			{
    			int ret = gettest_with_bit_field();
    			settest_with_bit_field(ret + 1)
			}

			int postDectest_with_bit_field()
			{
    			int ret = gettest_with_bit_field();
    			settest_with_bit_field(ret - 1)
			}
			*/
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("\n%s%s get%s()", tabOut(), this.type, this.name));
			sb.append(String.format("\n%s{", tabOut()));
			sb.append(String.format("\n%sreturn bitfields & 1", tabOut(1)));
			sb.append(String.format("\n%s}", tabOut()));
			
			sb.append(String.format("\n\n%s%s set%s(final %s val)", tabOut(), this.type, this.name, this.type));
			sb.append(String.format("\n%s{", tabOut()));
			sb.append(String.format("\n%sbitfields &= ~1;", tabOut(1)));
			sb.append(String.format("\n%sbitfields |= (val << 0) & 1;", tabOut(1)));
			sb.append(String.format("\n%sreturn val;", tabOut(1)));
			sb.append(String.format("\n%s}", tabOut()));
			
			sb.append(String.format("\n\n%s%s postInc%s()", tabOut(), this.type, this.name));
			sb.append(String.format("\n%s{", tabOut()));
			sb.append(String.format("\n%sint ret = get%s();", tabOut(1), this.name));
			sb.append(String.format("\n%sset%s(ret + 1)", tabOut(1), this.name));
			sb.append(String.format("\n%s}", tabOut()));			

			sb.append(String.format("\n\n%s%s postDec%s()", tabOut(), this.type, this.name));
			sb.append(String.format("\n%s{", tabOut()));
			sb.append(String.format("\n%sint ret = get%s();", tabOut(1), this.name));
			sb.append(String.format("\n%sset%s(ret - 1)", tabOut(1), this.name));
			sb.append(String.format("\n%s}", tabOut()));			

			return sb.toString();
		}
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
