package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

class Models
{
	abstract static class MExpression
	{}

	abstract static class MArrayExpression extends MExpression
	{
		public MExpression operand;
		public List<MExpression> subscript = new ArrayList<MExpression>(1);
		public boolean leftSide;
	}
	
	static class MArrayExpressionPlain extends MArrayExpression
	{
		public boolean isArray = true;
	}

	static class MArrayExpressionPtr extends MArrayExpression
	{
		public boolean isPtrArray = true;
	}
	
	abstract static class MInfixExpression extends MExpression
	{
		public MExpression left;
		public MExpression right;
		public String operator;
	}
	
	static class MInfixExpressionWithBitfieldOnLeft extends MInfixExpression
	{
		public boolean isInfixWithBitfieldOnLeft = true;
	}
	
	static class MInfixExpressionWithDerefOnLeft extends MInfixExpression
	{
		public boolean isInfixWithDerefOnLeft = true;
	}
	
	static class MInfixExpressionPlain extends MInfixExpression
	{
		public boolean isInfix = true;
	}
	
	abstract static class MIdentityExpression extends MExpression
	{
		public String ident;
	}

	static class MIdentityExpressionPlain extends MIdentityExpression
	{
		public boolean isIdentity = true;
	}
	
	static class MIdentityExpressionBitfield extends MIdentityExpression
	{
		public boolean isIdentityBitfield = true;
	}

	static class MIdentityExpressionPtr extends MIdentityExpression
	{
		public boolean isIdentityPtr = true;
	}
	
	static class MIdentityExpressionEnumerator extends MIdentityExpression
	{
		public boolean isIdentityEnumerator = true;

		public String enumName;
	}
	
	static class MTernaryExpression extends MExpression
	{
		public boolean isTernary = true;
		
		public MExpression condition;
		public MExpression negative;
		public MExpression positive;
	}
	
	abstract static class MFieldReferenceExpression extends MExpression
	{
		public MExpression object;
		public String field;
	}

	static class MFieldReferenceExpressionPlain extends MFieldReferenceExpression
	{
		public boolean isFieldReference = true;
	}

	static class MFieldReferenceExpressionBitfield extends MFieldReferenceExpression
	{
		public boolean isFieldReferenceBitfield = true;
	}
	
	static class MFieldReferenceExpressionPtr extends MFieldReferenceExpression
	{
		public boolean isFieldReferencePtr = true;
	}
	
	static class MFieldReferenceExpressionEnumerator extends MFieldReferenceExpression
	{
		public boolean isFieldReferenceEnumerator = true;
	}
	
	static class MFunctionCallExpression extends MExpression
	{
		public boolean isFunctionCall = true;
		
		public MExpression name;
		public List<MExpression> args;
	}
	
	static class MLiteralExpression extends MExpression
	{
		public boolean isLiteral = true;
		
		public String literal;
	}
	
	static class MPrefixExpression extends MExpression
	{
		public MExpression operand;
		public String operator;
	}

	static class MPrefixExpressionPlain extends MPrefixExpression
	{
		public boolean isPrefix = true;
	}

	static class MPrefixExpressionPointer extends MPrefixExpression
	{
		public boolean isPrefixPointer = true;
	}

	static class MPostfixExpression extends MExpression
	{
		public MExpression operand;
		public String operator;
	}
	
	static class MPostfixExpressionPlain extends MPostfixExpression
	{
		public boolean isPostfix = true;
	}

	static class MPostfixExpressionPointerInc extends MPostfixExpression
	{
		public boolean isPostfixPointerInc = true;
	}

	static class MPostfixExpressionPointerDec extends MPostfixExpression
	{
		public boolean isPostfixPointerDec = true;
	}
	
	static class MCastExpression extends MExpression
	{
		public boolean isCast = true;
		
		public MExpression operand;
		public String type = "int"; // TODO
	}
	
	
	
	
	
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

