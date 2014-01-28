package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.IType;

class TranslationUnitContext 
{
	SourceConverter     converter;
	StackManager        stackMngr;
	ExpressionEvaluator exprEvaluator;
	StmtEvaluator       stmtEvaluator;
	BitfieldManager     bitfieldMngr;
	EnumManager         enumMngr;
	FunctionManager     funcMngr;
	SpecialGenerator    specialGenerator;
	InitializationManager initMngr;
	TypeManager           typeMngr;
	GlobalCtx             global;
	StmtModels            stmtModels;
	DeclarationModels     declModels;
	int                   tabLevel;
	
	String currentFileName;
	IType currentReturnType;
}
