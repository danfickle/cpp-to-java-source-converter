package com.github.danfickle.cpptojavasourceconverter.models;

import java.util.ArrayList;
import java.util.List;

public class ExpressionModels
{
	interface PlainString
	{
		public String toStringPlain();
	}
	
	public abstract static class MExpression
	{}

	public abstract static class MArrayExpression extends MExpression
	{
		public MExpression operand;
		public List<MExpression> subscript = new ArrayList<MExpression>(1);
	}
	
	public abstract static class MFieldReferenceExpression extends MExpression
	{
		public MExpression object;
		public String field;
		abstract String toStringLhOnly();
		abstract String toStringRhOnly();
	}
	
	public abstract static class MInfixExpression extends MExpression
	{
		public MExpression left;
		public MExpression right;
		public String operator;
	}
	
	public abstract static class MPrefixExpression extends MExpression
	{
		public MExpression operand;
		public String operator;
	}
	
	public abstract static class MPostfixExpression extends MExpression
	{
		public MExpression operand;
		public String operator;
	}
	
	public abstract static class MIdentityExpression extends MExpression implements PlainString
	{
		public String ident;
	}
	
	public abstract static class MDeleteExpression extends MExpression
	{
		public MExpression operand;
	}
	
	public abstract static class MFunctionCallExpressionParent extends MExpression
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
			assert(false);
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
			assert(false);
			return null;
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
	
