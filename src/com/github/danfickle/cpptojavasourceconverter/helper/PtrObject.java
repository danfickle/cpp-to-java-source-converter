package com.github.danfickle.cpptojavasourceconverter.helper;

public class PtrObject<T> implements IPtrObject<T>
{
	private T val;
	
	private PtrObject(T value)
	{
		val = value;
	}
	
	public static <T> IPtrObject<T> valueOf(T value)
	{
		return new PtrObject<T>(value);
	}
	
	@Override
	public IPtrObject<T> ptrCopy()
	{
		return new PtrObject<T>(val);
	}

	@Override
	public IPtrObject<T> ptrOffset(int cnt)
	{
		if (cnt != 0)
			throw new IllegalArgumentException();
	
		return this;
	}

	@Override
	public IPtrObject<T> ptrAdjust(int cnt) 
	{
		if (cnt != 0)
			throw new IllegalArgumentException();
	
		return this;
	}

	@Override
	public IPtrObject<T> ptrPostInc()
	{
		throw new IllegalStateException();
	}

	@Override
	public IPtrObject<T> ptrPostDec()
	{
		throw new IllegalStateException();
	}

	@Override
	public IPtrObject<IPtrObject<T>> ptrAddressOf() 
	{
		return PtrObject.valueOf((IPtrObject<T>) this);
	}

	@Override
	public T get() 
	{
		return val;
	}

	@Override
	public T set(T value) 
	{
		val = value;
		return value;
	}

	@Override
	public int ptrCompare() 
	{
		return 0;
	}

	@Override
	public T[] deep() 
	{
		throw new IllegalStateException();
	}
}
