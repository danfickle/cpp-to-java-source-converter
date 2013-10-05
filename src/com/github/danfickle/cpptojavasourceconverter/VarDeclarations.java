package com.github.danfickle.cpptojavasourceconverter;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppDeclaration;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;

public class VarDeclarations 
{
	static class MSimpleDecl extends CppDeclaration
	{
		public boolean isSimpleDecl = true;

		public boolean isStatic;
		public String type;
		public MExpression initExpr;
		public boolean isPublic;
	}
}
