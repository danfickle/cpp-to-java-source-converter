package com.github.danfickle.cpptojavasourceconverter.helper;

/**
 * Can act as a pointer or a value.
 * Just use valueOf or addressOf when passing
 * and assigning depending on whether you need it to be
 * a value or a pointer.
 */
public interface IInteger
{
	public IInteger ptrCopy();
	public IInteger ptrOffset(int cnt);
	public IInteger ptrAdjust(int cnt);
	public IInteger ptrPostInc();
	public IInteger ptrPostDec();
	public IInteger ptrAddressOf();
	public IInteger addressOf();
	public int ptrCompare();
	public int get();
	public int set(int value);
	public int postInc();
	public int postDec();
	public int[] deep();
}
