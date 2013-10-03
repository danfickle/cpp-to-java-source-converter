package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IType;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppDeclaration;

class GlobalContext 
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
	TypeHelpers           typeMngr;
	
	List<CppDeclaration> globalDeclarations = new ArrayList<CppDeclaration>();
	Map<IType, String>   anonTypes = new HashMap<IType, String>();
	int                  anonClassCount = 0;
	
	String currentFileName;
	IType currentReturnType;
}
