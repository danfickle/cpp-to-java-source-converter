package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IType;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;

class GlobalCtx
{
	// Items which need to be persisted accross translation
	// units should go here.
	
	int anonCount = 0;
	
	// A set of qualified names containing the bitfields...
	Set<String> bitfields = new HashSet<String>();
	
	// Maps the complete C++ name (which may have been partially generated)
	// to a CppDeclaration model.
	Map<String, CppDeclaration> declarations = new HashMap<String, CppDeclaration>();

	static class ITypeName
	{
		final IType tp;
		final String nm;
		
		ITypeName(IType t, String n)
		{
			tp = t;
			nm = n;
		}
	}
	
	// Maps the type to a complete name.
	// Note, this is not a hash map as IType does
	// not implement hash method. Therefore it must
	// be iterated and checked with IType::isSameType method.
	List<ITypeName> types = new ArrayList<ITypeName>();

	// This contains a mapping from filenames to a generated global
	// declaration for that file which will house global items such
	// as variables and functions.
	Map<String, CppClass> fileClasses = new HashMap<String, CppClass>();
}
