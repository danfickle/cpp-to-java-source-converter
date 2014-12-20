package com.github.danfickle.cpptojavasourceconverter.helper;

import java.lang.reflect.Array;

/**
 * A helper class to allocate and initialize one and two dimension
 * arrays.
 * @author DanFickle
 */
public class CreateHelper 
{
	/**
	 * A helper method to initialize one dimension arrays with the 
	 * default constructor.
	 */
	private static void initializeArray(CppType[] arr, Class<? extends CppType> type, int number)
	{
		for (int i = 0; i < arr.length; i++)
		{
			try {
				arr[i] = type.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
				System.exit(-1);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/**
	 * A helper method to initialize two dimension arrays with the 
	 * default constructor.
	 */
	private static void initializeArray(CppType[][] arr, Class<? extends CppType> type, int dim1, int dim2)
	{
		for (int i = 0; i < arr.length; i++)
		{
			for (int j = 0; j < arr[i].length; j++)
			{
				try {
					arr[i][j] = type.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
					System.exit(-1);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}
	
	/**
	 * A helper method to allocate one dimension arrays with the 
	 * default constructor.
	 */
	public static <T extends CppType> T[] allocateArray(Class<? extends CppType> type, int cnt)
	{
		@SuppressWarnings("unchecked")
		T[] ret = (T[]) Array.newInstance(type, cnt);
		initializeArray(ret, type, cnt);
		return ret;
	}

	/**
	 * A helper method to allocate two dimension arrays with the 
	 * default constructor.
	 */
	public static <T extends CppType> T[][] allocateArray(Class<? extends CppType> type, int dim1, int dim2)
	{
		@SuppressWarnings("unchecked")
		T[][] ret = (T[][]) Array.newInstance(type, dim1, dim2);
		initializeArray(ret, type, dim1, dim2);
		return ret;
	}
}
