package com.github.danfickle.cpptojavasourceconverter.helper;

public interface CppType<T>
{
	void destruct();
	T copy();
}
