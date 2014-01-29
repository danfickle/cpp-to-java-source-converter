package com.github.danfickle.cpptojavasourceconverter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

import com.github.danfickle.cpptojavasourceconverter.models.DeclarationModels.*;

class TypeManager 
{
	private final TranslationUnitContext ctx;
	
	TypeManager(TranslationUnitContext con)
	{
		ctx = con;
	}
	
	enum TypeEnum
	{
		NUMBER, 
		BOOLEAN,
		CHAR,
		VOID,
		OBJECT_POINTER,
		BASIC_POINTER,
		OBJECT,
		OBJECT_ARRAY,
		BASIC_ARRAY,
		ANY,
		BASIC_REFERENCE,
		OBJECT_REFERENCE,
		OTHER,
		ENUMERATION,
		UNKNOWN,
		FUNCTION,
		FUNCTION_POINTER,
		FUNCTION_ARRAY,
		FUNCTION_REFERENCE, VOID_POINTER;
	}
	
	static boolean isOneOf(IType tp, TypeEnum... tps) throws DOMException
	{
		TypeEnum temp = getTypeEnum(tp);
		
		for (TypeEnum type : tps)
		{
			if (type == temp)
				return true;
		}

		return false;
	}
	
	
	static boolean isBasicType(IType tp) throws DOMException
	{
		return isOneOf(tp, TypeEnum.BOOLEAN, TypeEnum.CHAR, TypeEnum.NUMBER);
	}
	
	static IType expand(IType type)
	{
		while (type instanceof ITypedef ||
			   type instanceof IQualifierType)
		{
			if (type instanceof ITypedef)
				type = ((ITypedef) type).getType();
			else
				type = ((IQualifierType) type).getType();
		}		
		
		return type;
	}
	
	/**
	 * Determines if a type will turn into a pointer.
	 * @throws DOMException 
	 */
	static boolean isPtrOrArrayBasic(IType type) throws DOMException
	{
		return isOneOf(type, TypeEnum.BASIC_POINTER, TypeEnum.BASIC_ARRAY);
	}
	
	enum TypeType
	{
		INTERFACE,
		IMPLEMENTATION,
		RAW;
	}
	
	/**
	 * Look up a registered struct, class, union, method, etc.
	 */
	CppDeclaration getDeclFromTypeName(IType type, IASTName nm) throws DOMException
	{
		if (type != null)
		{
			for (CppDeclaration ent : ctx.global.decls)
			{
				if (ent.cppType.isSameType(type) && ent.nm.equals(nm))
					return ent;
				else if (ent.cppType.isSameType(type) && getCompleteName(ent.nm).equals(getCompleteName(nm)))
					return ent;
			}
		}
		return null;
	}

	/**
	 * IMPORTANT: This can only be used for classes, structs, unions
	 * and enums. If used for functions, methods, fields, etc
	 * it will return false positives.
	 */
	CppDeclaration getDeclFromType(IType type) throws DOMException
	{
		if (type != null)
		{
			for (CppDeclaration ent : ctx.global.decls)
			{
				if (ent.cppType.isSameType(type))
					return ent;
			}
		}
		return null;
	}
	
	private void makeName(IASTName nm, CppDeclaration decl, NameType nt) throws DOMException
	{
		String complete = getCompleteName(nm);
		String simple = getSimpleType(complete);
		
		if (simple == null || simple.trim().isEmpty())
		{
			simple = "Anon" + (decl instanceof CppClass ? "Class" : "Enum") + ctx.global.anonCount++;
			complete += simple;
		}

		decl.completeCppName = complete;		
		
		if (simple.equals("operator ++") ||
			simple.equals("operator --"))
		{
			// If we have a dummy parameter, then it is post inc/dec
			// rather than pre inc/dec.
			if (nm.resolveBinding() instanceof ICPPMethod)
			{
				if (((ICPPMethod) nm.resolveBinding()).getParameters().length == 1)
				{
					simple = "op" + (simple.equals("operator ++") ? "PostIncrement" : "PostDecrement");
				}
			}
			else
			{
				assert(nm.resolveBinding() instanceof ICPPFunction);

				if (((ICPPFunction) nm.resolveBinding()).getParameters().length == 2)
				{
					simple = "op" + (simple.equals("operator ++") ? "PostIncrement" : "PostDecrement");
				}
			}
		}

		decl.name = cppNameToJavaName(simple, nt);
	}
	
