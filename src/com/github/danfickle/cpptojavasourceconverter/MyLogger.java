package com.github.danfickle.cpptojavasourceconverter;

class MyLogger
{
	static void log(String msg)
	{
		System.out.println(msg);
	}

	static void logImportant(String msg)
	{
		System.err.println(msg);
	}
	
	static void exitOnError()
	{
		System.exit(-1);
	}
}
