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
	String traverse(IASTTranslationUnit translationUnit, GlobalCtx global2)
	{
		TranslationUnitContext con = new TranslationUnitContext();

		con.global = global2;
		con.converter = new SourceConverter(con);
		con.exprEvaluator = new ExpressionEvaluator(con);
		con.stmtEvaluator = new StmtEvaluator(con);
		con.bitfieldMngr = new BitfieldManager(con);
		con.stackMngr = new StackManager(con);
		con.enumMngr = new EnumManager(con);
		con.funcMngr = new FunctionManager(con);
		con.specialGenerator = new SpecialGenerator(con);
		con.initMngr = new InitializationManager(con);
		con.typeMngr = new TypeManager(con);
		con.currentFileName = translationUnit.getContainingFilename();

		MyLogger.ctx = con;

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
		StringBuilder output = new StringBuilder();
		STGroup group = new STGroupDir("/home/daniel/workspace/cpp-to-java-source-converter/templates");
		
		for (CppDeclaration decl : con.global.decls)
		{
			if (decl.parent == null)
			{
				ST test3 = group.getInstanceOf("declaration_tp");
				test3.add("decl", decl);
				output.append(test3.render());
			}
		}
		
		for (CppDeclaration decl : con.global.fileClasses.values())
		{
			ST test3 = group.getInstanceOf("declaration_tp");
			test3.add("decl", decl);
			output.append(test3.render());
		}
		
		// TODO: Match up declarations with files.
		con.global.decls.clear();
		con.global.fileClasses.clear();
		
		// Replace 2 or more (greedy) newlines with 2 newlines.
		return output.toString().replaceAll("(\\n){2,}", "\n\n");
	}
}
