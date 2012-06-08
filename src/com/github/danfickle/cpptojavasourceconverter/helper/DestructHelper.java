package com.github.danfickle.cpptojavasourceconverter.helper;

/**
 * A helper class to call destruct on arrays and lists of items.
 * @author DanFickle
 */
public class DestructHelper
{
	public static <T extends CppType> void destructArray(T[] arr)
	{
		for (T item : arr)
			item.destruct();
	}
	
	public static <T extends CppType> void destructArray(T[][] arr)
	{
		for (int i = 0; i < arr.length; i++)
			for (int j = 0; j < arr[i].length; j++)
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
