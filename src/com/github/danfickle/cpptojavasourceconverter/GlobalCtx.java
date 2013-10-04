package com.github.danfickle.cpptojavasourceconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IType;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppDeclaration;

class GlobalCtx
{
	// Items which need to be persisted accross translation
	// units should go here.
	
	int anonClassCount = 0;
	int anonEnumCount  = 0;
	
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
}
