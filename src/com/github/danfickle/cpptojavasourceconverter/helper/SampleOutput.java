package com.github.danfickle.cpptojavasourceconverter.helper;

class SampleOutput
{
	void ptrNumberSample()
	{
		IInteger i = MInteger.valueOf(0); // int i;
		IInteger j = i.addressOf();  // int * j = &i;
		IInteger k = j.ptrCopy();    // int * k = j;
		IInteger l = MIntegerMulti.valueOf(new int[24], 0); // int l[24]; 
		IInteger m = l.ptrOffset(2).addressOf(); // int * m = &l[2];
		IInteger n = l.ptrCopy();  // int * n = l;
		IInteger o = MInteger.valueOf(l.ptrOffset(5).get());   //int o = l[5]; or int o = *(l + 5);
	}
}
