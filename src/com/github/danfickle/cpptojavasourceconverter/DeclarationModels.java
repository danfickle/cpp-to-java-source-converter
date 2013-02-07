package com.github.danfickle.cpptojavasourceconverter;

import java.util.List;
import org.eclipse.jdt.core.dom.Type;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;

class DeclarationModels 
{
	static class CppEnumerator
	{
		String m_simpleName;
		MExpression m_value;
		
		static CppEnumerator create(String simpleName, MExpression value)
		{
			CppEnumerator ret = new CppEnumerator();
			ret.m_simpleName = simpleName;
			ret.m_value = value;
			return ret;
		}
	}
	
	static class CppEnum
	{
		String m_simpleName;
		String m_qualified;
		List<CppEnumerator> m_enumerators;

		static CppEnum create(String simpleName, String qualified)
		{
			CppEnum ret = new CppEnum();
			ret.m_simpleName = simpleName;
			ret.m_qualified = qualified;
			return ret;
		}
	}
	
	static class CppBitfield
	{
		String m_simpleName;
		String m_qualified;
		Type m_type;
		MExpression m_bits;

		static CppBitfield create(String simpleName, String qualified)
		{
			CppBitfield ret = new CppBitfield();
			ret.m_simpleName = simpleName;
			ret.m_qualified = qualified;
			return ret;
		}
	}

}
