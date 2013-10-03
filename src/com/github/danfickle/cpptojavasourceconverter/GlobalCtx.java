package com.github.danfickle.cpptojavasourceconverter;

import java.util.HashMap;

class GlobalCtx
{
	// Items which need to be persisted accross translation
	// units should go here.
	
	int anonClassCount = 0;
	int anonEnumCount  = 0;
	
	HashMap<String, String> anonEnumMap = new HashMap<String, String>();
}
