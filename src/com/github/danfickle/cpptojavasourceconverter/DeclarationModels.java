package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.Type;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;

class DeclarationModels 
{
	abstract static class CppDeclaration { }
	
	static class CppEnumerator
	{
		public boolean isEnumerator = true;
		public String name;
		public MExpression value;
	}
	
	static class CppEnum extends CppDeclaration
	{
		public boolean isEnum = true;
		public String simpleName;
		public String qualified;
		public List<CppEnumerator> enumerators = new ArrayList<CppEnumerator>();
	}
	
	static class CppBitfield extends CppDeclaration
	{
		public boolean isBitfield = true;
		public String name;
		public String qualified;
		public String type;
		public MExpression bits;
	}
}
