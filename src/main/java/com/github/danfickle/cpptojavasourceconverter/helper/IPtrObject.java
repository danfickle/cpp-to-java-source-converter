package com.github.danfickle.cpptojavasourceconverter.helper;

public interface IPtrObject<T>
{
	public IPtrObject<T> ptrCopy();
	public IPtrObject<T> ptrOffset(int cnt);
	public IPtrObject<T> ptrAdjust(int cnt);
	public IPtrObject<T> ptrPostInc();
	public IPtrObject<T> ptrPostDec();
	public IPtrObject<IPtrObject<T>> ptrAddressOf();
	public int ptrCompare();
	public T get();
	public T set(T value);
	public T[] deep();
}
