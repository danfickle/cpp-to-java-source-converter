package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.MSimpleDecl;

public class StmtModels
{
	abstract static class MStmt {}
	
	static class MForStmt extends MStmt
	{
		public boolean isFor = true;
		
		public MStmt initializer;
		public MExpression condition;
		public MExpression updater;
		public MSimpleDecl decl;
		
		public MStmt body;
	}
	
	static class MBreakStmt extends MStmt
	{
		public boolean isBreak = true;
		
		public MStmt cleanup;
	}
	
	static class MContinueStmt extends MStmt
	{
		public boolean isContinue = true;
		
		public MStmt cleanup;
	}
	
	static class MCaseStmt extends MStmt
	{
		public boolean isCase = true;
		
		public MExpression expr;
	}
	
	static class MDefaultStmt extends MStmt
	{
		public boolean isDefault = true;
	}
	
	static class MEmptyStmt extends MStmt
	{
		public boolean isEmpty = true;
	}
	
	static class MCompoundStmt extends MStmt
	{
		public boolean isCompound = true;
		
		public List<MStmt> statements = new ArrayList<MStmt>();
		public MStmt cleanup;
	}
	
	static class MDeclarationStmt extends MStmt
	{
		public boolean isDeclStmt = true;
		
		public MSimpleDecl simple;
	}

	static class MDoStmt extends MStmt
	{
		public boolean isDo = true;
		
		public MExpression expr;
		public MStmt body;
	}
	
	static class MExprStmt extends MStmt
	{
		public boolean isExprStmt = true;
		
		public MExpression expr;
	}

	static class MIfStmt extends MStmt
	{
		public boolean isIf = true;
		
		public MStmt body;
		public MExpression condition;
		public MStmt elseBody;
		public MSimpleDecl decl;
	}

	static class MReturnStmt extends MStmt
	{
		public boolean isReturn = true;
		
		public MExpression expr;
		public MStmt cleanup;
	}
	
	static class MWhileStmt extends MStmt
	{
		public boolean isWhile = true;
		
		public MStmt body;
		public MExpression expr;
		public MSimpleDecl decl;
	}
	
	static class MSwitchStmt extends MStmt
	{
		public boolean isSwitch = true;
		
		public MStmt body;
		public MExpression expr;
		public IASTDeclaration decl;
	}
	
	static class MGotoStmt extends MStmt
	{
		public boolean isGoto = true;
		
		public String lbl;
	}

	static class MProblemStmt extends MStmt
	{
		public boolean isProblemStmt = true;
		
		public String problem;
	}
	
	static class MLabelStmt extends MStmt
	{
		public boolean isLabel = true;
		
		public String lbl;
		public MStmt body;
	}
	
	static class MSuperStmt extends MStmt
	{
		public boolean isSuperStmt = true;
	}
	
	static class MSuperDtorStmt extends MStmt
	{
		public boolean isSuperDtorStmt = true;
	}
	
	static class MSuperAssignStmt extends MStmt
	{
		public boolean isSuperAssignStmt = true;
	}
}
