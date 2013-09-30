package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

class ExpressionModels
{
	abstract static class MExpression
	{}

	abstract static class MArrayExpression extends MExpression
	{
		public MExpression operand;
		public List<MExpression> subscript = new ArrayList<MExpression>(1);
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
	
	abstract static class MFunctionCallExpressionParent extends MExpression
	{
		public MExpression name;
		public List<MExpression> args = new ArrayList<MExpression>();
	}
	
	static class MStringExpression extends MExpression
	{
		public boolean isStringExpression = true;
		public String contents;
	}
	
	static class MMultiExpression extends MExpression
	{
		public boolean isMultiExpression = true;
		public List<MExpression> exprs = new ArrayList<MExpression>();
	}
	
	static class MAddressOfExpressionArrayItem extends MArrayExpression
	{
		public boolean isArrayAccessWithAddressOf = true;
	}
	
	static class MPostfixExpressionNumberInc extends MPostfixExpression
	{
		public boolean isPostfixNumberInc = true;
	}

	static class MPostfixExpressionNumberDec extends MPostfixExpression
	{
		public boolean isPostfixNumberDec = true;
	}

	static class MPrefixExpressionNumberInc extends MPrefixExpression
	{
		public boolean isPrefixNumberInc = true;
	}

	static class MPrefixExpressionNumberDec extends MPrefixExpression
	{
		public boolean isPrefixNumberDec = true;
	}

	static class MValueOfExpressionNumber extends MExpression
	{
		public boolean isValueOfNumber = true;
		public MExpression operand;
		public String type;
	}
	
	static class MValueOfExpressionArray extends MExpression
	{
		public boolean isValueOfArray = true;
		public List<MExpression> operands;
		public String type;
	}
	
	static class MInfixExpressionWithPtrOnLeft extends MInfixExpression
	{
		public boolean isInfixWithPtrOnLeft = true;
	}

	static class MInfixExpressionWithPtrOnRight extends MInfixExpression
	{
		public boolean isInfixWithPtrOnRight = true;
	}
	
	static class MInfixExpressionPtrComparison extends MInfixExpression
	{
		public boolean isInfixWithPtrComparison = true;
	}
	
	static class MCompoundWithPtrOnLeft extends MInfixExpression
	{
		public boolean isCompoundWithPtrOnLeft = true;
	}
	
	static class MPtrCreateNull
	{
		public boolean isNullPtrCreate = true;
	}
	
	static class MPostfixWithDeref extends MPostfixExpression
	{
		public boolean isPostfixDeref = true;
	}

	static class MPrefixWithDeref extends MPrefixExpression
	{
		public boolean isPrefixDeref = true;
	}
	
	static class MPtrCopy extends MExpression
	{
		public boolean isPtrCopy = true;
		public MExpression operand;
	}
	
	static class MInfixAssignmentWithPtrOnLeft extends MInfixExpression
	{
		public boolean isAssignmentWithPtrOnLeft = true;
	}
	
	static class MRefWrapper extends MExpression
	{
		public boolean isRefWrapper = true;
		public MExpression operand;
	}
	
	static class MAddressOfExpressionPtr extends MExpression
	{
		public boolean isAddressOfPtr = true;
		public MExpression operand;
	}
	
	static class MBracketExpression extends MExpression
	{
		public boolean isBrackets = true;
		public MExpression operand;
	}
	
	static class MArrayExpressionPtr extends MArrayExpression
	{
		public boolean isPtrArray = true;
	}
	
	static class MInfixExpressionWithBitfieldOnLeft extends MInfixExpression
	{
		public boolean isInfixWithBitfieldOnLeft = true;
	}

	static class MInfixAssignmentWithNumberOnLeft extends MInfixExpression
	{
		public boolean isAssignmentWithNumberOnLeft = true;
	}
	
	static class MInfixWithNumberOnLeft extends MInfixExpression
	{
		public boolean isInfix = true;
	}

	static class MCompoundWithNumberOnLeft extends MInfixExpression
	{
		public boolean isCompoundWithNumberOnLeft = true;
	}
	
	static class MInfixExpressionWithDerefOnLeft extends MInfixExpression
	{
		public boolean isInfixWithDerefOnLeft = true;
	}

	static class MInfixAssignmentWithBitfieldOnLeft extends MInfixExpression
	{
		public boolean isAssignmentWithBitfieldOnLeft = true;
	}

	static class MInfixAssignmentWithDerefOnLeft extends MInfixExpression
	{
		public boolean isAssignmentWithDerefOnLeft = true;
	}

	static class MCompoundWithBitfieldOnLeft extends MInfixExpression
	{
		public boolean isCompoundWithBitfieldOnLeft = true;
	}

