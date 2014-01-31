package com.github.danfickle.cpptojavasourceconverter.helper;

import org.joor.Reflect;
import org.joor.ReflectException;

// NOTE: This is an experimental class to see if we can
// call operators, etc at runtime in a template
// to avoid doing code generation.
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

	@SuppressWarnings("unchecked")
	public static <T> T doUnaryOp(Object var, UnaryOp op)
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
			int val = ((IInteger) var).get();

			switch (op)
			{
			case opAmper:
				return Reflect.on(var).call("addressOf").get();
			case opComplement:
				return (T) (Integer) (~val);
			case opLogicalNot:
				return (T) (Boolean) (val != 0);
			case opMinus:
				return (T) (Integer) (-val);
			case opPlus:
				return (T) (Integer) (+val);
			case opPostDecrement:
				return Reflect.on(var).call("postDec").get();
			case opPostIncrement:
				return Reflect.on(var).call("postInc").get();
			case opPreDecrement:
				return Reflect.on(var).call("set", val - 1).get();
			case opPreIncrement:
				return Reflect.on(var).call("set", val + 1).get();
			case opStar:
				break;
			}
		}
		// More else ifs to handle other built in types here.
		
		return null;
	}
}