	/**
	 * Register a class, struct, union (which all end up
	 * as Java classes). Also, all can be anonymous.
	 */
	void registerDecl(CppDeclaration decl, IType type,
			IASTName nm, NameType nt, String filename, int lineno) throws DOMException
	{
		decl.cppType = type;
		decl.nm = nm;

		// First we check if we are actually in a class declaration.
		if (ctx.converter.currentInfoStack.peekFirst() != null)
			decl.parent = ctx.converter.currentInfoStack.peekFirst().tyd;
		// Second we test if we can get an owner type.
		else if (nm.resolveBinding().getOwner() != null)
			decl.parent = (CppClass) getDeclFromType(ctx.converter.evalBindingReturnType(nm.resolveBinding().getOwner()));
		
		if (decl instanceof CppFunction && decl.parent == null)
		{
			((CppFunction) decl).isOriginallyGlobal = true;
		}
		
		// Third, we check the filename has no associated class.
		if (decl.parent == null)
			decl.parent = ctx.global.fileClasses.get(filename);
		
		if (decl.parent == null &&
			!(decl instanceof CppEnum) && 
			!(decl instanceof CppClass))
		{
			// If we must create a class to house declarations we do it here.
			decl.parent = getClassToHouseGlobalDecls(filename);
		}
		
		if (decl.parent != null)
		{
			((CppClass) decl.parent).declarations.add(decl);
		}

		decl.file = filename;
		decl.line = lineno;

		if (nm != null)
			makeName(nm, decl, nt);
		
		if (decl instanceof CppClass && decl.parent != null)
		{
			((CppClass) decl).isNested = true;
		}
		else if (decl instanceof CppEnum && decl.parent != null)
		{
			((CppEnum) decl).isNested = true;
		}

		ctx.global.decls.add(decl);
	}
	
	private CppClass getClassToHouseGlobalDecls(String filename)
	{
		CppClass cls = ctx.global.fileClasses.get(filename);
		
		if (cls == null)
		{
			cls = ctx.declModels.new CppClass();
			cls.name = filename;
			ctx.global.fileClasses.put(filename, cls);
		}

		return cls;
	}
	
