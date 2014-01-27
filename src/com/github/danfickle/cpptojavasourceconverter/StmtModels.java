package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.MSimpleDecl;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;

public class StmtModels
{
	int tabLevel;
	
	private String incTabLevel()
	{
		tabLevel++;
		return "";
	}

	private String decTabLevel()
	{
		tabLevel--;
		return "";
	}
	
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
		case 3:
			return "            ";
		case 4:
			return "                ";
		default:
		{
			StringBuilder sb = new StringBuilder("                ");

			for (int i = 5; i <= tabLevel; i++)
			{
				sb.append("    ");
			}

			return sb.toString();
		}
		}
	}
	
	
	abstract static class MStmt {}
	
	class MForStmt extends MStmt
	{
		public MStmt initializer;
		public MExpression condition;
		public MExpression updater;
		public MSimpleDecl decl;
		
		public MStmt body;
		
		@Override
		public String toString() 
		{
			String start = "";
			
			if (this.decl != null)
				start += String.format("%s%s\n", tabOut(), this.decl);

			start += String.format("%sfor (%s %s; %s)\n", tabOut(), this.initializer, this.condition, this.updater);
			start += String.format("%s%s%s%s\n", incTabLevel(), tabOut(), this.body, decTabLevel());

			return start;
		}
	}
	
	class MBreakStmt extends MStmt
	{
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n%sbreak;\n", tabOut(), this.cleanup, tabOut());
		}
	}
	
	class MContinueStmt extends MStmt
	{
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n%scontinue;\n", tabOut(), this.cleanup, tabOut());
		}
	}
	
	class MCaseStmt extends MStmt
	{
		public MExpression expr;
		
		@Override
		public String toString() 
		{
		  return String.format("%scase (%s):\n", tabOut(), this.expr);	
		}
	}
	
	class MDefaultStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%sdefault:\n", tabOut());
		}
	}
	
	class MEmptyStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%s/* Empty statement */;\n", tabOut());
		}
	}
	
	class MCompoundStmt extends MStmt
	{
		public List<MStmt> statements = new ArrayList<MStmt>();
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("%s%s%s{\n", decTabLevel(), tabOut(), incTabLevel()));
			
			for (MStmt stmt : this.statements)
			{
				sb.append(String.format("%s%s\n", tabOut(), stmt));
			}
			
			sb.append(String.format("%s%s\n", tabOut(), this.cleanup == null ? "" : this.cleanup));
			sb.append(String.format("%s%s%s}\n", decTabLevel(), tabOut(), incTabLevel()));

			return sb.toString();
		}
	}
	
	class MDeclarationStmt extends MStmt
	{
		public MSimpleDecl simple;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n", tabOut(), this.simple);
		}
	}

	class MDoStmt extends MStmt
	{
		public MExpression expr;
		public MStmt body;
		
		@Override
		public String toString() 
		{
			return String.format(
					"%sdo\n" +
			        "%s%s%s%s\n" +
					"%swhile (%s);\n",
					tabOut(), incTabLevel(), tabOut(), this.body, decTabLevel(), tabOut(), this.expr);
		}
	}
	
	class MExprStmt extends MStmt
	{
		public MExpression expr;
		
		@Override
		public String toString()
		{
			return String.format("%s%s;\n", tabOut(), this.expr);
		}
	}

	class MIfStmt extends MStmt
	{
		public MStmt body;
		public MExpression condition;
		public MStmt elseBody;
		public MSimpleDecl decl;
		
		@Override
		public String toString() 
		{
			String start = "";
			
			if (this.decl != null)
				start += String.format("%s%s;\n", tabOut(), this.decl);
			
			start += String.format("%sif (%s)\n" +
			                       "%s%s\n", tabOut(), this.condition, tabOut(), this.body);
			
			if (this.elseBody != null)
				start += String.format("%selse %s\n", tabOut(), this.elseBody);

			return start;
		}
	}

	class MReturnStmt extends MStmt
	{
		public MExpression expr;
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n%sreturn %s;\n", tabOut(), this.cleanup == null ? "" : this.cleanup, tabOut(), this.expr);
		}
	}
	
	class MWhileStmt extends MStmt
	{
		public MStmt body;
		public MExpression expr;
		public MSimpleDecl decl;
		
		@Override
		public String toString() 
		{
			String start = "";
			
			if (this.decl != null)
				start += String.format("%s%s;\n", tabOut(), this.decl);
			
			start += String.format("%swhile (%s)\n", tabOut(), this.expr);
			start += String.format("%s%s\n", tabOut(), this.body);
			
			return start;
		}
	}
	
	class MSwitchStmt extends MStmt
	{
		public MStmt body;
		public MExpression expr;
		public MSimpleDecl decl;
		
		@Override
		public String toString() 
		{
			String start = "";
			
			if (this.decl != null)
				start += String.format("%s%s;\n", tabOut(), this.decl);
		
			start += String.format("%sswitch (%s)\n", tabOut(), this.expr);
			start += String.format("%s%s\n", tabOut(), this.body);

			return start;
		}
	}
	
	class MGotoStmt extends MStmt
	{
		public String lbl;
		@Override
		public String toString() 
		{
			return String.format("%s/* TODO goto %s */\n", tabOut(), this.lbl);
		}
	}

	class MProblemStmt extends MStmt
	{
		public String problem;
		
		@Override
		public String toString() 
		{
			  return String.format("%s/* TODO: problem %s */\n", tabOut(), this.problem);
		}
	}
	
	class MLabelStmt extends MStmt
	{
		public String lbl;
		public MStmt body;
		
		@Override
		public String toString() 
		{
			return String.format("%s/* TODO label %s */\n%s%s\n", tabOut(), this.lbl, tabOut(), this.body);
		}
	}
	
	class MSuperStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%ssuper();\n", tabOut());
		}
	}
	
	class MSuperDtorStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%ssuper.destuct();\n", tabOut());
		}
	}
	
	class MSuperAssignStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%ssuper.opAssign(right);\n", tabOut());
		}
	}
}