	static String joinExpressions(List<MExpression> exprs)
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < exprs.size(); i++)
		{
			sb.append(exprs.get(i).toString());
			if (i != exprs.size() - 1)
				sb.append(", ");
		}

		return sb.toString();
	}
	
	/**
	 * Note: MStringExpression breaks the MVC architecture.
	 */
	public static class MStringExpression extends MExpression
	{
		public String contents;
		
		@Override
		public String toString() 
		{
			return String.format("%s", this.contents);
		}
	}
	
	public static class MMultiExpression extends MExpression
	{
		public List<MExpression> exprs = new ArrayList<MExpression>();
		
		@Override
		public String toString() 
		{
			return joinExpressions(exprs);
		}
	}
	
	public static class MAddressOfExpressionArrayItem extends MArrayExpression
	{
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
	
	public static class MPostfixExpressionNumberInc extends MPostfixExpression
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

	public static class MPostfixExpressionNumberDec extends MPostfixExpression
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

	public static class MPrefixExpressionNumberInc extends MPrefixExpression
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

	public static class MPrefixExpressionNumberDec extends MPrefixExpression
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

	public static class MValueOfExpressionNumber extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%s.valueOf(%s)", this.type, this.operand);
		}
	}
	
	public static class MValueOfExpressionArray extends MExpression
	{
		public List<MExpression> operands;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%s.create(%s)", this.type, joinExpressions(this.operands));
		}
	}
	
	public static class MValueOfExpressionPtr extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%s.valueOf(%s)", this.type, this.operand);
		}
	}
	
	public static class MInfixExpressionWithPtrOnLeft extends MInfixExpression
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

	public static class MInfixExpressionWithPtrOnRight extends MInfixExpression
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
	
	public static class MInfixExpressionPtrComparison extends MInfixExpression
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
	
	public static class MCompoundWithPtrOnLeft extends MInfixExpression
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
	
	public static class MPtrCopy extends MExpression
	{
		public MExpression operand;
		
		@Override
		public String toString() 
		{
			return String.format("%s", this.operand);
		}
	}
	
	public static class MInfixAssignmentWithPtrOnLeft extends MInfixExpression
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
	
	public static class MRefWrapper extends MExpression
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
	
	public static class MAddressOfExpressionPtr extends MExpression
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
	
	public static class MBracketExpression extends MExpression
	{
		public MExpression operand;
		
		@Override
		public String toString() 
		{
			return String.format("(%s)", this.operand);
		}
	}
	
	public static class MArrayExpressionPtr extends MArrayExpression implements PlainString
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
			
			return String.format("%s.ptrOffset(%s)", operand, joinExpressions(this.subscript));
		}
	}
	
	public static class MInfixExpressionWithBitfieldOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	public static class MInfixAssignmentWithNumberOnLeft extends MInfixExpression
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
	
	public static class MInfixWithNumberOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	public static class MCompoundWithNumberOnLeft extends MInfixExpression
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
	
	public static class MInfixExpressionWithDerefOnLeft extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	public static class MInfixAssignmentWithBitfieldOnLeft extends MInfixExpression
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

	public static class MInfixAssignmentWithDerefOnLeft extends MInfixExpression
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

	public static class MCompoundWithBitfieldOnLeft extends MInfixExpression
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

	public static class MCompoundWithDerefOnLeft extends MInfixExpression
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
	
	public static class MInfixExpressionPlain extends MInfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s %s %s", this.left, this.operator, this.right);
		}
	}

	public static class MIdentityExpressionPlain extends MIdentityExpression
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
	
	public static class MIdentityExpressionBitfield extends MIdentityExpression
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

	public static class MIdentityExpressionNumber extends MIdentityExpression
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
	
	public static class MIdentityExpressionPtr extends MIdentityExpression
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

	public static class MIdentityExpressionDeref extends MIdentityExpression
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
	
	public static class MIdentityExpressionEnumerator extends MIdentityExpression
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
	
	public static class MTernaryExpression extends MExpression
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

	public static class MFieldReferenceExpressionPlain extends MFieldReferenceExpression
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

	public static class MFieldReferenceExpressionBitfield extends MFieldReferenceExpression
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
	
	public static class MFieldReferenceExpressionNumber extends MFieldReferenceExpression
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
	
	public static class MFieldReferenceExpressionPtr extends MFieldReferenceExpression
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
	
	public static class MFieldReferenceExpressionDeref extends MFieldReferenceExpression
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
	
	public static class MFieldReferenceExpressionEnumerator extends MFieldReferenceExpression
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

	public static class MFunctionCallExpression extends MFunctionCallExpressionParent
	{
		@Override
		public String toString() 
		{
			return String.format("%s(%s)", this.name, joinExpressions(this.args));
		}
	}
	
	public static class MClassInstanceCreation extends MFunctionCallExpressionParent
	{
		@Override
		public String toString() 
		{
			return String.format("new %s(%s)", this.name, joinExpressions(this.args));
		}
	}
	
	public static class MLiteralExpression extends MExpression
	{
		public String literal;
		
		@Override
		public String toString() 
		{
			return literal;
		}
	}
	
	public static class MPrefixExpressionPlain extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s%s", this.operator, this.operand);
		}
	}

	public static class MAddressOfExpression extends MExpression
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
	
	public static class MPostfixExpressionPlain extends MPostfixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s%s", this.operand, this.operator);
		}
	}

	public static class MPostfixExpressionPointerInc extends MPostfixExpression
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

	public static class MPostfixExpressionPointerDec extends MPostfixExpression
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

	public static class MPrefixExpressionPointerInc extends MPrefixExpression
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

	public static class MPrefixExpressionPointerDec extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s.ptrAdjust(-1)", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.%s.ptrAdjust(-1)", lhs, rhs);
			}
		}
	}
	
	public static class MPrefixExpressionPointer extends MPrefixExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s%s", this.operator, this.operand);
		}
	}
	
	public static class MPrefixExpressionPointerStar extends MPrefixExpression implements PlainString
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

		@Override
		public String toStringPlain() 
		{
			String plain = getPlainString(this.operand);
			
			if (plain != null)
			{
				return String.format("%s", plain);
			}
			else
			{
				String lhs = getStringLhs(this.operand);
				String rhs = getStringRhs(this.operand);

				return String.format("%s.%s", lhs, rhs);
			}
		}
	}
	
	public static class MPostfixExpressionBitfieldInc extends MPostfixExpression
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

	public static class MPostfixExpressionBitfieldDec extends MPostfixExpression
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

	public static class MPrefixExpressionBitfieldInc extends MPrefixExpression
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
	
	public static class MPrefixExpressionBitfieldDec extends MPrefixExpression
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
	
	public static class MPrefixExpressionBitfield extends MPrefixExpression
	{
		public MExpression set;
		
		@Override
		public String toString() 
		{
			return String.format("%s%s", this.operator, this.operand);
		}
	}
	
	public static class MCastExpression extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("(%s) %s", this.type, this.operand);
		}
	}

	public static class MCastExpressionToEnum extends MExpression
	{
		public MExpression operand;
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%s.fromValue(%s)", this.type, this.operand);
		}
	}
	
	public static class MDeleteObjectSingle extends MDeleteExpression
	{
		@Override
		public String toString() 
		{
			return String.format("DestructHelper.destruct(%s)", this.operand);
		}
	}
	
	public static class MDeleteObjectMultiple extends MDeleteExpression
	{
		@Override
		public String toString() 
		{
			return String.format("DestructHelper.destructArray(%s)", this.operand);
		}
	}
	
	public static class MEmptyExpression extends MExpression
	{
		@Override
		public String toString() 
		{
			return String.format("%s", "/* Empty expression */");
		}
	}
	
	public static class MNewArrayExpression extends MExpression
	{
		public List<MExpression> sizes = new ArrayList<MExpression>();
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("%sMulti.create(%s)", this.type, joinExpressions(this.sizes));
		}
	}
	
	public static class MNewArrayExpressionObject extends MExpression
	{
		public List<MExpression> sizes = new ArrayList<MExpression>();
		public String type;
		
		@Override
		public String toString() 
		{
			return String.format("PTR.newObjPtr(CreateHelper.allocateArray(%s.class, %s", this.type, joinExpressions(this.sizes));
		}
	}
	
	public static class MNewExpression extends MExpression
	{
		public String type;
		public MExpression argument;
		
		@Override
		public String toString() 
		{
			return String.format("PTR.new%sPtr(%s)", this.type, this.argument);
		}
	}
	
	public static class MNewExpressionObject extends MExpression
	{
		public String type;
		public List<MExpression> arguments = new ArrayList<MExpression>();
		
		@Override
		public String toString() 
		{
			return String.format("new %s(%s)", this.type, joinExpressions(this.arguments));
		}
	}
	
	public static class MAddItemCall extends MExpression
	{
		public MExpression operand;
		public int nextFreeStackId;
		
		@Override
		public String toString() 
		{
			return String.format("StackHelper.addItem(%s, %d, __stack)", this.operand, this.nextFreeStackId);
		}
	}
	
	public static class MOverloadedMethodUnary extends MExpression
	{
		public String method;
		public MExpression object;
		public boolean withNullArgForPostIncAndDec;
		
		@Override
		public String toString() 
		{
			return String.format("%s.%s(%s)", this.object, this.method, this.withNullArgForPostIncAndDec ? "null" : "");
		}
	}
	
	public static class MOverloadedFunctionUnary extends MExpression
	{
		public String function;
		public MExpression object;
		public boolean withNullArgForPostIncAndDec;
		
		@Override
		public String toString() 
		{
			return String.format("%s(%s%s)", this.function, this.object, this.withNullArgForPostIncAndDec ? ", null" : "");
		}
	}
	
	public static class MOverloadedMethodInfix extends MExpression
	{
		public String method;
		public MExpression object;
		public MExpression right;
		
		@Override
		public String toString() 
		{
			return String.format("%s.%s(%s)", this.object, this.method, this.right);
		}
	}
	
	public static class MOverloadedFunctionInfix extends MExpression
	{
		public String function;
		public MExpression left;
		public MExpression right;
		
		@Override
		public String toString() 
		{
			return String.format("%s(%s, %s)", this.function, this.left, this.right);
		}
	}
	
	public static class MOverloadedMethodFuncCall extends MExpression
	{
		public MExpression object;
		public List<MExpression> args = new ArrayList<MExpression>();
		
		@Override
		public String toString() 
		{
			return String.format("%s.opFunctionCall(%s)", this.object, joinExpressions(this.args));
		}
	}
	
	public static class MOverloadedMethodSubscript extends MExpression
	{
		public MExpression object;
		public MExpression subscript;
		
		@Override
		public String toString() 
		{
			return String.format("%s.opSubscript(%s)", this.object, this.subscript);
		}
	}
	
	public static class MOverloadedMethodCast extends MExpression
	{
		public MExpression object;
		public String name;
		
		@Override
		public String toString() 
		{
			return String.format("%s.%s()", this.object, this.name);
		}
	}
}
