package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;

class Traverser 
{
	/**
	 * Traverses the AST of the given translation unit
	 * and tries to convert the C++ abstract syntax tree to
	 * a Java AST.
	 */
	String traverse(IASTTranslationUnit translationUnit)
	{
		GlobalContext con = new GlobalContext();

		con.converter = new SourceConverter(con);
		con.exprEvaluator = new ExpressionEvaluator(con);
		con.stmtEvaluator = new StmtEvaluator(con);
		con.bitfieldMngr = new BitfieldManager(con);
		con.stackMngr = new StackManager(con);
		con.enumMngr = new EnumManager(con);
		con.funcMngr = new FunctionManager(con);
		con.currentFileName = translationUnit.getContainingFilename();

		MyLogger.ctx = con;

		//compositeMap.put("", new CompositeInfo(global));
		CppClass global = new CppClass();
		global.name = "Global";
		con.converter.addDeclaration(global);
	
		for (IASTProblem prob : translationUnit.getPreprocessorProblems())
		{
			MyLogger.logImportant(prob.getRawSignature());
		}

		try
		{
			for (IASTDeclaration declaration : translationUnit.getDeclarations())
			{
				MyLogger.log(declaration.getFileLocation().getEndingLineNumber() + ":" + declaration.getContainingFilename());
				con.converter.evalDeclaration(declaration);
			}
		}
		catch (Exception e)
		{
			MyLogger.logImportant(e.getMessage());
			e.printStackTrace();
		}
		con.converter.popDeclaration();
		StringBuilder output = new StringBuilder();
		STGroup group = new STGroupDir("/home/daniel/workspace/cpp-to-java-source-converter/templates");
		
		for (CppDeclaration decl : con.globalDeclarations)
		{
			ST test3 = group.getInstanceOf("declaration_tp");
			test3.add("decl", decl);
			output.append(test3.render());
		}
		
		// Replace 2 or more (greedy) newlines with 2 newlines.
		return output.toString().replaceAll("(\\n){2,}", "\n\n");
	}
}
