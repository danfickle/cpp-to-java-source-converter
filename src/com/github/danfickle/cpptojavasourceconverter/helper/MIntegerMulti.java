package com.github.danfickle.cpptojavasourceconverter.helper;

public class MIntegerMulti implements IInteger
{
	private final int[] val;
	private int currentOffset;
	
	private MIntegerMulti(int[] arr, int offset)
	{
		val = arr;
		currentOffset = offset;
	}
	
	public static IInteger valueOf(int[] arr, int offset)
	{
		return new MIntegerMulti(arr, offset);
	}
	
	@Override
	public MIntegerMulti addressOf() 
	{
		return this;
	}

	@Override
	public int get()
	{
		return val[currentOffset];
	}

	@Override
	public int set(int value) 
	{
		val[currentOffset] = value;
		return value;
	}

	@Override
	public MIntegerMulti ptrPostInc()
	{
		int temp = currentOffset++;
		return new MIntegerMulti(val, temp);
	}

	@Override
	public MIntegerMulti ptrPostDec() 
	{
		int temp = currentOffset++;
		return new MIntegerMulti(val, temp);
	}

	@Override
	public MIntegerMulti ptrAdjust(int cnt) 
	{
		currentOffset += cnt;
		return null;
	}

	@Override
	public MIntegerMulti ptrOffset(int cnt) 
	{
		return new MIntegerMulti(val, currentOffset + cnt);
	}

	@Override
	public int postInc() 
	{
		return val[currentOffset]++;
	}

	@Override
	public int postDec() 
	{
		return val[currentOffset]--;
	}

	@Override
	public MIntegerMulti ptrCopy()
	{
		// must make a copy of currentOffset like real pointers
		return new MIntegerMulti(val, currentOffset);
	}

	@Override
	public IInteger ptrAddressOf() 
	{
		return this;
	}
}
