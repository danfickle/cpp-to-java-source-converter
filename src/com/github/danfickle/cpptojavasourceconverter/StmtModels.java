package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.MSimpleDecl;

public class StmtModels
{
	int tabLevel;
	
	String tabOut()
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
		public boolean isFor = true;
		
		public MStmt initializer;
		public MExpression condition;
		public MExpression updater;
		public MSimpleDecl decl;
		
		public MStmt body;
	}
	
	class MBreakStmt extends MStmt
	{
		public boolean isBreak = true;
		
		public MStmt cleanup;
	}
	
	class MContinueStmt extends MStmt
	{
		public boolean isContinue = true;
		
		public MStmt cleanup;
	}
	
	class MCaseStmt extends MStmt
	{
		public boolean isCase = true;
		
		public MExpression expr;
	}
	
	class MDefaultStmt extends MStmt
	{
		public boolean isDefault = true;
	}
	
	class MEmptyStmt extends MStmt
	{
		public boolean isEmpty = true;
	}
	
	class MCompoundStmt extends MStmt
	{
		public boolean isCompound = true;
		
		public List<MStmt> statements = new ArrayList<MStmt>();
		public MStmt cleanup;
	}
	
	class MDeclarationStmt extends MStmt
	{
		public boolean isDeclStmt = true;
		
		public MSimpleDecl simple;
	}

	class MDoStmt extends MStmt
	{
		public boolean isDo = true;
		
		public MExpression expr;
		public MStmt body;
	}
	
	class MExprStmt extends MStmt
	{
		public boolean isExprStmt = true;
		
		public MExpression expr;
	}

	class MIfStmt extends MStmt
	{
		public boolean isIf = true;
		
		public MStmt body;
		public MExpression condition;
		public MStmt elseBody;
		public MSimpleDecl decl;
	}

	class MReturnStmt extends MStmt
	{
		public boolean isReturn = true;
		
		public MExpression expr;
		public MStmt cleanup;
	}
	
	class MWhileStmt extends MStmt
	{
		public boolean isWhile = true;
		
		public MStmt body;
		public MExpression expr;
		public MSimpleDecl decl;
	}
	
	class MSwitchStmt extends MStmt
	{
		public boolean isSwitch = true;
		
		public MStmt body;
		public MExpression expr;
		public MSimpleDecl decl;
	}
	
	class MGotoStmt extends MStmt
	{
		public boolean isGoto = true;
		
		public String lbl;
	}

	class MProblemStmt extends MStmt
	{
		public boolean isProblemStmt = true;
		
		public String problem;
	}
	
	class MLabelStmt extends MStmt
	{
		public boolean isLabel = true;
		
		public String lbl;
		public MStmt body;
	}
	
	class MSuperStmt extends MStmt
	{
		public boolean isSuperStmt = true;
	}
	
	class MSuperDtorStmt extends MStmt
	{
		public boolean isSuperDtorStmt = true;
	}
	
	class MSuperAssignStmt extends MStmt
	{
		public boolean isSuperAssignStmt = true;
	}
}
