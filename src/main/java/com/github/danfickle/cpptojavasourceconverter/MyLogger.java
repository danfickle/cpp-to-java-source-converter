package com.github.danfickle.cpptojavasourceconverter;

class MyLogger
{
	static void log(String msg)
	{
		//System.out.println(msg);
	}

	static TranslationUnitContext ctx;
	
	static void logImportant(String msg)
	{
		System.err.println("In: " + ctx.currentFileName);
		System.err.println(msg);
	}
	
	static void exitOnError()
	{
		try 
		{
			throw new RuntimeException();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.exit(-1);
		}
	}
}
