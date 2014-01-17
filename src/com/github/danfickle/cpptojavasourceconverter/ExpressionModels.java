package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.List;

class ExpressionModels
{
	interface PlainString
	{
		public String toStringPlain();
	}
	
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
		abstract String toStringLhOnly();
		abstract String toStringRhOnly();
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
	
	abstract static class MIdentityExpression extends MExpression implements PlainString
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

	static String getStringLhs(MExpression model)
	{
		if (model instanceof MFieldReferenceExpression)
		{
			return ((MFieldReferenceExpression) model).object.toString();
		}
		else if (model instanceof MBracketExpression)
		{
			return getStringLhs(((MBracketExpression) model).operand); 
		}
		else
		{
			MyLogger.logImportant(model.getClass().getCanonicalName());
			return null;
		}
	}
	
	static String getStringRhs(MExpression model)
	{
		if (model instanceof MFieldReferenceExpression)
		{
			return ((MFieldReferenceExpression) model).field;
		}
		else if (model instanceof MBracketExpression)
		{
			return getStringRhs(((MBracketExpression) model).operand); 
		}
		else
		{
			MyLogger.logImportant(model.getClass().getCanonicalName());
			throw new RuntimeException();
		}
	}
	
	static String getPlainString(MExpression model)
	{
		if (model instanceof PlainString)
		{
			return ((PlainString) model).toStringPlain();
		}
		else if (model instanceof MLiteralExpression)
		{
			return ((MLiteralExpression) model).literal;
		}
		else if (model instanceof MBracketExpression)
		{
			return "(" + getPlainString(((MBracketExpression) model).operand) + ")";
		}
		else if (!(model instanceof MFieldReferenceExpression))
		{
			return model.toString();
		}
		else 
		{
			return null;
		}
	}
	
	/**
	 * Note: MStringExpression breaks the MVC architecture.
	 */
	static class MStringExpression extends MExpression
	{
		public String contents;
		
		@Override
		public String toString() 
		{
			return String.format("%s", this.contents);
		}
	}
	
	static class MMultiExpression extends MExpression
	{
		public List<MExpression> exprs = new ArrayList<MExpression>();
		
		@Override
		public String toString() 
		{
			String start = "";
			
			for (int i = 0; i < exprs.size(); i++)
			{
				start += exprs.get(i).toString();
				
				if (i != exprs.size() - 1)
					start +=  ", ";
			}
			
			return start;
		}
	}
	
