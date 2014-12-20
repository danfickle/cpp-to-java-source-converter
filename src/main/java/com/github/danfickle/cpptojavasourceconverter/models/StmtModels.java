package com.github.danfickle.cpptojavasourceconverter.models;

import java.util.ArrayList;
import java.util.List;

import com.github.danfickle.cpptojavasourceconverter.TranslationUnitContext;
import com.github.danfickle.cpptojavasourceconverter.models.DeclarationModels.MSimpleDecl;
import com.github.danfickle.cpptojavasourceconverter.models.ExpressionModels.MExpression;

public class StmtModels
{
	private TranslationUnitContext ctx;
	
	public StmtModels(TranslationUnitContext context)
	{
		ctx = context;
	}
	
	private String tabOut(int addTabs)
	{
		ctx.tabLevel += addTabs;
		String ret = ctx.declModels.tabOut();
		ctx.tabLevel -= addTabs;
		return ret;
	}
	
	private String tabOut()
	{
		return tabOut(0);
	}
	
	private String tabOut(Object child)
	{
		if (child == null ||
			child instanceof MCompoundStmt)
			return "";

		return tabOut(0);
	}
	
	private String stripNl(String str)
	{
		if (str.endsWith("\n"))
			return str.substring(0, str.length() - 1);
		return str;
	}
	
	public abstract static class MStmt {}
	
	public class MForStmt extends MStmt
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
				start += String.format("%s%s;\n", tabOut(), this.decl);

			start += tabOut();
			
			int temp = ctx.tabLevel;
			ctx.tabLevel = 0;
			  start += String.format("for (%s %s; %s)\n", tabOut(), 
					  stripNl(this.initializer.toString()), 
					  this.condition == null ? "" : this.condition,
					  this.updater == null ? "" : this.updater);

			ctx.tabLevel = temp;
			
			start += String.format("%s%s\n", tabOut(this.body), this.body);

			return start;
		}
	}
	
	public class MBreakStmt extends MStmt
	{
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n%sbreak;\n", tabOut(this.cleanup), this.cleanup == null ? "" : this.cleanup, tabOut());
		}
	}
	
	public class MContinueStmt extends MStmt
	{
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n%scontinue;\n", tabOut(this.cleanup), this.cleanup == null ? "" : this.cleanup, tabOut());
		}
	}
	
	public class MCaseStmt extends MStmt
	{
		public MExpression expr;
		
		@Override
		public String toString() 
		{
		  return String.format("%scase (%s):\n", tabOut(), this.expr);	
		}
	}
	
	public class MDefaultStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%sdefault:\n", tabOut());
		}
	}
	
	public class MEmptyStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%s/* Empty statement */;\n", tabOut());
		}
	}
	
	public class MCompoundStmt extends MStmt
	{
		public List<MStmt> statements = new ArrayList<MStmt>();
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("%s{\n", tabOut(0)));

			ctx.tabLevel++;
			for (MStmt stmt : this.statements)
			{
				sb.append(String.format("%s\n", stmt));
			}
			
			if (this.cleanup != null)
				sb.append(String.format("%s\n", this.cleanup));

			ctx.tabLevel--;
			
			sb.append(String.format("%s}\n", tabOut(0)));

			return sb.toString();
		}
	}
	
	public class MDeclarationStmt extends MStmt
	{
		public MSimpleDecl simple;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n", tabOut(), this.simple);
		}
	}

	public class MDoStmt extends MStmt
	{
		public MExpression expr;
		public MStmt body;
		
		@Override
		public String toString() 
		{
			return String.format(
					"%sdo\n" +
			        "%s%s\n" +
					"%swhile (%s);\n",
					tabOut(), tabOut(this.body), this.body, tabOut(), this.expr);
		}
	}
	
	public class MExprStmt extends MStmt
	{
		public MExpression expr;
		
		@Override
		public String toString()
		{
			return String.format("%s%s;\n", tabOut(), this.expr);
		}
	}

	public class MIfStmt extends MStmt
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
				start += String.format("%s%s;\n", tabOut(this.decl), this.decl);
			
			start += String.format("%sif (%s)\n" +
			                       "%s%s\n", tabOut(), this.condition, tabOut(this.body), this.body);
			
			if (this.elseBody != null)
				start += String.format("%selse %s\n", tabOut(this.elseBody), this.elseBody);

			return start;
		}
	}

	public class MReturnStmt extends MStmt
	{
		public MExpression expr;
		public MStmt cleanup;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s\n%sreturn %s;\n", tabOut(this.cleanup), this.cleanup == null ? "" : this.cleanup, tabOut(), this.expr);
		}
	}
	
	public class MWhileStmt extends MStmt
	{
		public MStmt body;
		public MExpression expr;
		public MSimpleDecl decl;
		
		@Override
		public String toString() 
		{
			String start = "";
			
			if (this.decl != null)
				start += String.format("%s%s\n", tabOut(this.decl), this.decl);
			
			start += String.format("%swhile (%s)\n", tabOut(), this.expr);
			start += String.format("%s%s\n", tabOut(this.body), this.body);
			
			return start;
		}
	}
	
	public class MSwitchStmt extends MStmt
	{
		public MStmt body;
		public MExpression expr;
		public MSimpleDecl decl;
		
		@Override
		public String toString() 
		{
			String start = "";
			
			if (this.decl != null)
				start += String.format("%s%s;\n", tabOut(this.decl), this.decl);
		
			start += String.format("%sswitch (%s)\n", tabOut(), this.expr);
			start += String.format("%s%s\n", tabOut(this.body), this.body);

			return start;
		}
	}
	
	public class MGotoStmt extends MStmt
	{
		public String lbl;
		@Override
		public String toString() 
		{
			return String.format("%s/* TODO goto %s */\n", tabOut(), this.lbl);
		}
	}

	public class MProblemStmt extends MStmt
	{
		public String problem;
		
		@Override
		public String toString() 
		{
			  return String.format("%s/* TODO: problem %s */\n", tabOut(), this.problem);
		}
	}
	
	public class MLabelStmt extends MStmt
	{
		public String lbl;
		public MStmt body;
		
		@Override
		public String toString() 
		{
			return String.format("%s/* TODO label %s */\n%s%s\n", tabOut(), this.lbl, tabOut(), this.body);
		}
	}
	
	public class MSuperStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%ssuper();\n", tabOut());
		}
	}
	
	public class MSuperDtorStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%ssuper.destuct();\n", tabOut());
		}
	}
	
	public class MSuperAssignStmt extends MStmt
	{
		@Override
		public String toString() 
		{
			return String.format("%ssuper.opAssign(right);\n", tabOut());
		}
	}
}
