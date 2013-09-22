package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

public class TypeHelpers 
{
	enum TypeEnum
	{
		NUMBER, 
		BOOLEAN,
		CHAR,
		VOID,
		POINTER,
		OBJECT,
		ARRAY,
		ANY,
		REFERENCE,
		OTHER,
		ENUMERATION,
		UNKNOWN,
		FUNCTION;
	}
	
	static TypeEnum expressionGetType(IASTExpression expr) throws DOMException
	{
		if (expr == null || expr.getExpressionType() == null)
			return TypeEnum.BOOLEAN; // FIXME..
		
		return getTypeEnum(expr.getExpressionType());
	}
	
	static boolean isObjectPtr(IType type) throws DOMException
	{
		if (type instanceof IPointerType)
		{
			IPointerType pointer = (IPointerType) type;
			
			if (getTypeEnum(pointer.getType()) == TypeEnum.OBJECT)
				return true;
		}
		return false;
	}
	
	/**
	 * Determines if a type will turn into a pointer.
	 */
	static boolean isEventualPtr(IType type)
	{
		if (type instanceof IPointerType)
		{
			IPointerType pointer = (IPointerType) type;
			int ptrCount = 1;
			
			for (IType ptr = pointer.getType(); ptr instanceof IPointerType; ptr = ((IPointerType) ptr).getType())
				ptrCount++;

			if (ptrCount >= 2)
				return true;
			else if (pointer.getType() instanceof IBasicType || (pointer.getType() instanceof ITypedef && ((ITypedef)(pointer.getType())).getType() instanceof IBasicType))
				return true;
			else
				return false;
		}
		else if (type instanceof IArrayType)
		{
			return true;
		}
		
		return false;
	}
	
	static boolean isEventualRef(IType type)
	{
		if (type instanceof ICPPReferenceType)
		{
			ICPPReferenceType ref = (ICPPReferenceType) type;

			if ((ref.getType() instanceof IQualifierType) || ref.getType() instanceof ICPPClassType)
				return false;
			else
				return true;
		}

		return false;
	}
	
