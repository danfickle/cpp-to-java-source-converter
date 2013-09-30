package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IType;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppDeclaration;

class GlobalContext 
{
	SourceConverter converter;
	StackManager stackMngr;
	ExpressionEvaluator exprEvaluator;
	StmtEvaluator stmtEvaluator;
	BitfieldManager bitfieldMngr;
	EnumManager enumMngr;
	FunctionManager funcMngr;
	SpecialGenerator specialGenerator;
	
	List<CppDeclaration> globalDeclarations = new ArrayList<CppDeclaration>();
	
	String currentFileName;
	IType currentReturnType;
}
