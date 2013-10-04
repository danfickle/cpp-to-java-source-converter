package com.github.danfickle.cpptojavasourceconverter.helper;

public class PtrObjectMulti<T> implements IPtrObject<T> 
{
	private final T[] val;
	private int currentOffset;
	
	@SuppressWarnings("unchecked")
	private PtrObjectMulti(int dim1)
	{
		val = (T[]) new Object[dim1];
	}
	
	private PtrObjectMulti(T[] value, int offset)
	{
		val = value;
		currentOffset = offset;
	}
	
	public static <T> PtrObjectMulti<T> create(int dim1)
	{
		return new PtrObjectMulti<T>(dim1);
	}
	
	@Override
	public IPtrObject<T> ptrCopy() 
	{
		return new PtrObjectMulti<T>(val, currentOffset);
	}

	@Override
	public IPtrObject<T> ptrOffset(int cnt) 
	{
		return new PtrObjectMulti<T>(val, currentOffset + cnt);
	}

	@Override
	public IPtrObject<T> ptrAdjust(int cnt) {
		currentOffset += cnt;
		return this;
	}

	@Override
	public IPtrObject<T> ptrPostInc() 
	{
		int temp = currentOffset++;
		return new PtrObjectMulti<T>(val, temp);
	}

	@Override
	public IPtrObject<T> ptrPostDec() 
	{
		int temp = currentOffset--;
		return new PtrObjectMulti<T>(val, temp);
	}

	@Override
	public IPtrObject<IPtrObject<T>> ptrAddressOf() 
	{
		return PtrObject.valueOf((IPtrObject<T>) this);
	}

	@Override
	public int ptrCompare()
	{
		return currentOffset;
	}

	@Override
	public T get() 
	{
		return val[currentOffset];
	}

	@Override
	public T set(T value) 
	{
		val[currentOffset] = value;
		return value;
	}

	@Override
	public T[] deep() 
	{
		return val;
	}
}
