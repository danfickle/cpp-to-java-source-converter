package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.danfickle.cpptojavasourceconverter.models.DeclarationModels.CppClass;
import com.github.danfickle.cpptojavasourceconverter.models.DeclarationModels.CppDeclaration;

class GlobalCtx
{
	// Items which need to be persisted accross translation
	// units should go here.
	
	int anonCount = 0;
	
	// A set of qualified names containing the bitfields...
	Set<String> bitfields = new HashSet<String>();
	
	// Maps the type to a declaration.
	// Note, this is not a hash map as IType does
	// not implement hash method. Therefore it must
	// be iterated and checked with IType::isSameType method.
	// Note: Generated declarations are not required to go in 
	// here as they are not called directly.
	List<CppDeclaration> decls = new ArrayList<CppDeclaration>();

	// This contains a mapping from filenames to a generated global
	// declaration for that file which will house global items such
	// as variables and functions.
	Map<String, CppClass> fileClasses = new HashMap<String, CppClass>();
}
