package com.github.danfickle.cpptojavasourceconverter.helper;

import java.lang.reflect.Array;

public class CPP
{
	public static <T extends CppType<T>> void assignArray(T[] left, T[] right)
	{
		for (int i = 0; i < left.length; i++)
		{
			left[i].opAssign(right[i]);
		}
	}
	
	public static <T extends CppType<T>> void assignArray(T[][] left, T[][] right)
	{
		for (int i = 0; i < left.length; i++)
		{
			for (int j = 0; j < left[i].length; j++)
			{
				left[i][j].opAssign(right[i][j]);
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
	
	public static <T extends CppType<T>> T[] copyArray(T[] right)
	{
		T[] ret = right.clone();
		
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = right[i].copy();
		}
		return ret;
	}
	
	public static <T extends CppType<T>> T[][] copyArray(T[][] right)
	{
		T[][] ret = right.clone();
		
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = right[i].clone();
			
			for (int j = 0; j < ret[i].length; j++)
			{
				ret[i][j] = right[i][j].copy();
			}
		}
		return ret;
	}

	public static Object copyBasicArray(Object right)
	{
		if (right instanceof boolean[]) return ((boolean[]) right).clone();
		if (right instanceof byte[]) return ((byte[]) right).clone();
		if (right instanceof char[]) return ((char[]) right).clone();
		if (right instanceof short[]) return ((short[]) right).clone();
		if (right instanceof int[]) return ((int[]) right).clone();
		if (right instanceof long[]) return ((long[]) right).clone();
		if (right instanceof float[]) return ((float[]) right).clone();
		if (right instanceof double[]) return ((double[]) right).clone();
		else
			throw new IllegalArgumentException();
	}

	/**
	 * Based on: http://stackoverflow.com/questions/419858/how-to-deep-copy-an-irregular-2d-array
	 * @author Chii
	 */
	public static Object copyMultiArray(Object right)
	{
		int arrLength = Array.getLength(right);
		Class<?> type = right.getClass().getComponentType();
		Object newInnerArray = Array.newInstance(type, arrLength);

		for (int i = 0; i < arrLength; i++) {
			Object elem = copyBasicArray(Array.get(right, i));
			Array.set(newInnerArray, i, elem);
		}
		return newInnerArray;
	}
}
