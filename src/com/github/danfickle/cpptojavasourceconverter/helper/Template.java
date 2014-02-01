package com.github.danfickle.cpptojavasourceconverter.helper;

import org.joor.Reflect;
import org.joor.ReflectException;

// NOTE: This is an experimental class to see if we can
// call operators, etc at runtime in a template
// to avoid doing code generation.
// TODO: All of this class will be terribly slow!
public class Template
{
	public static enum UnaryOp
	{
		opAmper,
		opMinus,
		opPlus,
		opLogicalNot,
		opPostIncrement,
		opPostDecrement,
		opPreIncrement,
		opPreDecrement,
		opStar,
		opComplement;
	}

	public static enum BinaryOp
	{
		opAssign;
	}
	
	public static Object doBinaryOp(Object a, Object b, BinaryOp op)
	{
		if (a instanceof CppType ||
			b instanceof CppType)
		{
			try
			{
				// 1. First try method a.op(b)
				return Reflect.on(a).call(op.toString(), b).get();
			}
			catch (ReflectException re)
			{
				// 2. Then try function op(a, b)
				return Reflect.on(GlobalOpOverloads.class).call(op.toString(), a, b).get();						
			}
		}
		else if (a instanceof IInteger)
		{
			switch (op)
			{
			case opAssign:
				((IInteger) a).set((Integer) getValue(b));
			// TOOD: Other binary operators.
			}
		}

		return null;
	}
	
	private static Object getValue(Object b) 
	{
		if (b instanceof IInteger)
			return ((IInteger) b).get();

		// TODO: Object types via cast operator, basic types via get, enums, etc.
		
		return null;
	}

	public static Object doUnaryOp(Object var, UnaryOp op)
	{
		if (var instanceof CppType)
		{
			if (op == UnaryOp.opPostIncrement ||
				op == UnaryOp.opPostDecrement)
			{
				// These two operators have a dummy argument to distinguish
				// them from their prefix alternatives.
				try
				{
					// Try member overloading first.
					return Reflect.on(var).call(op.toString(), (Object) null).get();
				}
				catch (ReflectException re)
				{
					// If that fails it must be a function overload.
					return Reflect.on(GlobalOpOverloads.class).call(op.toString(), var, null).get();
				}				
			}
			else
			{
				try
				{
					return Reflect.on(var).call(op.toString()).get();
				}
				catch (ReflectException re)
				{
					return Reflect.on(GlobalOpOverloads.class).call(op.toString(), var).get();
				}
			}
		}
		else if (var instanceof IInteger)
		{
			IInteger down = (IInteger) var;
			int val = down.get();

			// TODO: Pointers and arrays.
			switch (op)
			{
			case opAmper:
				return down.addressOf();
			case opPostDecrement:
				return MInteger.valueOf(down.postDec());
			case opPostIncrement:
				return MInteger.valueOf(down.postInc());
			case opComplement:
				return MInteger.valueOf(~val);
			case opLogicalNot:
				return MInteger.valueOf(val == 0 ? 1 : 0);
			case opMinus:
				return MInteger.valueOf(-val);
			case opPlus:
				return MInteger.valueOf(+val);
			case opPreDecrement:
				down.set(val - 1);
				return down;
			case opPreIncrement:
				down.set(val + 1);
				return down;
			case opStar:
				return MInteger.valueOf(val);
			}
		}
		// TODO: More else ifs to handle other built in types and enums here.

		return null;
	}
}
