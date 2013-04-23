package com.github.danfickle.cpptojavasourceconverter.helper;

public class MInteger implements PtrLike<MInteger, Integer>
{
	int m_value;
	
	private MInteger(int value)
	{
		m_value = value;
	}
	
	/**
	 * int val = 5;
	 * MInteger val = MInteger.valueOf(5);
	 */
	public static MInteger valueOf(int value)
	{
		return new MInteger(value);
	}
	
	/**
	 * int * ptr = &val;
	 * MInteger ptr = val.addressOf();
	 */
	@Override
	public MInteger addressOf()
	{
		return this;
	}
	
	/**
	 * short shrt = (short) val;
	 * MShort shrt = MShort.valueOf(val.get()); 
	 */
	@Override
	public Integer get()
	{
		return m_value;
	}
	
	/**
	 * (*ptr) = 5;
	 * ptr.set(5);
	 * 
	 * (ptr[0]) = 4;
	 * ptr.set(4);
	 */
	@Override
	public MInteger set(Integer value)
	{
		m_value = value;
		return this;
	}
	
	/**
	 * val++;
	 * val.postInc();
	 */
	@Override
	public Integer postInc()
	{
		return m_value++;
	}

	/**
	 * val--;
	 * val.postDec();
	 */
	@Override
	public Integer postDec()
	{
		return m_value--;
	}
	
	/**
	 * Copy when passing or assigning unless its being used
	 * as a pointer. If using as a pointer use the addressOf function.
	 *
	 * int * ptr = &val; 
	 * MInteger ptr = val.addressOf();
	 *
	 * int val2 = val;
	 * MInteger val2 = val.copy();
	 */
	@Override
	public MInteger copy()
	{
		return new MInteger(m_value);
	}
	
	
	/*
	 * Pointer operation methods.
	 */
	@Override
	public MInteger ptrPostInc() { throw new IllegalStateException(); }

	@Override
	public MInteger ptrPostDec() { throw new IllegalStateException(); }

	@Override
	public MInteger ptrAdjust(int cnt) 
	{
		if (cnt != 0)
			throw new IllegalArgumentException();

		return this;
	}

	@Override
	public MInteger ptrOffset(int cnt)
	{
		if (cnt != 0)
			throw new IllegalArgumentException();
	
		return this;
	}

	@Override
	public MInteger ptrCopy()
	{
		return this;
	}
}
