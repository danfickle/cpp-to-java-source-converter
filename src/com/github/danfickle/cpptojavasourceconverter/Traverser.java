package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import com.github.danfickle.cpptojavasourceconverter.models.DeclarationModels;
import com.github.danfickle.cpptojavasourceconverter.models.StmtModels;
import com.github.danfickle.cpptojavasourceconverter.models.DeclarationModels.*;

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
		con.stmtModels = new StmtModels(con);
		con.declModels = new DeclarationModels(con);

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
		
		for (CppDeclaration decl : con.global.decls)
		{
			if (decl.parent == null)
			{
				output.append(decl.toString());
			}
		}
		
		for (CppDeclaration decl : con.global.fileClasses.values())
		{
			output.append(decl.toString());
		}
		
		// TODO: Match up declarations with files.
		con.global.decls.clear();
		con.global.fileClasses.clear();
		
		// Replace 2 or more (greedy) newlines with 2 newlines.
		return output.toString().replaceAll("(\\n){2,}", "\n\n");
	}
}