	static class MAddressOfExpressionArrayItem extends MArrayExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.addressOf()", this.operand);
		}
	}
	
	static class MPostfixExpressionNumberInc extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);

			if (plain != null)
			{
				return String.format("%s.postInc()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.%s.postInc()", lhs, rhs);
			}
		}
	}

	static class MPostfixExpressionNumberDec extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);

			if (plain != null)
			{
				return String.format("%s.postDec()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.%s.postDec()", lhs, rhs);
			}
		}
	}

	static class MPrefixExpressionNumberInc extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.set(%s + 1)", plain, this.operand);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);
				
				return String.format("%s.%s.set(%s + 1)", lhs, rhs, this.operand);
			}
		}
	}

	static class MPrefixExpressionNumberDec extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.set(%s - 1)", plain, this.operand);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);
				
				return String.format("%s.%s.set(%s - 1)", lhs, rhs, this.operand);
			}
		}
	}

	static class MValueOfExpressionNumber extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%s.valueOf(%s)", this.type, this.operand);
		}
	}
	
	static class MValueOfExpressionArray extends MExpression
	{
		public List<MExpression> operands;
		public String type;
		
		@Override
		public String toString() 
		{
			String start = String.format("%s.create(", this.type);

			for (int i = 0; i < this.operands.size(); i++)
			{
				start += this.operands.get(i).toString();
				
				if (i != this.operands.size() - 1)
					start += ", ";
			}

			return start + ')';
		}
	}
	
	static class MValueOfExpressionPtr extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%s.valueOf(%s)", this.type, this.operand);
		}
	}
	
	static class MInfixExpressionWithPtrOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);
			
			if (plain != null)
			{
				return String.format("%s.ptrOffset(%s%s)", plain, this.operator, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.%s.ptrOffset(%s%s)", lhs, rhs, this.operator, this.right);
			}
		}
	}

	static class MInfixExpressionWithPtrOnRight extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.right);
			
			if (plain != null)
			{
				return String.format("%s.ptrOffset(%s%s)", plain, this.operator, this.left);
			}
			else
			{
				String lhs = getStringLhs(this.right);
				String rhs = getStringRhs(this.right);
				
				return String.format("%s.%s.ptrOffset(%s%s)", lhs, rhs, this.operator, this.left);
			}
		}
	}
	
	static class MInfixExpressionPtrComparison extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String right2 = getPlainString(this.right);
			String left2 = getPlainString(this.left);
			
			if (right2 != null)
			{
				right2 = String.format("%s.ptrCompare()", right2);
			}
			else
			{
				String lhs = getStringLhs(this.right);
				String rhs = getStringRhs(this.right);
				
				right2 = String.format("%s.%s.ptrCompare()", lhs, rhs);				
			}
			
			if (left2 != null)
			{
				left2 = String.format("%s.ptrCompare()", left2);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				left2 = String.format("%s.%s.ptrCompare()", lhs, rhs);				
			}
			
			return String.format("%s %s %s", left2, this.operator, right2);
		}		
	}
	
	static class MCompoundWithPtrOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);
			
			if (plain != null)
			{
				return String.format("%s.ptrAdjust(%s%s)", plain, this.operator, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.%s.ptrAdjust(%s%s)", lhs, rhs, this.operator, this.right);
			}
		}
	}
	
	static class MPtrCreateNull
	{
		// TODO: Not currently used.
	}
	
	static class MPostfixWithDeref extends MPostfixExpression
	{
		// TODO: Not currently used
	}

	static class MPrefixWithDeref extends MPrefixExpression
	{
		// TODO: Not currently used
	}
	
	static class MPtrCopy extends MExpression
	{
		public MExpression operand;
		
		@Override
		public String toString() 
		{
			return String.format("%s", this.operand);
		}
	}
	
	static class MInfixAssignmentWithPtrOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);
			
			if (plain != null)
			{
				return String.format("%s = %s", plain, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.%s = %s", lhs, rhs, this.right);
			}
		}
	}
	
	static class MRefWrapper extends MExpression
	{
		public MExpression operand;
		
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return plain;
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);
				
				return String.format("%s.%s", lhs, rhs);
			}
		}
	}
	
	static class MAddressOfExpressionPtr extends MExpression
	{
		public MExpression operand;
		
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.ptrAddressOf()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.%s.ptrAddressOf()", lhs, rhs);
			}
		}
	}
	
	static class MBracketExpression extends MExpression
	{
		public MExpression operand;
		
		@Override
		public String toString() 
		{
			return String.format("(%s)", this.operand);
		}
	}
	
	static class MArrayExpressionPtr extends MArrayExpression implements PlainString
	{
		@Override
		public String toString() 
		{
			return this.toStringPlain() + ".get()";
		}
		
		
		@Override
		public String toStringPlain() 
		{
			String operand;
			
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				operand = String.format("%s", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				operand = String.format("%s.%s", lhs, rhs);
			}
			
			operand += ".ptrOffset(";
			
			for (int i = 0; i < this.subscript.size(); i++)
			{
				operand += this.subscript.get(i).toString();
				
				if (i != this.subscript.size() - 1)
					operand += ", ";
			}
			
			return operand + ")";
		}
	}
	
	static class MInfixExpressionWithBitfieldOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	static class MInfixAssignmentWithNumberOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);
			
			if (plain != null)
			{
				return String.format("%s.set(%s)", plain, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.%s.set(%s)", lhs, rhs, this.right);
			}
		}
	}
	
	static class MInfixWithNumberOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	static class MCompoundWithNumberOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);
			
			if (plain != null)
			{
				return String.format("%s.set(%s %s %s)", plain, this.left, this.operator, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);

				return String.format("%s.%s.set(%s %s %s)", lhs, rhs, this.left, this.operator, this.right);
			}
		}
	}
	
	static class MInfixExpressionWithDerefOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	static class MInfixAssignmentWithBitfieldOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);
			
			if (plain != null)
			{
				return String.format("set%s(%s)", plain, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.set%s(%s)", lhs, rhs, this.right);
			}
		}
	}

	static class MInfixAssignmentWithDerefOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);
			
			if (plain != null)
			{
				return String.format("%s.set(%s)", plain, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.%s.set(%s)", lhs, rhs, this.right);
			}
		}
	}

	static class MCompoundWithBitfieldOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);

			if (plain != null)
			{
				return String.format("set%s(%s %s %s)", plain, this.left, this.operator, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.set%s(%s %s %s)", lhs, rhs, this.left, this.operator, this.right);
			}
		}
	}

	static class MCompoundWithDerefOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.left);

			if (plain != null)
			{
				return String.format("%s.set(%s %s %s)", plain, this.left, this.operator, this.right);
			}
			else
			{
				String lhs = getStringLhs(this.left);
				String rhs = getStringRhs(this.left);
				
				return String.format("%s.%s.set(%s %s %s)", lhs, rhs, this.left, this.operator, this.right);
			}
		}
	}
	
	static class MInfixExpressionPlain extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	static class MIdentityExpressionPlain extends MIdentityExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s", this.ident);
		}

		@Override
		public String toStringPlain() 
		{
			return this.toString();
		}
	}
	
	static class MIdentityExpressionBitfield extends MIdentityExpression
	{
		@Override
		public String toString() 
		{
			return String.format("get%s()", this.ident);
		}
		
		public String toStringPlain()
		{
			return String.format("%s", this.ident);
		}
	}

	static class MIdentityExpressionNumber extends MIdentityExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.get()", this.ident);
		}
		
		@Override
		public String toStringPlain()
		{
			return String.format("%s", this.ident);
		}
	}
	
	static class MIdentityExpressionPtr extends MIdentityExpression
	{
		@Override
		public String toString()
		{
			return String.format("%s.ptrCopy()", this.ident);
		}
		
		@Override
		public String toStringPlain()
		{
			return String.format("%s", this.ident);
		}
	}

	static class MIdentityExpressionDeref extends MIdentityExpression
	{
		@Override
		public String toString()
		{
			return String.format("%s.get()", this.ident);
		}
		
		@Override
		public String toStringPlain()
		{
			return String.format("%s", this.ident);
		}
	}
	
	static class MIdentityExpressionEnumerator extends MIdentityExpression
	{
		public String enumName;

		@Override
		public String toString()
		{
			return String.format("%s.%s.val", this.enumName, this.ident);
		}

		@Override
		public String toStringPlain()
		{
			return this.ident;
		}
	}
	
	static class MTernaryExpression extends MExpression
	{
		public MExpression condition;
		public MExpression negative;
		public MExpression positive;
		
		@Override
		public String toString() 
		{
			return String.format("%s ? %s : %s", this.condition, this.positive, this.negative);
		}
	}

	static class MFieldReferenceExpressionPlain extends MFieldReferenceExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.%s", this.object, this.field);
		}
		
		@Override
		String toStringLhOnly() 
		{
			return this.object.toString();
		}

		@Override
		String toStringRhOnly() 
		{
			return this.field;
		}
	}

	static class MFieldReferenceExpressionBitfield extends MFieldReferenceExpression
	{
		@Override
		public String toString()
		{
			return String.format("%s.get%s()", this.object, this.field);
		}
		
		@Override
		public String toStringLhOnly()
		{
			return String.format("%s", this.object);
		}

		@Override
		public String toStringRhOnly()
		{
			return String.format("%s", this.field);
		}
	}
	
	static class MFieldReferenceExpressionNumber extends MFieldReferenceExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.%s.get()", this.object, this.field);
		}
		
		@Override
		public String toStringLhOnly() 
		{
			return String.format("%s", this.object);
		}

		@Override
		public String toStringRhOnly() 
		{
			return String.format("%s", this.field);
		}
	}
	
	static class MFieldReferenceExpressionPtr extends MFieldReferenceExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.%s.ptrCopy()", this.object, this.field);
		}
		
		@Override
		public String toStringLhOnly() 
		{
			return String.format("%s", this.object);
		}

		@Override
		public String toStringRhOnly() 
		{
			return String.format("%s", this.field);
		}
	}
	
	static class MFieldReferenceExpressionDeref extends MFieldReferenceExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.%s.get()", this.object, this.field);
		}
		
		@Override
		public String toStringLhOnly() 
		{
			return String.format("%s", this.object);
		}

		@Override
		public String toStringRhOnly() 
		{
			return String.format("%s", this.field);
		}
	}
	
	static class MFieldReferenceExpressionEnumerator extends MFieldReferenceExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.%s", this.object, this.field);
		}
		
		@Override
		String toStringLhOnly()
		{
			return this.object.toString();
		}

		@Override
		String toStringRhOnly() 
		{
			return this.field;
		}
	}

	static class MFunctionCallExpression extends MFunctionCallExpressionParent
	{
		@Override
		public String toString() 
		{
			String start = this.name.toString() + "(";

			for (int i = 0; i < this.args.size(); i++)
			{
				start += this.args.get(i).toString();
				
				if (i != this.args.size() - 1)
					start += ", ";
			}

			return start + ")";
		}
	}
	
	static class MClassInstanceCreation extends MFunctionCallExpressionParent
	{
		@Override
		public String toString() 
		{
			String start = "new " + this.name.toString() + "(";

			for (int i = 0; i < this.args.size(); i++)
			{
				start += this.args.get(i).toString();
				
				if (i != this.args.size() - 1)
					start += ", ";
			}

			return start + ")";
		}
	}
	
	static class MLiteralExpression extends MExpression
	{
		public String literal;
		
		@Override
		public String toString() 
		{
			return literal;
		}
	}
	
	static class MPrefixExpressionPlain extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s%s", this.operator, this.operand);
		}
	}

	static class MPrefixExpressionPointer extends MPrefixExpression
	{
		// TODO: Shouldn't use this.
	}
	
	static class MAddressOfExpression extends MExpression
	{
		public MExpression operand;
		
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.addressOf()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);
				
				return String.format("%s.%s.addressOf()", lhs, rhs);
			}
		}
	}
	
	static class MPostfixExpressionPlain extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s%s", this.operand, this.operator);
		}
	}

	static class MPostfixExpressionPointerInc extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.ptrPostInc()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);
				
				return String.format("%s.%s.ptrPostInc()", lhs, rhs);
			}
		}
	}

	static class MPostfixExpressionPointerDec extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.ptrPostDec()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);
				
				return String.format("%s.%s.ptrPostDec()", lhs, rhs);
			}
		}
	}

	static class MPrefixExpressionPointerInc extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.ptrAdjust(1)", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.%s.ptrAdjust(1)", lhs, rhs);
			}
		}
	}

	static class MPrefixExpressionPointerDec extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			if (this.operand instanceof MIdentityExpressionPtr)
			{
				MIdentityExpressionPtr ident = (MIdentityExpressionPtr) this.operand;
				return String.format("%s.ptrAdjust(-1)", ident.toStringPlain());
			}
			else if (this.operand instanceof MFieldReferenceExpressionPtr)
			{
				MFieldReferenceExpressionPtr fr = (MFieldReferenceExpressionPtr) this.operand;
				return String.format("%s.%s.ptrAdjust(-1)", fr.toStringLhOnly(), fr.toStringRhOnly());
			}
			else
			{
				MyLogger.logImportant(this.getClass().getCanonicalName());
				return null;
			}
		}
	}
	
	static class MPrefixExpressionPointerStar extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.get()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.%s.get()", lhs, rhs);
			}
		}
	}
	
	static class MPostfixExpressionBitfieldInc extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("postInc%s()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.postInc%s()", lhs, rhs);
			}
		}
	}

	static class MPostfixExpressionBitfieldDec extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("postDec%s()", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.postDec%s()", lhs, rhs);
			}
		}
	}

	static class MPrefixExpressionBitfieldInc extends MPrefixExpression
	{
		public MExpression set;
		
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);

			if (plain != null)
			{
				return String.format("set%s(%s + 1)", plain, this.operand);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.set%s(%s + 1)", lhs, rhs, this.operand);
			}
		}
	}
	
	static class MPrefixExpressionBitfieldDec extends MPrefixExpression
	{
		public MExpression set;
		
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);

			if (plain != null)
			{
				return String.format("set%s(%s - 1)", plain, this.operand);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.set%s(%s - 1)", lhs, rhs, this.operand);
			}
		}
	}	
	
	static class MPrefixExpressionBitfield extends MPrefixExpression
	{
		public MExpression set;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s", this.operator, this.operand);
		}
	}
	
	static class MCastExpression extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("(%s) %s", this.type, this.operand);
		}
	}

	static class MCastExpressionToEnum extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%s.fromValue(%s)", this.type, this.operand);
		}
	}
	
	static class MDeleteObjectSingle extends MDeleteExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s.destruct()", this.operand);
		}
	}
	
	static class MDeleteObjectMultiple extends MDeleteExpression
	{
		@Override
		public String toString() 
		{
			return String.format("DestructHelper.destructArray(%s)", this.operand);
		}
	}
	
	static class MEmptyExpression extends MExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s", "/* Empty expression */");
		}
	}
	
	static class MNewArrayExpression extends MExpression
	{
		public List<MExpression> sizes = new ArrayList<MExpression>();
		public String type;
		
		@Override
		public String toString() 
		{
			String start = String.format("%sMulti.create(", this.type);
			
			for (int i = 0; i < this.sizes.size(); i++)
			{
				start += this.sizes.get(i).toString();
				
				if (i != this.sizes.size() - 1)
					start += ", ";
			}
			
			return start + ")";
		}
	}
	
	static class MNewArrayExpressionObject extends MExpression
	{
		public List<MExpression> sizes = new ArrayList<MExpression>();
		public String type;
		
		@Override
		public String toString() 
		{
			String start = String.format("PTR.newObjPtr(CreateHelper.allocateArray(%s.class,", this.type);
			
			for (int i = 0; i < this.sizes.size(); i++)
			{
				start += this.sizes.get(i).toString();
				
				if (i != this.sizes.size() - 1)
					start += ", ";
			}
			
			return start + ")";
		}
	}
	
	static class MNewExpression extends MExpression
	{
		public String type;
		public MExpression argument;
		
		@Override
		public String toString() 
		{
			return String.format("PTR.new%sPtr(%s)", this.type, this.argument);
		}
	}
	
	static class MNewExpressionObject extends MExpression
	{
		public String type;
		public List<MExpression> arguments = new ArrayList<MExpression>();
		
		@Override
		public String toString() 
		{
			String start = String.format("new %s(", this.type);
			
			for (int i = 0; i < this.arguments.size(); i++)
			{
				start += this.arguments.get(i).toString();
				
				if (i != this.arguments.size() - 1)
					start += ", ";
			}
			
			return start + ")";
		}
	}
	
	static class MAddItemCall extends MExpression
	{
		public MExpression operand;
		public int nextFreeStackId;
		
		@Override
		public String toString() 
		{
			return String.format("StackHelper.addItem(%s, %d, __stack)", this.operand, this.nextFreeStackId);
		}
	}
}
