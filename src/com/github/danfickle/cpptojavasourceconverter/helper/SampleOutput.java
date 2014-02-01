package com.github.danfickle.cpptojavasourceconverter.helper;

import com.github.danfickle.cpptojavasourceconverter.helper.Template.UnaryOp;

class SampleOutput
{
	@SuppressWarnings("unused")
	void ptrNumberSample()
	{
		IInteger i = MInteger.valueOf(0); // int i;
		IInteger j = i.addressOf();  // int * j = &i;
		IInteger k = j.ptrCopy();    // int * k = j;
		IInteger l = MIntegerMulti.create(24); // int l[24]; 
		IInteger m = l.ptrOffset(2).addressOf(); // int * m = &l[2];
		IInteger n = l.ptrCopy();  // int * n = l;
		IInteger o = MInteger.valueOf(l.ptrOffset(5).get());   //int o = l[5]; or int o = *(l + 5);
	}
	
	@SuppressWarnings("unused")
	void templateSample()
	{
		IInteger i = MInteger.valueOf(10); // int i = 10;
		Object j = i; // Replicate template conditions.
		Object k = Template.doUnaryOp(j, UnaryOp.opMinus);
		Object l = Template.doUnaryOp(k, UnaryOp.opAmper);
	}
}
