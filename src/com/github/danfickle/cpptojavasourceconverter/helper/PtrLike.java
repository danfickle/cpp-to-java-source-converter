package com.github.danfickle.cpptojavasourceconverter.helper;

/**
 * Can act as a pointer or a value.
 * Just use copy or addressOf when passing
 * and assigning depending on whether you need it to be
 * a value or a pointer.
 */
public interface PtrLike<T, K>
{
	public T addressOf();
	public T copy();
	public K get();
	public T set(K val);
	public T ptrPostInc();
	public T ptrPostDec();
	public T ptrAdjust(int cnt);
	public T ptrOffset(int cnt);
	public K postInc();
	public K postDec();
	public T ptrCopy();
}
