package com.github.danfickle.cpptojavasourceconverter.helper;

public class StackHelper 
{
	public static <T> T addItem(T item, int stackId, Object[] arr)
	{
		arr[stackId] = item;
		return item;
	}

	private static void cleanup(Object[] arr, int until)
	{
		for (int i = arr.length - 1; i >= until; i--)
		{
			if (arr[i] instanceof CppType)
			{
				((CppType) arr[i]).destruct();
				arr[i] = null;
			}
			else if (arr[i] instanceof CppType[][])
			{
				DestructHelper.destructArray((CppType[][]) arr[i]);
				arr[i] = null;
			}
			else if (arr[i] instanceof CppType[])
			{
				DestructHelper.destructArray((CppType[]) arr[i]);
				arr[i] = null;
			}
		}
	}
	
	public static <T> T cleanup(T ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	public static boolean cleanup(boolean ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	public static byte cleanup(byte ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	public static char cleanup(char ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	
	public static short cleanup(short ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	public static int cleanup(int ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	public static long cleanup(long ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	public static float cleanup(float ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}

	public static double cleanup(double ret, Object[] arr, int until)
	{
		cleanup(arr, until);
		return ret;
	}
}


