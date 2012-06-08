package com.github.danfickle.cpptojavasourceconverter.helper;

/**
 * A helper class to call destruct on arrays and lists of items.
 * Note: Arrays should be destructed in reverse order according to 
 * C++ docs.
 * @author DanFickle
 */
public class DestructHelper
{
	public static <T extends CppType> void destructArray(T[] arr)
	{
		for (int i = arr.length - 1; i >= 0; i--)
			arr[i].destruct();
	}
	
	public static <T extends CppType> void destructArray(T[][] arr)
	{
		for (int i = arr.length - 1; i >= 0; i--)
			for (int j = arr[i].length - 1; j >= 0; j--)
				arr[i][j].destruct();
	}

	public static void destructItems(CppType item)
	{
		item.destruct();
	}

	public static void destructItems(CppType item, CppType item2)
	{
		item.destruct();
		item2.destruct();
	}

	public static void destructItems(CppType item, CppType item2, CppType item3)
	{
		item.destruct();
		item2.destruct();
		item3.destruct();
	}

	public static void destructItems(CppType item, CppType item2, CppType item3, CppType item4)
	{
		item.destruct();
		item2.destruct();
		item3.destruct();
		item4.destruct();
	}

	public static void destructItems(CppType...items)
	{
		for (CppType item : items)
			item.destruct();
	}
}
