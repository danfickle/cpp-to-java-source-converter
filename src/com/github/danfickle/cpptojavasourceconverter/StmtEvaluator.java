package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;

import com.github.danfickle.cpptojavasourceconverter.InitializationManager.InitType;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.models.DeclarationModels.MSimpleDecl;
import com.github.danfickle.cpptojavasourceconverter.models.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.models.StmtModels.*;

class StmtEvaluator 
{
	private TranslationUnitContext ctx;
	
	StmtEvaluator(TranslationUnitContext con) {
		ctx = con;
	}

	MStmt eval1Stmt(IASTStatement stmt) throws DOMException
	{
		List<MStmt> ret = evalStmt(stmt);
		assert(ret.size() == 1);
		return ret.get(0);
	}
	
	/**
	 * Attempts to convert the given C++ statement to one or more Java statements.
	 */
	List<MStmt> evalStmt(IASTStatement statement) throws DOMException
	{
		List<MStmt> stmts = new ArrayList<MStmt>();
		
		if (statement instanceof IASTBreakStatement)
		{
			MyLogger.log("break");

			Integer temp = ctx.stackMngr.findLastSwitchOrLoopId();

			MBreakStmt brk = ctx.stmtModels.new MBreakStmt();
			stmts.add(brk);
			
			if (temp != null) // Cleanup back to the closest loop or switch...
				brk.cleanup = ctx.stackMngr.createCleanupCall(temp);
		}
		else if (statement instanceof IASTCaseStatement)
		{
			MyLogger.log("case");
			
			IASTCaseStatement caseStatement = (IASTCaseStatement) statement;

			MCaseStmt cs = ctx.stmtModels.new MCaseStmt();
			stmts.add(cs);
			
			cs.expr = ctx.exprEvaluator.eval1Expr(caseStatement.getExpression());
		}
		else if (statement instanceof IASTContinueStatement)
		{
			MyLogger.log("continue");

			Integer temp = ctx.stackMngr.findLastLoopId();

			MContinueStmt con = ctx.stmtModels.new MContinueStmt();
			stmts.add(con);
			
			if (temp != null) // Cleanup back to the closest loop...
				con.cleanup = ctx.stackMngr.createCleanupCall(temp);
		}
		else if (statement instanceof IASTDefaultStatement)
		{
			MyLogger.log("default");

			MDefaultStmt def = ctx.stmtModels.new MDefaultStmt();
			stmts.add(def);
		}
		else if (statement instanceof IASTGotoStatement)
		{
			MyLogger.log("goto");

			IASTGotoStatement gotoStatement = (IASTGotoStatement) statement;

			MGotoStmt go = ctx.stmtModels.new MGotoStmt();
			stmts.add(go);
			
			go.lbl = gotoStatement.getName().toString();
		}
		else if (statement instanceof IASTNullStatement)
		{
			MyLogger.log("Empty statement");
			
			MEmptyStmt empty = ctx.stmtModels.new MEmptyStmt();
			stmts.add(empty);
		}
		else if (statement instanceof IASTProblemStatement)
		{
			IASTProblemStatement probStatement = (IASTProblemStatement) statement;

			MyLogger.log("problem: " + probStatement.getProblem().getMessageWithLocation());

			MProblemStmt prob = ctx.stmtModels.new MProblemStmt();
			stmts.add(prob);
			
			prob.problem = probStatement.getProblem().getMessageWithLocation();
		}
		else if (statement instanceof IASTCompoundStatement)
		{
			IASTCompoundStatement compoundStatement = (IASTCompoundStatement)statement;
			MyLogger.log("Compound");
			ctx.stackMngr.startNewCompoundStmt(false, false);
//			ctx.stackMngr.startNewCompoundStmt(
//					currentLocation == BodyLocation.DO ||
//					currentLocation == BodyLocation.WHILE ||
//					currentLocation == BodyLocation.FOR,
//					currentLocation == BodyLocation.SWITCH);

			MCompoundStmt compound = ctx.stmtModels.new MCompoundStmt();
			stmts.add(compound);

			for (IASTStatement s : compoundStatement.getStatements())
				compound.statements.addAll(evalStmt(s));
			
			Integer idToCleanTo = ctx.stackMngr.endCompoundStmt();
			
			if (idToCleanTo != null &&
			    !compound.statements.isEmpty() &&
			    !isTerminatingStatement(compound.statements.get(compound.statements.size() - 1)))
			{
				compound.cleanup = ctx.stackMngr.createCleanupCall(idToCleanTo);
			}
		}
		else if (statement instanceof IASTDeclarationStatement)
		{
			IASTDeclarationStatement declarationStatement = (IASTDeclarationStatement)statement;
			MyLogger.log("Declaration");

			List<String> types = ctx.converter.evaluateDeclarationReturnTypes(declarationStatement.getDeclaration());
			List<String> names = ctx.converter.evaluateDeclarationReturnNames(declarationStatement.getDeclaration());
			List<MExpression> exprs = ctx.converter.evaluateDeclarationReturnInitializers((IASTSimpleDeclaration) declarationStatement.getDeclaration(), InitType.WRAPPED);
			
			for (int i = 0; i < types.size(); i++)
			{
				MSimpleDecl simple = ctx.declModels.new MSimpleDecl();
				simple.type = types.get(i);
				simple.name = names.get(i); 
				simple.initExpr = exprs.get(i);

				MDeclarationStmt stmt = ctx.stmtModels.new MDeclarationStmt();
				stmt.simple = simple;
			
				stmts.add(stmt);
			}
		}
		else if (statement instanceof IASTDoStatement)
		{
			MyLogger.log("Do");

			IASTDoStatement doStatement = (IASTDoStatement)statement;

			MDoStmt dos = ctx.stmtModels.new MDoStmt();
			stmts.add(dos);

			dos.body = surround(evalStmt(doStatement.getBody()));
			dos.expr = ctx.exprEvaluator.eval1Expr(doStatement.getCondition());
			dos.expr = ExpressionHelpers.makeExpressionBoolean(dos.expr, doStatement.getCondition());
		}
		else if (statement instanceof IASTExpressionStatement)
		{
			MyLogger.log("Expression");

			IASTExpressionStatement expressionStatement = (IASTExpressionStatement)statement;

			MExprStmt exprStmt = ctx.stmtModels.new MExprStmt();
			stmts.add(exprStmt);
			exprStmt.expr = ctx.exprEvaluator.eval1Expr(expressionStatement.getExpression());
		}
		else if (statement instanceof IASTForStatement)
		{
			MyLogger.log("For");

			IASTForStatement forStatement = (IASTForStatement)statement;

			MForStmt fs = ctx.stmtModels.new MForStmt();
			stmts.add(fs);
			
			if (forStatement.getInitializerStatement() != null)
				fs.initializer = eval1Stmt(forStatement.getInitializerStatement());
			
			if (forStatement.getConditionExpression() != null)
			{
				fs.condition = ctx.exprEvaluator.eval1Expr(forStatement.getConditionExpression());
				fs.condition = ExpressionHelpers.makeExpressionBoolean(fs.condition, forStatement.getConditionExpression());
			}

			if (forStatement.getIterationExpression() != null)
				fs.updater = ctx.exprEvaluator.eval1Expr(forStatement.getIterationExpression());
			
			fs.body = surround(evalStmt(forStatement.getBody()));
			
			if (forStatement instanceof ICPPASTForStatement &&
				((ICPPASTForStatement) forStatement).getConditionDeclaration() != null)
			{
				// I really doubt any C++ programmer puts a declaration in the condition space
				// of a for loop but the language seems to allow it.
				// eg. for (int a = 1; int b = 3; a++)
				IType tp = ctx.converter.eval1DeclReturnCppType(((ICPPASTForStatement) forStatement).getConditionDeclaration());
				
				fs.decl = ctx.converter.eval1Decl(((ICPPASTForStatement) forStatement).getConditionDeclaration(), InitType.RAW);
				fs.condition = ctx.converter.makeInfixFromDecl(fs.decl.name, fs.decl.initExpr, tp, true);
				fs.decl.initExpr = ctx.exprEvaluator.makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof IASTIfStatement)
		{
			MyLogger.log("If");

			IASTIfStatement ifStatement = (IASTIfStatement)statement;

			MIfStmt ifs = ctx.stmtModels.new MIfStmt();
			stmts.add(ifs);

			if (ifStatement.getConditionExpression() != null)
			{
				ifs.condition = ctx.exprEvaluator.eval1Expr(ifStatement.getConditionExpression());
				ifs.condition = ExpressionHelpers.makeExpressionBoolean(ifs.condition, ifStatement.getConditionExpression());
			}
			
			ifs.body = surround(evalStmt(ifStatement.getThenClause()));
			
			if (ifStatement.getElseClause() != null)
				ifs.elseBody = eval1Stmt(ifStatement.getElseClause());
			
			if (ifStatement instanceof ICPPASTIfStatement &&
				((ICPPASTIfStatement) ifStatement).getConditionDeclaration() != null)
			{
				IType tp = ctx.converter.eval1DeclReturnCppType(((ICPPASTIfStatement) ifStatement).getConditionDeclaration());

				ifs.decl = ctx.converter.eval1Decl(((ICPPASTIfStatement) ifStatement).getConditionDeclaration(), InitType.RAW);
				ifs.condition = ctx.converter.makeInfixFromDecl(ifs.decl.name, ifs.decl.initExpr, tp, true);
				ifs.decl.initExpr = ctx.exprEvaluator.makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof IASTLabelStatement)
		{
			MyLogger.log("Label");
			
			IASTLabelStatement labelStatement = (IASTLabelStatement)statement;

			MLabelStmt lbl = ctx.stmtModels.new MLabelStmt();
			stmts.add(lbl);
			
			lbl.lbl = labelStatement.getName().toString();
			lbl.body = eval1Stmt(labelStatement.getNestedStatement());
		}
		else if (statement instanceof IASTReturnStatement)
		{
			MyLogger.log("return");

			IASTReturnStatement returnStatement = (IASTReturnStatement)statement;

			MReturnStmt retu = ctx.stmtModels.new MReturnStmt();
			stmts.add(retu);

			retu.expr = ctx.exprEvaluator.wrapIfNeeded(returnStatement.getReturnValue(), ctx.currentReturnType);

			// Only call cleanup if we have something on the stack.
			if (ctx.stackMngr.getLocalVariableId() != 0 &&
				TypeManager.isOneOf(ctx.currentReturnType, TypeEnum.VOID))
			{
				retu.cleanup = ctx.stackMngr.createCleanupCall(0);
			}
			else if (ctx.stackMngr.getLocalVariableId() != 0)
			{
				retu.expr = ctx.stackMngr.wrapCleanupCall(retu.expr);
			}
		}
		else if (statement instanceof IASTSwitchStatement)
		{
			MyLogger.log("Switch");
			
			IASTSwitchStatement switchStatement = (IASTSwitchStatement)statement;

			MSwitchStmt swi = ctx.stmtModels.new MSwitchStmt();
			stmts.add(swi);
			
			swi.body = surround(evalStmt(switchStatement.getBody()));

			if (switchStatement.getControllerExpression() != null)
			{
				swi.expr = ctx.exprEvaluator.eval1Expr(switchStatement.getControllerExpression());
			}
			
			if (switchStatement instanceof ICPPASTSwitchStatement &&
			    ((ICPPASTSwitchStatement) switchStatement).getControllerDeclaration() != null)
			{
				IType tp = ctx.converter.eval1DeclReturnCppType(((ICPPASTSwitchStatement) switchStatement).getControllerDeclaration());

				swi.decl = ctx.converter.eval1Decl(((ICPPASTSwitchStatement) switchStatement).getControllerDeclaration(), InitType.RAW);
				swi.expr = ctx.converter.makeInfixFromDecl(swi.decl.name, swi.decl.initExpr, tp, false);
				swi.decl.initExpr = ctx.exprEvaluator.makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof IASTWhileStatement)
		{
			MyLogger.log("while");
			
			IASTWhileStatement whileStatement = (IASTWhileStatement)statement;

			MWhileStmt whi = ctx.stmtModels.new MWhileStmt();
			stmts.add(whi);
			
			whi.body = surround(evalStmt(whileStatement.getBody()));

			if (whileStatement.getCondition() != null)
			{
				whi.expr = ctx.exprEvaluator.eval1Expr(whileStatement.getCondition());
				whi.expr = ExpressionHelpers.makeExpressionBoolean(whi.expr, whileStatement.getCondition());
			}
			
			if (whileStatement instanceof ICPPASTWhileStatement &&
				((ICPPASTWhileStatement) whileStatement).getConditionDeclaration() != null)
			{
				IType tp = ctx.converter.eval1DeclReturnCppType(((ICPPASTWhileStatement) whileStatement).getConditionDeclaration());
				
				whi.decl = ctx.converter.eval1Decl(((ICPPASTWhileStatement) whileStatement).getConditionDeclaration(), InitType.RAW);
				whi.expr = ctx.converter.makeInfixFromDecl(whi.decl.name, whi.decl.initExpr, tp, true);
				whi.decl.initExpr = ctx.exprEvaluator.makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof ICPPASTTryBlockStatement)
		{
//			ICPPASTTryBlockStatement tryBlockStatement = (ICPPASTTryBlockStatement)statement;
//			print("Try");
//
//			TryStatement trys = ast.newTryStatement();
//
//			trys.setBody(surround(eval1Stmt(tryBlockStatement.getTryBody())));
//
//			for (ICPPASTCatchHandler catchHandler : tryBlockStatement.getCatchHandlers())
//				trys.catchClauses().add(evaluateCatchClause(catchHandler));
//
//			ret.add(trys);
		}
		else if (statement != null)
		{
			MyLogger.logImportant(statement.getClass().getCanonicalName());
		}
		
		if (stmts.isEmpty())
			stmts.add(ctx.stmtModels.new MEmptyStmt()); // TODO
		return stmts;
	}
	
	private boolean isTerminatingStatement(MStmt mStmt)
	{
		if (mStmt instanceof MBreakStmt ||
			mStmt instanceof MReturnStmt ||
			mStmt instanceof MContinueStmt)
			return true;
		
		return false;
	}

	private MCompoundStmt surround(List<MStmt> stmts) throws DOMException
	{
		if (stmts.size() == 1 && stmts.get(0) instanceof MCompoundStmt)
			return (MCompoundStmt) stmts.get(0);
		
		MCompoundStmt compound = ctx.stmtModels.new MCompoundStmt();
		compound.statements.addAll(stmts);
		return compound;
	}
}
