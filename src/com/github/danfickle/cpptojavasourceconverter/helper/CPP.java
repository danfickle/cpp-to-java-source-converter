package com.github.danfickle.cpptojavasourceconverter.helper;

import java.lang.reflect.Array;

public class CPP
{
	public static <T extends CppType<T>> void assignArray(T[] left, T[] right)
	{
		for (int i = 0; i < left.length; i++)
		{
			left[i].op_assign(right[i]);
		}
	}
	
	public static <T extends CppType<T>> void assignArray(T[][] left, T[][] right)
	{
		for (int i = 0; i < left.length; i++)
		{
			for (int j = 0; j < left[i].length; j++)
			{
				left[i][j].op_assign(right[i][j]);
			}
		}
	}

	public static <T> void assignBasicArray(T left, T right)
	{
		System.arraycopy(right, 0, left, 0, Array.getLength(left));
	}

	public static <T> void assignMultiArray(T left, T right)
	{
		for (int i = 0; i < Array.getLength(left); i++)
		{
			System.arraycopy(Array.get(right, i), 0, Array.get(left, i), 0, Array.getLength(Array.get(left, i)));
		}
	}
}