	/**
	 * Attempts to convert a CDT type to the approriate Java
	 * type.
	 */
	String cppToJavaType(IType type, TypeType tp) throws DOMException
	{
		if (type instanceof IBasicType)
		{
			// Primitive type - int, bool, char, etc...
			IBasicType basic = (IBasicType) type;
			
			if (tp == TypeType.RAW)
				return evaluateSimpleType(basic.getKind(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
			else if (tp == TypeType.INTERFACE)
				return "I" + evaluateSimpleTypeBoxed(basic.getKind(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
			else
				return "M" + evaluateSimpleTypeBoxed(basic.getKind(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
		}
		else if (type instanceof IArrayType)
		{
			IArrayType array = (IArrayType) type;

			String jt = cppToJavaType(array.getType(), tp);
			
			if (tp == TypeType.RAW)
				return jt;
			else if (tp == TypeType.INTERFACE)
				return jt;
			else
				return jt + "Multi";
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
			IType baseType = getPointerBaseType(type);
			int ptrCount = getPointerIndirectionCount(type);

			if (isBasicType(baseType) && ptrCount == 1)
			{
				return cppToJavaType(baseType, tp);
			}
			else if (tp == TypeType.RAW)
			{
				return cppToJavaType(baseType, tp);
			}
			else
			{
				// One level of indirection becomes:
				//   IPtrObject<BASE_TYPE>
				// Two levels of indirection become:
				//   IPtrObject<IPtrObject<BASE_TYPE>>
				// and so on.
				String wrap = cppToJavaType(baseType, tp);
				
				if (isBasicType(baseType))
					ptrCount--;
				
				while (ptrCount-- > 0)
				{
					if (tp == TypeType.INTERFACE)
						wrap = "IPtrObject<" + wrap + ">";
					else if (tp == TypeType.IMPLEMENTATION)
						wrap = "PtrObject<" + wrap + ">";
				}

				return wrap;
			}
		}
		else if (type instanceof ICPPReferenceType)
		{
			ICPPReferenceType ref = (ICPPReferenceType) type;

			if ((ref.getType() instanceof IQualifierType) || 
				ref.getType() instanceof ICPPClassType /* &&
				 ((IQualifierType) ref.getType()).isConst()) */)
			{
				return cppToJavaType(ref.getType(), tp);
			}
			else if (ref.getType() instanceof IBasicType || 
					(ref.getType() instanceof ITypedef && 
					((ITypedef) ref.getType()).getType() instanceof IBasicType))
			{
				IBasicType basic;
				if (ref.getType() instanceof ITypedef)
					basic = ((IBasicType)((ITypedef) ref.getType()).getType());
				else
					basic = (IBasicType) ref.getType();

				String basicStr = evaluateSimpleTypeBoxed(basic.getKind(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
				String simpleType = "I" + basicStr;
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
			return cppToJavaType(qual.getType(), tp);
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
			return cppToJavaType(typedef.getType(), tp);
		}
		else if (type instanceof IEnumeration)
		{
			IEnumeration enumeration = (IEnumeration) type;
			return getSimpleType(enumeration.getName());
		}
		else if (type instanceof IFunctionType)
		{
			//IFunctionType func = (IFunctionType) type;
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
	
	/**
	 * Attempts to get the boxed type for a primitive type.
	 * We need the boxed type to use for Ptr and Ref parametized
	 * types.
	 */
	static String evaluateSimpleTypeBoxed(IBasicType.Kind type, boolean isShort, boolean isLongLong, boolean isUnsigned)
	{
		switch (type)
		{
		case eChar:
			MyLogger.log("char");
			return "Byte";
		case eInt:
			MyLogger.log("int");
			if (isShort)
				return "Short";
			else if (isLongLong)
				return "Long";
			else
				return "Integer";
		case eFloat:
			MyLogger.log("float");
			return "Float";
		case eDouble:
			MyLogger.log("double");
			return "Double";
		case eUnspecified:
			MyLogger.log("unspecified");
			if (isUnsigned)
				return "Integer";
			else
				return "Integer";
		case eVoid:
			MyLogger.log("void");
			return "Void";
		case eBoolean:
			MyLogger.log("bool");
			return "Boolean";
		case eChar16:
		case eWChar:
			MyLogger.log("wchar_t");
			return "Character";
		default:
			return null;
		}
	}
	
	/**
	 * Returns the Java simple type for the corresponding C++ type. 
	 */
	static String evaluateSimpleType(IBasicType.Kind type, boolean isShort, boolean isLongLong, boolean isUnsigned)
	{
		switch (type)
		{
		case eChar:
			MyLogger.log("char");
			return "byte";
		case eInt:
			MyLogger.log("int");
			if (isShort)
				return "short";
			else if (isLongLong)
				return "long";
			else
				return "int";
		case eFloat:
			MyLogger.log("float");
			return "float";
		case eDouble:
			MyLogger.log("double");
			return "double";
		case eUnspecified:
			MyLogger.log("unspecified");
			if (isUnsigned)
				return "int";
			else
				return "int";
		case eVoid:
			MyLogger.log("void");
			return "void";
		case eBoolean:
			MyLogger.log("bool");
			return "boolean";
		case eChar16:
		case eWChar:
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
		if (qualifiedType.contains("::"))
			return qualifiedType.substring(qualifiedType.lastIndexOf("::") + "::".length());
		else
			return qualifiedType;
	}

	static final Map<String, String> operators = new HashMap<String, String>();
	
	static 
	{
		operators.put("operator =", "opAssign");
		operators.put("operator +", "opPlus");
		operators.put("operator -", "opMinus");
		operators.put("operator *", "opStar");
		operators.put("operator /", "opDivide");
		operators.put("operator %", "opModulo");
		operators.put("operator ++", "opPreIncrement");
		operators.put("operator --", "opPreDecrement");
		operators.put("operator ==", "equals");
		operators.put("operator !=", "opNotEquals");
		operators.put("operator >", "opGreaterThan");
		operators.put("operator <", "opLessThan");
		operators.put("operator >=", "opGtEquals");
		operators.put("operator <=", "opLtEquals");
		operators.put("operator !", "opLogicalNot");
		operators.put("operator &&", "opLogicalAnd");
		operators.put("operator ||", "opLogicalOr");
		operators.put("operator ~", "opComplement");
		operators.put("operator &", "opBitwiseAnd");
		operators.put("operator |", "opBitwiseOr");
		operators.put("operator ^", "opBitwiseXOr");
		operators.put("operator <<", "opBitwiseLeftShift");
		operators.put("operator >>", "opBitwiseRightShift");
		operators.put("operator +=", "opPlusAssign");
		operators.put("operator -=", "opMinusAssign");
		operators.put("operator *=", "opMultiplyAssign");
		operators.put("operator /=", "opDivideAssign");
		operators.put("operator %=", "opModuloAssign");
		operators.put("operator &=", "opAndAssign");
		operators.put("operator |=", "opOrAssign");
		operators.put("operator ^=", "opXOrAssign");
		operators.put("operator <<=", "opLeftShiftAssign");
		operators.put("operator >>=", "opRightShiftAssign");
		operators.put("operator []", "opSubscript");
		operators.put("operator ->", "opThinPointer");
		operators.put("operator ->*", "opThinPointerWithDeref");		
		operators.put("operator ()", "opFunctionCall");
		operators.put("operator ,", "opComma");
		operators.put("operator new", "opNewSingle");		
		operators.put("operator new[]", "opNewArray");
		operators.put("operator delete", "opDelSingle");		
		operators.put("operator delete[]", "opDelArray");		
	}
	
	/**
	 * Replaces C++ names with Java compatible names for functions.
	 * You may need to add missing operators.
	 */
	static String normalizeName(String name)
	{
		String replace;
		
		if (name.startsWith("operator "))
		{
			replace = operators.get(name);
			if (replace == null)
			{
				// Cast operators need cleaning.
				replace = "opCastTo" + name
						.replace("operator ", "")
						.replace(" ", "")
						.replace(':', '_')
						.replace("&", "Ref")
						.replace('(', '_')
						.replace(')', '_')
						.replace("*", "Ptr")
						.replace('<', '_')
						.replace('>', '_')
						.replace(',', '_');
			}
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
			replace = name;
		
		return replace;
	}

	/**
	 * Gets a simplified Java compatible name.
	 */
	static String getSimpleName(IASTName name) throws DOMException
	{
		String nm = name.resolveBinding().getName();
		nm = cppNameToJavaName(nm, NameType.CAMEL_CASE);

		MyLogger.log("name: " + name.resolveBinding().getName() + ":" + nm);

		return nm;
	}
	
	/**
	 * Gets our enum TypeEnum of an IType.
	 */
	private static TypeEnum getTypeEnum(IType type) throws DOMException
	{
		type = expand(type);
		
		if (type instanceof IBasicType)
		{
			if (((IBasicType) type).getKind() == IBasicType.Kind.eBoolean)
			{
				return TypeEnum.BOOLEAN;
			}

			if (((IBasicType) type).getKind() == IBasicType.Kind.eChar16 ||
				((IBasicType) type).getKind() == IBasicType.Kind.eWChar)
			{
				return TypeEnum.CHAR;
			}

			if (((IBasicType) type).getKind() != IBasicType.Kind.eVoid)
			{
				return TypeEnum.NUMBER;
			}
		
			if (((IBasicType) type).getKind() == IBasicType.Kind.eVoid)
			{
				return TypeEnum.VOID;
			}
		}
		
		if (type instanceof IFunctionType)
		{
			return TypeEnum.FUNCTION;
		}

		if (type instanceof IPointerType)
		{
			type = getPointerBaseType(type);
			
			if (isOneOf(type, TypeEnum.OBJECT))
				return TypeEnum.OBJECT_POINTER;
			else if (isOneOf(type, TypeEnum.BOOLEAN, TypeEnum.CHAR, TypeEnum.NUMBER))
				return TypeEnum.BASIC_POINTER;
			else if (isOneOf(type, TypeEnum.FUNCTION))
				return TypeEnum.FUNCTION_POINTER;
			else if (isOneOf(type, TypeEnum.VOID))
				return TypeEnum.VOID_POINTER;
		}

		if (type instanceof IArrayType)
		{
			type = getArrayBaseType(type);
			
			if (isOneOf(type, TypeEnum.OBJECT))
				return TypeEnum.OBJECT_ARRAY;
			else if (isOneOf(type, TypeEnum.BOOLEAN, TypeEnum.CHAR, TypeEnum.NUMBER))
				return TypeEnum.BASIC_ARRAY;
			else if (isOneOf(type, TypeEnum.FUNCTION))
				return TypeEnum.FUNCTION_ARRAY;
		}
			
		if (type instanceof ICPPReferenceType)
		{
			type = getReferenceBaseType(type);

			if (isOneOf(type, TypeEnum.OBJECT))
				return TypeEnum.OBJECT_REFERENCE;
			else if (isOneOf(type, TypeEnum.BOOLEAN, TypeEnum.CHAR, TypeEnum.NUMBER))
				return TypeEnum.BASIC_REFERENCE;
			else if (isOneOf(type, TypeEnum.FUNCTION))
				return TypeEnum.FUNCTION_REFERENCE;
		}

		if (type instanceof ICPPClassType)
		{
			return TypeEnum.OBJECT;
		}
		
		if (type instanceof ICPPTemplateTypeParameter)
		{
			return TypeEnum.OTHER;
		}
		
		if (type instanceof IEnumeration)
		{
			return TypeEnum.ENUMERATION;
		}
		
		if (type instanceof ICPPUnknownType)
		{
			return TypeEnum.UNKNOWN;
		}
		
		if (type instanceof IProblemType)
		{
			MyLogger.logImportant(((IProblemType) type).getMessage());
			return TypeEnum.UNKNOWN;
		}
			
		MyLogger.logImportant("Unknown type: " + type.getClass().getInterfaces()[0].toString());
		MyLogger.exitOnError();
		return null;
	}

	static IType getReferenceBaseType(IType type) throws DOMException
	{
		while (type instanceof ICPPReferenceType ||
			   expand(type) instanceof ICPPReferenceType)
		{
			if (type instanceof ICPPReferenceType)
				type = ((ICPPReferenceType) type).getType();
			else
				type = ((ICPPReferenceType) expand(type)).getType();
		}
		
		return type;
	}
	
	/**
	 * Gets the base type of an array.
	 * @return CDT IType of array.
	 */
	static IType getArrayBaseType(IType type) throws DOMException
	{
		while (type instanceof IArrayType ||
			   expand(type) instanceof IArrayType)
		{
			if (type instanceof IArrayType)
				type = ((IArrayType) type).getType();
			else
				type = ((IArrayType) expand(type)).getType();
		}

		return type;
	}

	/**
	 * Gets the base type of a pointer.
	 * @return CDT IType of pointer.
	 */
	static IType getPointerBaseType(IType type) throws DOMException
	{
		while (type instanceof IPointerType ||
			   expand(type) instanceof IPointerType)
		{
			if (type instanceof IPointerType)
				type = ((IPointerType) type).getType();
			else
				type = ((IPointerType) expand(type)).getType();
		}
		
		return type;
	}
	
	static int getPointerIndirectionCount(IType type) throws DOMException
	{
		int cnt = 0;
		
		while (type instanceof IPointerType ||
			   expand(type) instanceof IPointerType)
		{
			cnt++;
			
			if (type instanceof IPointerType)
				type = ((IPointerType) type).getType();
			else
				type = ((IPointerType) expand(type)).getType();
		}

		return cnt;
	}
	
	static boolean decaysToPointer(IType type) throws DOMException
	{
		type = getReferenceBaseType(type);
		
		return TypeManager.isOneOf(type, TypeEnum.OBJECT_POINTER,
			TypeEnum.BASIC_POINTER, TypeEnum.FUNCTION_POINTER,
			TypeEnum.VOID_POINTER, TypeEnum.BASIC_ARRAY, TypeEnum.OBJECT_ARRAY,
			TypeEnum.FUNCTION_ARRAY);
	}

	/**
	 * Gets the complete C++ qualified name.
	 */
	static String getCompleteName(IBinding binding) throws DOMException
	{
		if (binding instanceof ICPPBinding)
		{
			ICPPBinding cpp = (ICPPBinding) binding;
			String names[] = cpp.getQualifiedName();
			StringBuilder ret = new StringBuilder(); 

			for (int i = 0; i < names.length; i++)
			{
				ret.append(names[i]);
				if (i != names.length - 1) 
					ret.append("::");
			}

			MyLogger.log("Complete Name: " + ret);
			return ret.toString();
		}

		return binding.getName();
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
			StringBuilder ret = new StringBuilder(); 

			for (int i = 0; i < names.length; i++)
			{
				ret.append(names[i]);
				if (i != names.length - 1) 
					ret.append("::");
			}

			MyLogger.log("Complete Name: " + ret);
			return ret.toString();
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
			StringBuilder ret = new StringBuilder();

			for (int i = 0; i < names.length - 1; i++)
			{
				ret.append(names[i]);
				if (i != names.length - 2) 
					ret.append("::");
			}

			MyLogger.log("Qualified Name found: " + ret);
			return ret.toString();
		}

		return "";
	}
	
	enum NameType
	{
		CAPITALIZED,
		CAMEL_CASE,
		ALL_CAPS;
	}
	
	static String cppNameToJavaName(String simple, NameType tp)
	{
		// TODO: Capitalization, camel case.
		String nm = normalizeName(simple);
		return nm;
	}
}
