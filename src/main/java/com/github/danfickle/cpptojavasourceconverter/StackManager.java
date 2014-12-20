package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import com.github.danfickle.cpptojavasourceconverter.models.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.models.StmtModels.MStmt;

/**
 * The stack manager is in charge of keeping track of the stack of 
 * objects in C++ so we can implement it in in Java. This will include
 * adding items to the stack and destructing items at the appropriate
 * time.
 */
class StackManager
{
	private Deque<ScopeVar> localVariableStack = new ArrayDeque<ScopeVar>();
	private Integer localVariableMaxId;
	private int nextVariableId;
	
	private static class ScopeVar
	{
		ScopeVar(int idi, boolean isloop, boolean isswitch)
		{
			id = idi;
			isLoop = isloop;
			isSwitch = isswitch;
		}

		final boolean isLoop;
		final boolean isSwitch;
		final int id;
		int cnt;
	}
	
	private final TranslationUnitContext ctx;
	
	StackManager(TranslationUnitContext con) {
		ctx = con;
	}

	/**
	 * This creates a Java expression that adds an object to the
	 * local (to the function) stack.
	 */
	MExpression createAddItemCall(MExpression item)
	{
		MAddItemCall add = new MAddItemCall();
		add.operand = item;
		add.nextFreeStackId = nextVariableId;
		getAndIncrementLocalVariableId();
		return add;
	}

	/**
	 * This is used to find where to cleanup to when continue
	 * statement is used.
	 */
	Integer findLastLoopId()
	{
		int cnt = 0;
		for (ScopeVar sv : localVariableStack)
		{
			cnt += sv.cnt;
			if (sv.isLoop)
				return cnt == 0 ? null : sv.id;
		}
		
		return null;
	}
	
	/**
	 * This is used to find where we should cleanup to when
	 * break statement is used.
	 */
	Integer findLastSwitchOrLoopId()
	{
		int cnt = 0;
		for (ScopeVar sv : localVariableStack)
		{
			cnt += sv.cnt;
			if (sv.isSwitch || sv.isLoop)
				return cnt == 0 ? null : sv.id;
		}
		return null;
	}
	
	void reset()
	{
		nextVariableId = 0;
		localVariableMaxId = null;
	}
	
	int getLocalVariableId()
	{
		return nextVariableId;
	}

	Integer getMaxLocalVariableId()
	{
		return localVariableMaxId;
	}
	
	/**
	 * This method keeps track of the variable numbers.
	 */
	int getAndIncrementLocalVariableId()
	{
		nextVariableId++;

		if (localVariableStack.peek() != null)
			localVariableStack.peek().cnt++;
		
		if (localVariableMaxId == null || nextVariableId > localVariableMaxId)
			localVariableMaxId = nextVariableId;
		
		return nextVariableId;
	}
	
	MStmt createCleanupCall(int until)
	{
		MStmt fcall = ModelCreation.createMethodCall(ctx, "StackHelper", "cleanup", 
				ModelCreation.createLiteral("null"),
				ModelCreation.createLiteral("__stack"),
				ModelCreation.createLiteral(String.valueOf(until)));
		
		return fcall;
	}
	
	void startNewCompoundStmt(boolean isLoop, boolean isSwitch)
	{
		localVariableStack.push(
				new ScopeVar(nextVariableId,
						isLoop,
						isSwitch));
	}
	
	Integer endCompoundStmt()
	{
		int cnt = localVariableStack.peek().cnt;
		nextVariableId = localVariableStack.peek().id;
		localVariableStack.pop();
		return cnt == 0 ? null : nextVariableId;
	}

	MExpression wrapCleanupCall(MExpression expr) 
	{
		MFunctionCallExpression funcCall = new MFunctionCallExpression();
		funcCall.name = ModelCreation.createLiteral("StackHelper.cleanup");
		funcCall.args = Arrays.asList(expr,
				ModelCreation.createLiteral("__stack"),
				ModelCreation.createLiteral("0"));
		return funcCall;
	}
}