	static class MCompoundWithDerefOnLeft extends MInfixExpression
	{
		public boolean isCompoundWithDerefOnLeft = true;
	}
	
	static class MInfixExpressionPlain extends MInfixExpression
	{
		public boolean isInfix = true;
	}

	static class MIdentityExpressionPlain extends MIdentityExpression
	{
		public boolean isIdentity = true;
	}
	
	static class MIdentityExpressionBitfield extends MIdentityExpression
	{
		public boolean isIdentityBitfield = true;
	}

	static class MIdentityExpressionNumber extends MIdentityExpression
	{
		public boolean isIdentityNumber = true;
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

	static class MFieldReferenceExpressionPlain extends MFieldReferenceExpression
	{
		public boolean isFieldReference = true;
	}

	static class MFieldReferenceExpressionBitfield extends MFieldReferenceExpression
	{
		public boolean isFieldReferenceBitfield = true;
	}
	
	static class MFieldReferenceExpressionNumber extends MFieldReferenceExpression
	{
		public boolean isFieldReferenceNumber = true;
	}
	
	static class MFieldReferenceExpressionPtr extends MFieldReferenceExpression
	{
		public boolean isFieldReferencePtr = true;
	}
	
	static class MFieldReferenceExpressionEnumerator extends MFieldReferenceExpression
	{
		public boolean isFieldReferenceEnumerator = true;
	}

	static class MFunctionCallExpression extends MFunctionCallExpressionParent
	{
		public boolean isFunctionCall = true;
	}
	
	static class MClassInstanceCreation extends MFunctionCallExpressionParent
	{
		public boolean isClassInstanceCreation = true;
	}
	
	static class MLiteralExpression extends MExpression
	{
		public boolean isLiteral = true;
		
		public String literal;
	}
	
	static class MPrefixExpressionPlain extends MPrefixExpression
	{
		public boolean isPrefix = true;
	}

	static class MPrefixExpressionPointer extends MPrefixExpression
	{
		public boolean isPrefixPointer = true;
	}
	
	static class MAddressOfExpression extends MExpression
	{
		public boolean isAddressOf = true;
		
		public MExpression operand;
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

	static class MPrefixExpressionPointerInc extends MPrefixExpression
	{
		public boolean isPrefixPointerInc = true;
	}

	static class MPrefixExpressionPointerDec extends MPrefixExpression
	{
		public boolean isPrefixPointerDec = true;
	}
	
	static class MPrefixExpressionPointerStar extends MPrefixExpression
	{
		public boolean isPrefixPointerStar = true;
	}
	
	static class MPostfixExpressionBitfieldInc extends MPostfixExpression
	{
		public boolean isPostfixBitfieldInc = true;
	}

	static class MPostfixExpressionBitfieldDec extends MPostfixExpression
	{
		public boolean isPostfixBitfieldDec = true;
	}

	static class MPrefixExpressionBitfieldInc extends MPrefixExpression
	{
		public MExpression set;
		public boolean isPrefixBitfieldInc = true;
	}
	
	static class MPrefixExpressionBitfieldDec extends MPrefixExpression
	{
		public MExpression set;
		public boolean isPrefixBitfieldDec = true;
	}	
	
	static class MPrefixExpressionBitfield extends MPrefixExpression
	{
		public MExpression set;
		public boolean isPrefixBitfield = true;
	}
	
	static class MCastExpression extends MExpression
	{
		public boolean isCast = true;
		
		public MExpression operand;
		public String type = "int"; // TODO
	}
	
	static class MDeleteObjectSingle extends MDeleteExpression
	{
		public boolean isDeleteObjectSingle = true;
	}
	
	static class MDeleteObjectMultiple extends MDeleteExpression
	{
		public boolean isDeleteObjectMultiple = true;
	}
	
	static class MEmptyExpression extends MExpression
	{
		public boolean isEmpty = true;
	}
	
	static class MNewArrayExpression extends MExpression
	{
		public boolean isBasicNewArray = true;

		public List<MExpression> sizes = new ArrayList<MExpression>();
		public String type;
	}
	
	static class MNewArrayExpressionObject extends MExpression
	{
		public boolean isObjectNewArray = true;

		public List<MExpression> sizes = new ArrayList<MExpression>();
		public String type;
	}
	
	static class MNewExpression extends MExpression
	{
		public boolean isNewSingle = true;
		
		public String type;
		public MExpression argument;
	}
	
	static class MNewExpressionObject extends MExpression
	{
		public boolean isNewObject = true;
		
		public String type;
		public List<MExpression> arguments = new ArrayList<MExpression>();
	}
	
	static class MAddItemCall extends MExpression
	{
		public boolean isAddItemCall = true;
		
		public MExpression operand;
		public int nextFreeStackId;
	}
}
