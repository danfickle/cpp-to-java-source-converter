package com.github.danfickle.cpptojavasourceconverter.helper;

public class MInteger implements IInteger
{
	private int m_value;
	
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

	@Override
	public IPtrObject<IInteger> ptrAddressOf()
	{
		return PtrObject.valueOf((IInteger) this);
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
	public int get()
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
	public int set(int value)
	{
		m_value = value;
		return m_value;
	}
	
	/**
	 * val++;
	 * val.postInc();
	 */
	@Override
	public int postInc()
	{
		return m_value++;
	}

	/**
	 * val--;
	 * val.postDec();
	 */
	@Override
	public int postDec()
	{
		return m_value--;
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

	@Override
	public int ptrCompare() 
	{
		return 1;
	}

	@Override
	public int[] deep()
	{
		throw new IllegalStateException();
	}
}
