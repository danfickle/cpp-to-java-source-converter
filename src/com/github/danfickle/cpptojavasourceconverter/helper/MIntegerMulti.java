package com.github.danfickle.cpptojavasourceconverter.helper;

public class MIntegerMulti implements PtrLike<MIntegerMulti, Integer> 
{
	private final int[] val;
	private int currentOffset;
	
	private MIntegerMulti(int[] arr, int offset)
	{
		val = arr;
		currentOffset = offset;
	}
	
	public static MIntegerMulti valueOf(int[] arr, int offset)
	{
		return new MIntegerMulti(arr, offset);
	}
	
	@Override
	public MIntegerMulti addressOf() 
	{
		return this;
	}

	@Override
	public MIntegerMulti copy() 
	{
		return new MIntegerMulti(val, currentOffset);
	}

	@Override
	public Integer get()
	{
		return val[currentOffset];
	}

	@Override
	public MIntegerMulti set(Integer val) 
	{
		this.val[currentOffset] = val;
		return this;
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
	public Integer postInc() 
	{
		return val[currentOffset]++;
	}

	@Override
	public Integer postDec() 
	{
		return val[currentOffset]--;
	}

	@Override
	public MIntegerMulti ptrCopy()
	{
		// must make a copy of currentOffset like real pointers
		return new MIntegerMulti(val, currentOffset);
	}
}