	/**
	 * Attempts to convert a CDT type to a JDT type.
	 */
	static String cppToJavaType(IType type, boolean retValue, boolean needBoxed) throws DOMException
	{
		if (type instanceof IBasicType)
		{
			// Primitive type - int, bool, char, etc...
			IBasicType basic = (IBasicType) type;
			
			//if (needBoxed)
				return evaluateSimpleTypeBoxed(basic.getType(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
			//return evaluateSimpleType(basic.getType(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
		}
		else if (type instanceof IArrayType)
		{
			IArrayType array = (IArrayType) type;
			return cppToJavaType(array.getType()) + "Multi";
		}
		else if (type instanceof ICompositeType)
		{
			ICompositeType comp = (ICompositeType) type;
			String simple = getSimpleType(comp.getName());

			//printerr(comp.getClass().getCanonicalName());
			
//			if (type instanceof ICPPTemplateInstance)
//			{
//				ICPPTemplateInstance template = (ICPPTemplateInstance) type;
//				print("template instance");
//
//				ParameterizedType param = ast.newParameterizedType(simple);
//				List<Type> list = getTypeParams(template.getTemplateArguments());
//				param.typeArguments().addAll(list);
//				return param;
//			}
//			else
			{
				return simple;
			}
		}
		else if (type instanceof IPointerType)
		{
			IPointerType pointer = (IPointerType) type;
			int ptrCount = 1;

			for (IType ptr = pointer.getType(); ptr instanceof IPointerType; ptr = ((IPointerType) ptr).getType())
				ptrCount++;

			if (ptrCount == 2)
			{
				return "Ptr" + cppToJavaType(pointer.getType());
			}
			else if (pointer.getType() instanceof IBasicType || (pointer.getType() instanceof ITypedef && ((ITypedef)(pointer.getType())).getType() instanceof IBasicType))
			{
				IBasicType basic;
				if (pointer.getType() instanceof ITypedef)
					basic = ((IBasicType)((ITypedef) pointer.getType()).getType());
				else
					basic = (IBasicType) pointer.getType();

				String basicStr = evaluateSimpleTypeBoxed(basic.getType(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
				String simpleType = basicStr;
				return simpleType;
			}
			else if (ptrCount == 1)
				return cppToJavaType(pointer.getType());
			else
			{
				MyLogger.logImportant("Too many pointer indirections");
				MyLogger.exitOnError();
			}
		}
		else if (type instanceof ICPPReferenceType)
		{
			ICPPReferenceType ref = (ICPPReferenceType) type;

			if ((ref.getType() instanceof IQualifierType) || ref.getType() instanceof ICPPClassType /* && ((IQualifierType) ref.getType()).isConst()) */ || retValue)
				return cppToJavaType(ref.getType(), retValue, false);
			else if (ref.getType() instanceof IBasicType || (ref.getType() instanceof ITypedef && ((ITypedef) ref.getType()).getType() instanceof IBasicType))
			{
				IBasicType basic;
				if (ref.getType() instanceof ITypedef)
					basic = ((IBasicType)((ITypedef) ref.getType()).getType());
				else
					basic = (IBasicType) ref.getType();

				String basicStr = evaluateSimpleTypeBoxed(basic.getType(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
				String simpleType = "Ref" + basicStr;
				return simpleType;
			}
//			else
//			{
//				ParameterizedType paramType = ast.newParameterizedType(jast.newType("Ref"));    		  
//				paramType.typeArguments().add(cppToJavaType(ref.getType(), false, true));  		  
//				return paramType;
//			}
		}
		else if (type instanceof IQualifierType)
		{
			IQualifierType qual = (IQualifierType) type;
			return cppToJavaType(qual.getType(), retValue, needBoxed);
		}
		else if (type instanceof IProblemBinding)
		{
			IProblemBinding prob = (IProblemBinding) type;
			MyLogger.logImportant("PROBLEM:" + prob.getMessage() + prob.getFileName() + prob.getLineNumber());

			return "PROBLEM__";
		}
		else if (type instanceof ITypedef)
		{
			ITypedef typedef = (ITypedef) type;
			return cppToJavaType(typedef.getType(), retValue, needBoxed);
		}
		else if (type instanceof IEnumeration)
		{
			IEnumeration enumeration = (IEnumeration) type;
			return getSimpleType(enumeration.getName());
		}
		else if (type instanceof IFunctionType)
		{
			IFunctionType func = (IFunctionType) type;
			return "FunctionPointer";
		}
		else if (type instanceof IProblemType)
		{
			IProblemType prob = (IProblemType)type; 
			MyLogger.logImportant("Problem type: " + prob.getMessage());
			//exitOnError();
			return "PROBLEM";
		}
		else if (type instanceof ICPPTemplateTypeParameter)
		{
			ICPPTemplateTypeParameter template = (ICPPTemplateTypeParameter) type;
			MyLogger.logImportant("template type");
			return template.toString();
		}
		else if (type != null)
		{
			MyLogger.logImportant("Unknown type: " + type.getClass().getCanonicalName() + type.toString());
			MyLogger.exitOnError();
		}
		return null;
	}
	
	static String cppToJavaType(IType type) throws DOMException
	{
		return cppToJavaType(type, false, false);
	}
	
	/**
	 * Attempts to get the boxed type for a primitive type.
	 * We need the boxed type to use for Ptr and Ref parametized
	 * types.
	 */
	static String evaluateSimpleTypeBoxed(int type, boolean isShort, boolean isLongLong, boolean isUnsigned)
	{
		switch (type)
		{
		case IASTSimpleDeclSpecifier.t_char:
			MyLogger.log("char");
			return "Byte";
		case IASTSimpleDeclSpecifier.t_int:
			MyLogger.log("int");
			if (isShort)
				return "Short";
			else if (isLongLong)
				return "Long";
			else
				return "MInteger";
		case IASTSimpleDeclSpecifier.t_float:
			MyLogger.log("float");
			return "Float";
		case IASTSimpleDeclSpecifier.t_double:
			MyLogger.log("double");
			return "Double";
		case IASTSimpleDeclSpecifier.t_unspecified:
			MyLogger.log("unspecified");
			if (isUnsigned)
				return "Integer";
			else
				return null;
		case IASTSimpleDeclSpecifier.t_void:
			MyLogger.log("void");
			return "Void";
		case ICPPASTSimpleDeclSpecifier.t_bool:
			MyLogger.log("bool");
			return "Boolean";
		case ICPPASTSimpleDeclSpecifier.t_wchar_t:
			MyLogger.log("wchar_t");
			return "Character";
		default:
			return null;
		}
	}
	
	/**
	 * Returns the Java simple type for the corresponding C++ type. 
	 */
	static String evaluateSimpleType(int type, boolean isShort, boolean isLongLong, boolean isUnsigned)
	{
		switch (type)
		{
		case IASTSimpleDeclSpecifier.t_char:
			MyLogger.log("char");
			return "byte";
		case IASTSimpleDeclSpecifier.t_int:
			MyLogger.log("int");
			if (isShort)
				return "short";
			else if (isLongLong)
				return "long";
			else
				return "int";
		case IASTSimpleDeclSpecifier.t_float:
			MyLogger.log("float");
			return "float";
		case IASTSimpleDeclSpecifier.t_double:
			MyLogger.log("double");
			return "double";
		case IASTSimpleDeclSpecifier.t_unspecified:
			MyLogger.log("unspecified");
			if (isUnsigned)
				return "int";
			else
				return "int";
		case IASTSimpleDeclSpecifier.t_void:
			MyLogger.log("void");
			return "void";
		case ICPPASTSimpleDeclSpecifier.t_bool:
			MyLogger.log("bool");
			return "boolean";
		case ICPPASTSimpleDeclSpecifier.t_wchar_t:
			MyLogger.log("wchar_t");
			return "char";
		default:
			return null;
		}
	}
	
	/**
	 * Gets a simple type. Eg. WebCore::RenderObject becomes
	 * RenderObject.
	 */
	static String getSimpleType(String qualifiedType)
	{
		String ret;

		if (qualifiedType.contains("::"))
		{
			ret = qualifiedType.substring(qualifiedType.lastIndexOf("::"));
		}
		else
			ret = qualifiedType;

		if (ret.isEmpty())
			return "MISSING";
		else
			return ret;
	}

	/**
	 * Replaces C++ names with Java compatible names for functions.
	 * You may need to add missing operators.
	 */
	static String normalizeName(String name)
	{
		String replace;
		if (name.startsWith("operator"))
		{
			if (name.equals("operator +="))
				replace = "op_plus_assign";
			else if (name.equals("operator =="))
				replace = "equals";
			else if (name.equals("operator -="))
				replace = "op_minus_assign";
			else if (name.equals("operator !="))
				replace = "op_not_equals";
			else if (name.equals("operator !"))
				replace = "op_not";
			else if (name.equals("operator ->"))
				replace = "op_access";
			else if (name.equals("operator |"))
				replace = "op_or";
			else if (name.equals("operator -"))
				replace = "op_minus";
			else if (name.equals("operator +"))
				replace = "op_plus";
			else if (name.equals("operator *"))
				replace = "op_star";
			else if (name.equals("operator &"))
				replace = "op_addressof";
			else if (name.equals("operator []"))
				replace = "op_access";
			else if (name.equals("operator new[]"))
				replace = "op_new_array";
			else if (name.equals("operator delete[]"))
				replace = "op_delete_array";
			else if (name.equals("operator ="))
				replace = "op_assign";
			else if (name.equals("operator |="))
				replace = "op_or_assign";
			else if (name.equals("operator new"))
				replace = "op_new";
			else if (name.equals("operator delete"))
				replace = "op_delete";
			else
				replace = "__PROBLEM__";
		}
		else if (name.startsWith("~"))
			replace = "destruct";
		else if (name.equals("bool"))
			replace = "Boolean";
		else if (name.equals("byte"))
			replace = "Byte";
		else if (name.equals("char"))
			replace = "Character";
		else if (name.equals("short"))
			replace = "Short";
		else if (name.equals("int"))
			replace = "Integer";
		else if (name.equals("long"))
			replace = "Long";
		else if (name.equals("float"))
			replace = "Float";
		else if (name.equals("double"))
			replace = "Double";
		else if (name.equals("String"))
			replace = "CppString";
		else
			replace = name // Cast operators need cleaning.
			.replace(' ', '_')
			.replace(':', '_')
			.replace('&', '_')
			.replace('(', '_')
			.replace(')', '_')
			.replace('*', '_')
			.replace('<', '_')
			.replace('>', '_')
			.replace(',', '_');

		if (replace.isEmpty())
			replace = "MISSING";
		
		return replace;
	}

	/**
	 * Gets a simplified Java compatible name.
	 */
	static String getSimpleName(IASTName name) throws DOMException
	{
		String nm = name.resolveBinding().getName();
		nm = normalizeName(nm);

		MyLogger.log("name: " + name.resolveBinding().getName() + ":" + nm);
		return nm;
	}
	
	/**
	 * Gets our enum TypeEnum of an IType.
	 */
	static TypeEnum getTypeEnum(IType type) throws DOMException
	{
		if (type instanceof IQualifierType)
		{
			type = ((IQualifierType) type).getType();
		}
		
		if (type instanceof IBasicType &&
				type instanceof ICPPBasicType &&
				((ICPPBasicType) type).getType() == ICPPBasicType.t_bool)
			return TypeEnum.BOOLEAN;

		if (type instanceof IBasicType &&
				type instanceof ICPPBasicType &&
				((ICPPBasicType) type).getType() == ICPPBasicType.t_wchar_t)
			return TypeEnum.CHAR;

		if (type instanceof IBasicType &&
				((IBasicType) type).getType() != IBasicType.t_void)
			return TypeEnum.NUMBER;

		if (type instanceof IBasicType &&
				((IBasicType) type).getType() == IBasicType.t_void)
			return TypeEnum.VOID;

		if (type instanceof IPointerType)
			return TypeEnum.POINTER;

		if (type instanceof IArrayType)
			return TypeEnum.ARRAY;
		
		if (type instanceof ICPPReferenceType)
			return TypeEnum.REFERENCE;

		if (type instanceof ICPPClassType)
			return TypeEnum.OBJECT;
		
		if (type instanceof ICPPTemplateTypeParameter)
			return TypeEnum.OTHER;
		
		if (type instanceof IEnumeration)
			return TypeEnum.ENUMERATION;
		
		if (type instanceof ICPPUnknownType)
			return TypeEnum.UNKNOWN;
		
		if (type instanceof ICPPFunctionType)
			return TypeEnum.FUNCTION;
		
		if (type instanceof IProblemType)
		{
			MyLogger.logImportant(((IProblemType) type).getMessage());
			return TypeEnum.UNKNOWN;
		}
			
		MyLogger.logImportant("Unknown type: " + type.getClass().getInterfaces()[0].toString());
		MyLogger.exitOnError();
		return null;
	}

	/**
	 * Gets the base type of an array.
	 * @return CDT IType of array.
	 */
	static IType getArrayBaseType(IType type) throws DOMException
	{
		IArrayType arr = (IArrayType) type;

		while (arr.getType() instanceof IArrayType)
		{
			IArrayType arr2 = (IArrayType) arr.getType();
			arr = arr2;
		}

		return arr.getType();
	}

	/**
	 * Gets the base type of a pointer.
	 * @return CDT IType of pointer.
	 */
	static IType getPointerBaseType(IType type) throws DOMException
	{
		IPointerType arr = (IPointerType) type;

		while (arr.getType() instanceof IPointerType)
		{
			IPointerType arr2 = (IPointerType) arr.getType();
			arr = arr2;
		}

		return arr.getType();
	}
	
	/**
	 * Gets the complete C++ qualified name.
	 */
	static String getCompleteName(IASTName name) throws DOMException
	{
		IBinding binding = name.resolveBinding();

		if (binding instanceof ICPPBinding)
		{
			ICPPBinding cpp = (ICPPBinding) binding;
			String names[] = cpp.getQualifiedName();
			String ret = "";
			for (int i = 0; i < names.length; i++)
			{
				ret += names[i];
				if (i != names.length - 1) 
					ret += "::";
			}
			MyLogger.log("Complete Name: " + ret);
			return ret;
		}

		return binding.getName();
	}

	/**
	 * Gets the qualifier part of a name.
	 */
	static String getQualifiedPart(IASTName name) throws DOMException
	{
		IBinding binding = name.resolveBinding();

		if (binding instanceof ICPPBinding)
		{
			ICPPBinding cpp = (ICPPBinding) binding;
			String names[] = cpp.getQualifiedName();
			String ret = "";
			for (int i = 0; i < names.length - 1; i++)
			{
				ret += names[i];
				if (i != names.length - 2) 
					ret += "::";
			}
			MyLogger.log("Qualified Name found: " + ret);
			return ret;
		}

		return "";
	}
}
