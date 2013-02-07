package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

class ExpressionModels
{
	abstract static class MExpression
	{}

	abstract static class MArrayExpression extends MExpression
	{
		public MExpression operand;
		public List<MExpression> subscript = new ArrayList<MExpression>(1);
		public boolean leftSide; //TODO
	}
	
	abstract static class MFieldReferenceExpression extends MExpression
	{
		public MExpression object;
		public String field;
	}
	
	abstract static class MInfixExpression extends MExpression
	{
		public MExpression left;
		public MExpression right;
		public String operator;
	}
	
	abstract static class MPrefixExpression extends MExpression
	{
		public MExpression operand;
		public String operator;
	}
	
	abstract static class MPostfixExpression extends MExpression
	{
		public MExpression operand;
		public String operator;
	}
	
	abstract static class MIdentityExpression extends MExpression
	{
		public String ident;
	}
	
	abstract static class MDeleteExpression extends MExpression
	{
		public MExpression operand;
	}
	
	// 1
	static class MArrayExpressionPlain extends MArrayExpression
	{
		public boolean isArray = true;
	}

	// 2
	static class MArrayExpressionPtr extends MArrayExpression
	{
		public boolean isPtrArray = true;
	}
	
	// 3
	static class MInfixExpressionWithBitfieldOnLeft extends MInfixExpression
	{
		public boolean isInfixWithBitfieldOnLeft = true;
	}

	// 4
	static class MInfixExpressionWithDerefOnLeft extends MInfixExpression
	{
		public boolean isInfixWithDerefOnLeft = true;
	}

	// 35
	static class MInfixAssignmentWithBitfieldOnLeft extends MInfixExpression
	{
		public boolean isAssignmentWithBitfieldOnLeft = true;
	}

	// 36
	static class MInfixAssignmentWithDerefOnLeft extends MInfixExpression
	{
		public boolean isAssignmentWithDerefOnLeft = true;
	}

	// 37
	static class MCompoundWithBitfieldOnLeft extends MInfixExpression
	{
		public boolean isCompoundWithBitfieldOnLeft = true;
	}

	// 38
	static class MCompoundWithDerefOnLeft extends MInfixExpression
	{
		public boolean isCompoundWithDerefOnLeft = true;
	}
	
	// 5
	static class MInfixExpressionPlain extends MInfixExpression
	{
		public boolean isInfix = true;
	}

	// 7
	static class MIdentityExpressionPlain extends MIdentityExpression
	{
		public boolean isIdentity = true;
	}
	
	// 8
	static class MIdentityExpressionBitfield extends MIdentityExpression
	{
		public boolean isIdentityBitfield = true;
	}

	// 9
	static class MIdentityExpressionPtr extends MIdentityExpression
	{
		public boolean isIdentityPtr = true;
	}
	
	// 10
	static class MIdentityExpressionEnumerator extends MIdentityExpression
	{
		public boolean isIdentityEnumerator = true;

		public String enumName;
	}
	
	// 11
	static class MTernaryExpression extends MExpression
	{
		public boolean isTernary = true;
		
		public MExpression condition;
		public MExpression negative;
		public MExpression positive;
	}

	// 12
	static class MFieldReferenceExpressionPlain extends MFieldReferenceExpression
	{
		public boolean isFieldReference = true;
	}

	// 13
	static class MFieldReferenceExpressionBitfield extends MFieldReferenceExpression
	{
		public boolean isFieldReferenceBitfield = true;
	}
	
	// 14
	static class MFieldReferenceExpressionPtr extends MFieldReferenceExpression
	{
		public boolean isFieldReferencePtr = true;
	}
	
	// 15
	static class MFieldReferenceExpressionEnumerator extends MFieldReferenceExpression
	{
		public boolean isFieldReferenceEnumerator = true;
	}
	
	// 16
	static class MFunctionCallExpression extends MExpression
	{
		public boolean isFunctionCall = true;
		
		public MExpression name;
		public List<MExpression> args;
	}
	
	// 17
	static class MLiteralExpression extends MExpression
	{
		public boolean isLiteral = true;
		
		public String literal;
	}
	
	// 18
	static class MPrefixExpressionPlain extends MPrefixExpression
	{
		public boolean isPrefix = true;
	}

	// 19
	static class MPrefixExpressionPointer extends MPrefixExpression
	{
		public boolean isPrefixPointer = true;
	}
	
	// 21
	static class MPostfixExpressionPlain extends MPostfixExpression
	{
		public boolean isPostfix = true;
	}

	// 23
	static class MPostfixExpressionPointerInc extends MPostfixExpression
	{
		public boolean isPostfixPointerInc = true;
	}

	// 24
	static class MPostfixExpressionPointerDec extends MPostfixExpression
	{
		public boolean isPostfixPointerDec = true;
	}

	// 25
	static class MPrefixExpressionPointerInc extends MPrefixExpression
	{
		public boolean isPrefixPointerInc = true;
	}

	// 26
	static class MPrefixExpressionPointerDec extends MPrefixExpression
	{
		public boolean isPrefixPointerDec = true;
	}
	
	// 27
	static class MPrefixExpressionPointerStar extends MPrefixExpression
	{
		public boolean isPrefixPointerStar = true;
	}
	
	// 28
	static class MPostfixExpressionBitfieldInc extends MPostfixExpression
	{
		public boolean isPostfixBitfieldInc = true;
	}
	
	// 29
	static class MPostfixExpressionBitfieldDec extends MPostfixExpression
	{
		public boolean isPostfixBitfieldDec = true;
	}

	// 33
	static class MPrefixExpressionBitfieldInc extends MPrefixExpression
	{
		public MExpression set;
		public boolean isPrefixBitfieldInc = true;
	}
	
	// 34
	static class MPrefixExpressionBitfieldDec extends MPrefixExpression
	{
		public MExpression set;
		public boolean isPrefixBitfieldDec = true;
	}	
	
	// 31
	static class MPrefixExpressionBitfield extends MPrefixExpression
	{
		public MExpression set;
		public boolean isPrefixBitfield = true;
	}
	
	// 32
	static class MCastExpression extends MExpression
	{
		public boolean isCast = true;
		
		public MExpression operand;
		public String type = "int"; // TODO
	}
	
	// 40
	static class MDeleteObjectSingle extends MDeleteExpression
	{
		public boolean isDeleteObjectSingle = true;
	}
	
	// 41
	static class MDeleteObjectMultiple extends MDeleteExpression
	{
		public boolean isDeleteObjectMultiple = true;
	}
	
	// 42
	static class MEmptyExpression extends MExpression
	{
		public boolean isEmpty = true;
	}
}
