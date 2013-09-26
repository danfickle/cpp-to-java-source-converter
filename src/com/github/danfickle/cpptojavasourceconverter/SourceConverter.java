package com.github.danfickle.cpptojavasourceconverter;
/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.core.dom.ast.gnu.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MValueOfExpressionNumber;
import com.github.danfickle.cpptojavasourceconverter.TypeHelpers.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.TypeHelpers.TypeType;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.*;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.*;

/**
 * Second stage of the C++ to Java source converter.
 * @author DanFickle
 * http://github.com/danfickle/cpp-to-java-source-converter/
 */
public class SourceConverter
{
	private final GlobalContext ctx;
	
	SourceConverter(GlobalContext con) {
		ctx = con;
	}
	
	/**
	 * This class keeps track of all the info on a C++ class
	 * or struct.
	 */
	static class CompositeInfo
	{
		CompositeInfo(CppDeclaration tydArg)
		{
			tyd = tydArg;
		}
		
		CppDeclaration tyd;
		IASTDeclSpecifier declSpecifier;
		String superClass;
		boolean hasCtor;
		boolean hasDtor;
		boolean hasCopy;
		boolean hasAssign;
		boolean hasSuper;
	}
	
	/**
	 * The info for the current class/struct.
	 */
	private CompositeInfo currentInfo = null;
	
	/**
	 * The stack of classes/structs.
	 */
	private Deque<CompositeInfo> currentInfoStack = new ArrayDeque<CompositeInfo>();
	
	boolean addDeclaration(CppDeclaration decl)
	{
		boolean nested = true;

		if (currentInfo == null)
		{
			ctx.globalDeclarations.add(decl);
			nested = false;
		}
		else
		{
			currentInfo.tyd.declarations.add(decl);
		}
		currentInfo = new CompositeInfo(decl);
		currentInfoStack.push(currentInfo);
		return nested;
	}
	
	void popDeclaration()
	{
		currentInfoStack.pop();
		currentInfo = currentInfoStack.peekFirst();
	}

	CompositeInfo getCurrentCompositeInfo()
	{
		return currentInfo;
	}
	
	/**
	 * Given a list of fields for a class, adds initialization statements
	 * to the constructor for each field as required.
	 * Initializers provided to this function are generated from C++ initializer
	 * lists, and implicit initializers for objects.
	 * Note: We must initialize in order that fields were declared.
	 */
	void generateCtorStatements(List<FieldInfo> fields, MCompoundStmt method)
	{
		int start = 0;
		for (FieldInfo fieldInfo : fields)
		{
			MyLogger.log(fieldInfo.field.getName());

			// Static fields can't be initialized in the constructor.
			if (fieldInfo.init != null && !fieldInfo.isStatic)
			{
				// Use 'this.field' construct as we may be shadowing a param name.
				MFieldReferenceExpression frl = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
				MExpression expr = ModelCreation.createInfixExpr(frl, fieldInfo.init, "=");
				MStmt stmt = ModelCreation.createExprStmt(expr);
				
				// Add assignment statements to start of generated method...
				method.statements.add(start, stmt);
				start++;
			}
		}
	}

	/**
	 * Generate destruct calls for fields in reverse order of field declaration.
	 */
	void generateDtorStatements(List<FieldInfo> fields, MCompoundStmt method, boolean hasSuper) throws DOMException
	{
		for (int i = fields.size() - 1; i >= 0; i--)
		{
			MyLogger.log(fields.get(i).field.getName());

			if (fields.get(i).isStatic)
				/* Do nothing. */ ;
			else if (TypeHelpers.getTypeEnum(fields.get(i).field.getType()) == TypeEnum.OBJECT)
			{
				// Call this.field.destruct()
				MStmt stmt = ModelCreation.createMethodCall("this", fields.get(i).field.getName(), "destruct");
				method.statements.add(stmt);
			}
			else if (TypeHelpers.getTypeEnum(fields.get(i).field.getType()) == TypeEnum.OBJECT_ARRAY)
			{
				// Call DestructHelper.destruct(this.field)
				MStmt stmt = ModelCreation.createMethodCall(
						"DestructHelper",
						"destruct",
						ModelCreation.createFieldReference("this", fields.get(i).field.getName()));
				
				method.statements.add(stmt);
			}
		}
		
		// Last, we call destruct on the super object.
		if (hasSuper)
		{
			// Call super.destruct()
			MStmt stmt = ModelCreation.createMethodCall("super", "destruct");
			method.statements.add(stmt);
		}
	}

	/**
	 * Generates a Java field, given a C++ field.
	 */
	private void generateField(IBinding binding, IASTDeclarator declarator, MExpression init) throws DOMException
	{
		IField ifield = (IField) binding;

		MSimpleDecl frag = new MSimpleDecl();
		frag.name = ifield.getName();

		if (ifield.isStatic())
		{
			frag.isStatic = true;
			
			if (TypeHelpers.getTypeEnum(ifield.getType()) == TypeEnum.OBJECT ||
				TypeHelpers.getTypeEnum(ifield.getType()) == TypeEnum.OBJECT_ARRAY)
			{
				frag.initExpr = init;
			}
		}

		if (ifield.getType().toString().isEmpty())
			frag.type = "AnonClass" + (anonClassCount - 1);
		else
			frag.type = TypeHelpers.cppToJavaType(ifield.getType());

		frag.isPublic = true;
		
		addDeclaration(frag);
		popDeclaration();
	}
	
	private int anonClassCount = 0;
	
	/**
	 * Generates a Java field, given a C++ top-level (global) variable.
	 */
	private void generateVariable(IBinding binding, IASTDeclarator declarator, MExpression init) throws DOMException
	{
		IVariable ifield = (IVariable) binding;

		MSimpleDecl frag = new MSimpleDecl();
		frag.name = ifield.getName();
		frag.initExpr = init;
		frag.isStatic = true;
		
		if (ifield.getType().toString().isEmpty())
			frag.type = "AnonClass" + (anonClassCount - 1);
		else
			frag.type = TypeHelpers.cppToJavaType(ifield.getType());

		frag.isPublic = true;
		
		addDeclaration(frag);
		popDeclaration();
	}
	
	/**
	 * C++ has four special methods that must be generated if not present.
	 * This function attempts to find constructor, destructor, copy and assign
	 * methods, so they can be generated if not present. 
	 */
	private void findSpecialMethods(IASTDeclSpecifier declSpec, CompositeInfo info) throws DOMException
	{
		if (!(declSpec instanceof IASTCompositeTypeSpecifier))
			return;

		IASTCompositeTypeSpecifier composite = (IASTCompositeTypeSpecifier) declSpec;

		for (IASTDeclaration decl : composite.getMembers())
		{
			if (decl instanceof IASTFunctionDefinition)
			{
				if (((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding() instanceof ICPPConstructor)
				{
					info.hasCtor = true;

					ICPPConstructor ctor = (ICPPConstructor) ((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding(); 
					ICPPParameter[] params  = ctor.getParameters();

					if (params.length != 0 &&
						TypeHelpers.getTypeEnum(params[0].getType()) == TypeEnum.OBJECT_REFERENCE &&
						TypeHelpers.cppToJavaType(params[0].getType()).toString().equals(ctor.getName()))
					{
						// TODO: We should check there are no params or others have default values...
						info.hasCopy = true;
					}
				}
				else if (((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding() instanceof ICPPMethod)
				{
					ICPPMethod meth = (ICPPMethod) ((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding();
					
					if (meth.isDestructor())
						info.hasDtor = true;
					else if (meth.getName().equals("operator ="))
						info.hasAssign = true;
				}
			}
		}
	}
	
	/**
	 * This method creates a list of fields present in the class.
	 */
	List<FieldInfo> collectFieldsForClass(IASTDeclSpecifier declSpec) throws DOMException
	{
		if (!(declSpec instanceof IASTCompositeTypeSpecifier))
			return Collections.emptyList();

		IASTCompositeTypeSpecifier composite = (IASTCompositeTypeSpecifier) declSpec;

		List<FieldInfo> fields = new ArrayList<FieldInfo>();
		
		for (IASTDeclaration decl : composite.getMembers())
		{
			if (decl instanceof IASTSimpleDeclaration)
			{
				IASTSimpleDeclaration simple = (IASTSimpleDeclaration) decl;

				List<MExpression> exprs = evaluateDeclarationReturnInitializers(simple);				
				
				int i = 0;
				for (IASTDeclarator declarator : simple.getDeclarators())
				{
					IBinding binding = declarator.getName().resolveBinding();
				
					if (binding instanceof IField)
					{
						FieldInfo field = new FieldInfo(declarator, exprs.get(i), (IField) binding);

						if (declarator instanceof IASTFieldDeclarator &&
							((IASTFieldDeclarator) declarator).getBitFieldSize() != null)
						{
							ctx.bitfieldMngr.addBitfield(declarator.getName());
							field.isBitfield = true;
						}
						
						if (((IField) binding).isStatic())
							field.isStatic = true;

						fields.add(field);
					}
					i++;
				}
			}
		}
		return fields;
	}
	
	List<MExpression> evaluateDeclarationReturnInitializers(IASTSimpleDeclaration simple) throws DOMException 
	{
		List<MExpression> exprs = new ArrayList<MExpression>();
		List<IType> types = evaluateDeclarationReturnCppTypes(simple);
		int i = 0;
		
		for (IASTDeclarator decl : simple.getDeclarators())
		{
			if (decl.getInitializer() == null)
			{
				if (TypeHelpers.isBasicType(types.get(i)))
				{
					MValueOfExpressionNumber expr = new MValueOfExpressionNumber();
					
					expr.type = TypeHelpers.cppToJavaType(types.get(i), TypeType.IMPLEMENTATION);

					if (TypeHelpers.getTypeEnum(types.get(i)) == TypeEnum.BOOLEAN)
						expr.operand = ModelCreation.createLiteral("false");
					else
						expr.operand = ModelCreation.createLiteral("0");

					exprs.add(expr);
				}
				else if (TypeHelpers.getTypeEnum(types.get(i)) == TypeEnum.BASIC_ARRAY)
				{
					MValueOfExpressionArray expr = new MValueOfExpressionArray();

					expr.type = TypeHelpers.cppToJavaType(types.get(i), TypeType.IMPLEMENTATION);
					expr.operands = ctx.exprEvaluator.getArraySizeExpressions(types.get(i));
					exprs.add(expr);
				}

				exprs.add(null);
			}
			else if (decl.getInitializer() instanceof IASTEqualsInitializer)
			{
				MExpression expr = ctx.exprEvaluator.eval1Expr((IASTExpression) ((IASTEqualsInitializer) decl.getInitializer()).getInitializerClause()); 

				if (TypeHelpers.isBasicType(types.get(i)))
				{
					MValueOfExpressionNumber wrap = new MValueOfExpressionNumber();
					wrap.type = TypeHelpers.cppToJavaType(types.get(i), TypeType.IMPLEMENTATION);
					wrap.operand = expr;
					exprs.add(wrap);
				}
				else
				{
					exprs.add(expr);
				}
			}
			else if (decl.getInitializer() instanceof ICPPASTConstructorInitializer)
			{
				exprs.add(ctx.exprEvaluator.eval1Expr((IASTExpression) ((ICPPASTConstructorInitializer) decl.getInitializer()).getExpression()));
			}

			i++;
		}

		return exprs;
	}

	/**
	 * Attempts to evaluate the given declaration (function, class,
	 * namespace, template, etc).
	 */
	void evalDeclaration(IASTDeclaration declaration) throws DOMException
	{
		if (declaration instanceof IASTFunctionDefinition &&
			((IASTFunctionDefinition)declaration).getDeclarator().getName().resolveBinding() instanceof IFunction)
		{
			MyLogger.log("function definition");
			ctx.funcMngr.evalFunction(declaration);
		}
		else if (declaration instanceof IASTFunctionDefinition)
		{
			IBinding bind = ((IASTFunctionDefinition) declaration).getDeclarator().getName().resolveBinding();
			
			if (bind instanceof IProblemBinding)
				MyLogger.logImportant("Problem function: " + ((IProblemBinding) bind).getMessage() + ((IProblemBinding) bind).getLineNumber());
			else
				MyLogger.logImportant("Function with unknown binding: " + bind.getClass().getCanonicalName());
		}
		else if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simple = (IASTSimpleDeclaration) declaration;
			evalDeclSpecifier(simple.getDeclSpecifier());

			List<MExpression> exprs = evaluateDeclarationReturnInitializers(simple);
			int i = 0;

			for (IASTDeclarator declarator : simple.getDeclarators())
			{
				IBinding binding = declarator.getName().resolveBinding();

				if (declarator instanceof IASTFieldDeclarator &&
					((IASTFieldDeclarator) declarator).getBitFieldSize() != null)
				{
					MyLogger.log("bit field");
					// We replace bit field fields with getter and setter methods...
					ctx.bitfieldMngr.evalDeclBitfield((IField) binding, declarator);
				}
				else if (binding instanceof IField)
				{
					MyLogger.log("standard field");
					generateField(binding, declarator, exprs.get(i));
				}
				else if (binding instanceof IFunction &&
						declarator instanceof IASTFunctionDeclarator)
				{
					ctx.funcMngr.makeDefaultCalls((IASTFunctionDeclarator) declarator, binding);
				}
				else if (binding instanceof IVariable)
				{
					generateVariable(binding, declarator, exprs.get(i));
				}
				else
				{
					MyLogger.logImportant("Unsupported declarator: " + declarator.getClass().getCanonicalName() + ":" + binding.getClass().getName());
				}
				i++;
			}
		}
		else if (declaration instanceof ICPPASTNamespaceDefinition)
		{
			ICPPASTNamespaceDefinition namespace = (ICPPASTNamespaceDefinition) declaration;
			MyLogger.log("namespace definition");

			// We don't care about namespaces but obviously must process the declarations
			// they contain...
			for (IASTDeclaration childDeclaration : namespace.getDeclarations())
				evalDeclaration(childDeclaration);
		}
		else if (declaration instanceof IASTASMDeclaration)
		{
			// Can't do anything with assembly...
			MyLogger.logImportant("ASM : " + declaration.getRawSignature());
			MyLogger.exitOnError();
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			MyLogger.logImportant("Problem declaration: " + p.getProblem().getMessageWithLocation() + ":" + p.getRawSignature());
			//exitOnError();
		}
		else if (declaration instanceof ICPPASTVisibilityLabel)
		{
			//ICPPASTVisibilityLabel vis = (ICPPASTVisibilityLabel) declaration;
			MyLogger.log("visibility");
			// We currently ignore visibility labels. If you wish to process
			// labels remember friend classes...
		}
		else if (declaration instanceof ICPPASTUsingDirective)
		{
			// ICPPASTUsingDirective usingDirective = (ICPPASTUsingDirective)declaration;
			MyLogger.log("using directive");
			// We ignore using directives, for now everything goes in the one package...
		}
		else if (declaration instanceof ICPPASTNamespaceAlias)
		{
			//ICPPASTNamespaceAlias namespaceAlias = (ICPPASTNamespaceAlias)declaration;
			MyLogger.log("Namespace alias");
			// We ignore namespace aliases...
		}
		else if (declaration instanceof ICPPASTUsingDeclaration)
		{
			// ICPPASTUsingDeclaration usingDeclaration = (ICPPASTUsingDeclaration)declaration;
			MyLogger.log("using declaration");
			// We ignore the using declaration...
		}
		else if (declaration instanceof ICPPASTLinkageSpecification)
		{
			ICPPASTLinkageSpecification linkageSpecification = (ICPPASTLinkageSpecification)declaration;
			MyLogger.log("linkage specification");

			// We don't care about linkage but we must process declarations using said linkage...
			for (IASTDeclaration childDeclaration : linkageSpecification.getDeclarations())
				evalDeclaration(childDeclaration);
		}
		else if (declaration instanceof ICPPASTTemplateDeclaration)
		{
			ICPPASTTemplateDeclaration templateDeclaration = (ICPPASTTemplateDeclaration)declaration;
//			print("template declaration");
//			printerr(templateDeclaration.getDeclaration().getClass().getCanonicalName());
//			List<TypeParameter> templateTypes = getTemplateParams(templateDeclaration.getTemplateParameters());
//			templateParamsQueue = templateTypes;
			
			evalDeclaration(templateDeclaration.getDeclaration());
		}
		else if (declaration instanceof ICPPASTExplicitTemplateInstantiation)
		{
			ICPPASTExplicitTemplateInstantiation explicitTemplateInstantiation = (ICPPASTExplicitTemplateInstantiation)declaration;
			MyLogger.log("explicit template instantiation");

			evalDeclaration(explicitTemplateInstantiation.getDeclaration());
		}
		else if (declaration instanceof ICPPASTTemplateSpecialization)
		{
			ICPPASTTemplateSpecialization templateSpecialization = (ICPPASTTemplateSpecialization)declaration;
			MyLogger.log("template specialization");

			evalDeclaration(templateSpecialization.getDeclaration());
		}
		else
		{
			MyLogger.log("Unknown declaration: " + declaration.getClass().getCanonicalName());
			MyLogger.exitOnError();
		}
	}

//	private List<TypeParameter> templateParamsQueue = new ArrayList<TypeParameter>(); // TODO...
	
//	/**
//	 * Gets a list of template type parameters.
//	 */
//	private List<TypeParameter> getTemplateParams(ICPPASTTemplateParameter[] templateParams) throws DOMException
//	{
//		List<TypeParameter> ret = new ArrayList<TypeParameter>();
//		
//		for (ICPPASTTemplateParameter parameter : templateParams)
//		{
//			if (parameter instanceof ICPPASTParameterDeclaration)
//			{
//				ICPPASTParameterDeclaration parameterDeclaration = (ICPPASTParameterDeclaration)parameter;
//				printerr("parameterDeclaration: " + parameter.getRawSignature() + parameterDeclaration.getDeclarator().getName().resolveBinding().getClass().getCanonicalName());
//
//				// Not much we can do with this...
//				String str = parameterDeclaration.getDeclarator().getName().resolveBinding().getName();
//				TypeParameter typeParam = ast.newTypeParameter();
//				typeParam.setName(ast.newSimpleName(normalizeName(str)));
//				ret.add(typeParam);
//			}
//			else if (parameter instanceof ICPPASTSimpleTypeTemplateParameter)
//			{
//				ICPPASTSimpleTypeTemplateParameter simpleTypeTemplateParameter = (ICPPASTSimpleTypeTemplateParameter)parameter;
//				print("simpletypeTemplateparameter");
//
//				TypeParameter typeParam = ast.newTypeParameter();
//				typeParam.setName(ast.newSimpleName(simpleTypeTemplateParameter.getName().resolveBinding().getName()));
//				ret.add(typeParam);
//			}
//			else if (parameter instanceof ICPPASTTemplatedTypeTemplateParameter)
//			{
//				//ICPPASTTemplatedTypeTemplateParameter templatedTypeTemplateParameter = (ICPPASTTemplatedTypeTemplateParameter)parameter;
//				printerr("templatedtypetemplate: " + parameter.getRawSignature());
//				TypeParameter typeParam = ast.newTypeParameter();
//				typeParam.setName(ast.newSimpleName("PROBLEM"));
//				ret.add(typeParam);
//				// We don't support nested templates at this stage...
//				//exitOnError();
//
//				//for (ICPPASTTemplateParameter childParameter : templatedTypeTemplateParameter.getTemplateParameters())
//				//	evaluate(childParameter);
//			}
//		}
//	
//		return ret;
//	}
	
	/**
	 * Evaluates a variable declaration and returns types for each variable.
	 * For example int a, b, * c; would return two ints and an int *. 
	 */
	List<String> evaluateDeclarationReturnTypes(IASTDeclaration declaration) throws DOMException
	{
		List<String> ret = new ArrayList<String>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			MyLogger.log("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
			{
				IBinding binding = decl.getName().resolveBinding();

				if (binding instanceof IVariable)
				{
					ret.add(TypeHelpers.cppToJavaType(((IVariable) binding).getType()));
				}
			}
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			MyLogger.logImportant("Problem declaration" + p.getProblem().getMessageWithLocation());
			MyLogger.exitOnError();
		}
		else
		{
			MyLogger.logImportant("Unexpected declaration type here: " + declaration.getClass().getCanonicalName());
			MyLogger.exitOnError();
		}
		return ret;
	}

	private List<IType> evaluateDeclarationReturnCppTypes(IASTDeclaration declaration) throws DOMException
	{
		List<IType> ret = new ArrayList<IType>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			MyLogger.log("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
			{
				IBinding binding  = decl.getName().resolveBinding();

				if (binding instanceof IVariable)
				{
					ret.add(((IVariable) binding).getType());
				}
				else if (binding instanceof IFunction)
				{
					ret.add(((IFunction) binding).getType());
				}
				else
				{
					MyLogger.logImportant("binding not a variable or function:" + binding.getName());
					ret.add(null);
				}
			}
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			MyLogger.logImportant("Problem declaration" + p.getProblem().getMessageWithLocation());
			MyLogger.exitOnError();
		}
		else
		{
			MyLogger.logImportant("Unexpected declaration type here: " + declaration.getClass().getCanonicalName());
			MyLogger.exitOnError();
		}
		return ret;
	}

	IType eval1DeclReturnCppType(IASTDeclaration decl) throws DOMException
	{
		return evaluateDeclarationReturnCppTypes(decl).get(0);
	}

	private String evaluateDeclSpecifierReturnType(IASTDeclSpecifier declSpecifier) throws DOMException
	{
//		evaluateStorageClass(declSpecifier.getStorageClass());
//
//		if (declSpecifier instanceof IASTCompositeTypeSpecifier)
//		{
//			IASTCompositeTypeSpecifier compositeTypeSpecifier = (IASTCompositeTypeSpecifier)declSpecifier;
//			print("composite type specifier");
//
//			getSimpleName(compositeTypeSpecifier.getName());
//
//			for (IASTDeclaration decl : compositeTypeSpecifier.getMembers())
//			{
//				evaluate(decl);
//			}
//
//			if (compositeTypeSpecifier instanceof ICPPASTCompositeTypeSpecifier)
//			{
//				ICPPASTCompositeTypeSpecifier cppCompositeTypeSpecifier = (ICPPASTCompositeTypeSpecifier)compositeTypeSpecifier;
//
//				for (ICPPASTBaseSpecifier base : cppCompositeTypeSpecifier.getBaseSpecifiers())
//					getSimpleName(base.getName());
//			}
//		}
//		else if (declSpecifier instanceof IASTElaboratedTypeSpecifier)
//		{
//			IASTElaboratedTypeSpecifier elaboratedTypeSpecifier = (IASTElaboratedTypeSpecifier)declSpecifier;
//			print("elaborated type specifier" + elaboratedTypeSpecifier.getRawSignature());
//
//			evaluateElaborated(elaboratedTypeSpecifier.getKind());
//			getSimpleName(elaboratedTypeSpecifier.getName());
//
//			if (declSpecifier instanceof ICPPASTElaboratedTypeSpecifier)
//			{
//				//				ICPPASTElaboratedTypeSpecifier elaborated = (ICPPASTElaboratedTypeSpecifier) declSpecifier;
//				print("cpp elaborated");
//			}
//		}
//		else if (declSpecifier instanceof IASTEnumerationSpecifier)
//		{
//			IASTEnumerationSpecifier enumerationSpecifier = (IASTEnumerationSpecifier)declSpecifier;
//			IASTEnumerator[] enumerators = enumerationSpecifier.getEnumerators();
//
//			getSimpleName(enumerationSpecifier.getName());
//			//db.insertEnum(enumerationSpecifier.getName().getRawSignature(), "", enumerationSpecifier.getContainingFilename());
//
//			for (IASTEnumerator enumerator : enumerators)
//			{
//				getSimpleName(enumerator.getName());
//				evalExpr(enumerator.getValue());
//			}
//		}
		if (declSpecifier instanceof IASTNamedTypeSpecifier)
		{
			IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier)declSpecifier;
			MyLogger.log("named type");

			return TypeHelpers.getSimpleName(namedTypeSpecifier.getName());

			//			if (declSpecifier instanceof ICPPASTNamedTypeSpecifier)
			//			{
			//				//				ICPPASTNamedTypeSpecifier named = (ICPPASTNamedTypeSpecifier) declSpecifier;
			//				print("cpp named");
			//			}
		}
//		else if (declSpecifier instanceof IGPPASTSimpleDeclSpecifier)
//		{
//			IGPPASTSimpleDeclSpecifier simpleTypeSpecifier = (IGPPASTSimpleDeclSpecifier)declSpecifier;
//			print("gpp simple decl specifier");
//			if (simpleTypeSpecifier.isLongLong())
//				return ast.newPrimitiveType(PrimitiveType.LONG);
//		}
		else if (declSpecifier instanceof IASTSimpleDeclSpecifier)
		{
			IASTSimpleDeclSpecifier simple = (IASTSimpleDeclSpecifier) declSpecifier;
			MyLogger.log("simple decl specifier");
//
//			if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier)
//			{
//				//				ICPPASTSimpleDeclSpecifier simple2 = (ICPPASTSimpleDeclSpecifier) declSpecifier;
//				print("cpp simple");
//			}

			return TypeHelpers.evaluateSimpleType(simple.getType(), simple.isShort(), simple.isLong(), simple.isUnsigned());
		}
		else if (declSpecifier instanceof ICASTDeclSpecifier)
		{
			//			ICASTDeclSpecifier spec = (ICASTDeclSpecifier) declSpecifier;
			MyLogger.log("C declaration specifier (unimplemented)");
		}

		return null;
	}

	/**
	 * Attempts to evaluate the given declaration specifier
	 */
	private CppDeclaration evalDeclSpecifier(IASTDeclSpecifier declSpecifier) throws DOMException
	{
		if (declSpecifier instanceof IASTCompositeTypeSpecifier)
		{
			IASTCompositeTypeSpecifier compositeTypeSpecifier = (IASTCompositeTypeSpecifier)declSpecifier;
			MyLogger.log("composite type specifier");

			CppClass tyd = new CppClass();
			tyd.isNested = addDeclaration(tyd);
			
			CompositeInfo info = getCurrentCompositeInfo();

			if (compositeTypeSpecifier.getKey() == IASTCompositeTypeSpecifier.k_union)
				tyd.isUnion = true;
			
			if (TypeHelpers.getSimpleName(compositeTypeSpecifier.getName()).equals("MISSING"))
				tyd.name = "AnonClass" + anonClassCount++;
			else
				tyd.name = TypeHelpers.getSimpleName(compositeTypeSpecifier.getName());

			info.declSpecifier = declSpecifier;
			findSpecialMethods(declSpecifier, info);
		
			// TODOtyd.typeParameters().addAll(templateParamsQueue);			
			// TODOtemplateParamsQueue.clear();
			
			if (compositeTypeSpecifier instanceof ICPPASTCompositeTypeSpecifier)
			{
				ICPPASTCompositeTypeSpecifier cppCompositeTypeSpecifier = (ICPPASTCompositeTypeSpecifier)compositeTypeSpecifier;

				if (cppCompositeTypeSpecifier.getBaseSpecifiers() != null && cppCompositeTypeSpecifier.getBaseSpecifiers().length != 0)
				{
					info.hasSuper = true;
					info.superClass = tyd.superclass = TypeHelpers.getSimpleName(cppCompositeTypeSpecifier.getBaseSpecifiers()[0].getName());
				}
				

				for (int i = 0; i < cppCompositeTypeSpecifier.getBaseSpecifiers().length; i++)
					tyd.additionalSupers.add(TypeHelpers.getSimpleName(cppCompositeTypeSpecifier.getBaseSpecifiers()[i].getName()));
			}

			for (IASTDeclaration decl : compositeTypeSpecifier.getMembers())
			{
				evalDeclaration(decl);
			}
			
			if (!info.hasCtor)
			{
				// Generate a constructor.
				CppCtor ctor = new CppCtor();
				ctor.type = tyd.name;
				
				MCompoundStmt blk = new MCompoundStmt();
				ctor.body = blk;
				
				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);
				generateCtorStatements(fields, ctor.body);

				if (info.hasSuper)
				{
					MSuperStmt sup = new MSuperStmt();
					blk.statements.add(0, sup);
				}
				
				tyd.declarations.add(ctor);
			}
			
			if (!info.hasDtor)
			{
				// Generate desctructor.
				CppDtor dtor = new CppDtor();
				
				MCompoundStmt blk = new MCompoundStmt();
				dtor.body = blk;
				
				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);
				generateDtorStatements(fields, dtor.body, info.hasSuper);

				if (info.hasSuper)
				{
					MSuperDtorStmt sup = new MSuperDtorStmt();
					blk.statements.add(0, sup);
				}

				tyd.declarations.add(dtor);
			}
			
			if (!info.hasAssign)
			{
				CppAssign ass = new CppAssign();
				
				ass.type = tyd.name;
				ass.body = new MCompoundStmt();
				
				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);

				MCompoundStmt ifBlock = new MCompoundStmt();

				if (info.hasSuper)
				{
					MSuperAssignStmt sup = new MSuperAssignStmt();
					ifBlock.statements.add(sup);
				}

				for (FieldInfo fieldInfo : fields)
				{
					MyLogger.log(fieldInfo.field.getName());

					if (fieldInfo.isStatic)
						/* Do nothing. */ ;
					else if (fieldInfo.init != null &&
							TypeHelpers.getTypeEnum(fieldInfo.field.getType()) != TypeEnum.BASIC_ARRAY)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						ifBlock.statements.add(ModelCreation.createMethodCall("this", fieldInfo.field.getName(), "opAssign", right));
					}
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.OBJECT_ARRAY)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression left = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						ifBlock.statements.add(ModelCreation.createMethodCall("CPP", "assignArray", left, right));
					}
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.BASIC_ARRAY)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression left = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						String methodName = "assignBasicArray";

						if (ctx.exprEvaluator.getArraySizeExpressions(fieldInfo.field.getType()).size() > 1)
							methodName = "assignMultiArray";

						ifBlock.statements.add(ModelCreation.createMethodCall("CPP", methodName, left, right));
					}
					else
					{
						MStmt stmt = ModelCreation.createExprStmt(
								ModelCreation.createInfixExpr("this", fieldInfo.field.getName(), "right", fieldInfo.field.getName(), "="));
						ifBlock.statements.add(stmt);
					}
				}

				if (!ifBlock.statements.isEmpty())
				{	// if (right != this) { ... } 
					MExpression expr = ModelCreation.createInfixExpr(
							ModelCreation.createLiteral("right"),
							ModelCreation.createLiteral("this"),
							"!=");
					
					MIfStmt stmt = new MIfStmt();

					stmt.condition = expr;
					stmt.body = ifBlock;

					ass.body.statements.add(stmt);
				}

				MReturnStmt retu = new MReturnStmt();
				retu.expr = ModelCreation.createLiteral("this");
				
				ass.body.statements.add(retu);

				tyd.declarations.add(ass);
			}
			
			if (!info.hasCopy)
			{
				CppFunction meth = new CppFunction();
				meth.retType = "";
				meth.name = tyd.name;
				meth.isCtor = true;
				
				MSimpleDecl var = new MSimpleDecl();
				var.type = tyd.name;
				var.name = "right";

				meth.args.add(var);
				meth.body = new MCompoundStmt();
				
				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);

				if (info.hasSuper)
				{
					// super(right);
					MStmt sup = ModelCreation.createExprStmt(
							ModelCreation.createFuncCall("super", ModelCreation.createLiteral("right")));
					
					meth.body.statements.add(sup);
				}

				for (FieldInfo fieldInfo : fields)
				{
					MyLogger.log(fieldInfo.field.getName());

					if (fieldInfo.isStatic)
						/* Do nothing. */ ;
					else if (fieldInfo.init != null &&
							TypeHelpers.getTypeEnum(fieldInfo.field.getType()) != TypeEnum.BASIC_ARRAY)
					{
						// this.field = right.field.copy();
						MFieldReferenceExpression fr1 = ModelCreation.createFieldReference("right", fieldInfo.field.getName());	
						MFieldReferenceExpression fr2 = ModelCreation.createFieldReference(fr1, "copy"); 
						MFieldReferenceExpression fr3 = ModelCreation.createFieldReference("this", fieldInfo.field.getName());

						MFunctionCallExpression fcall = new MFunctionCallExpression();
						fcall.name = fr2;
					
						MExpression infix = ModelCreation.createInfixExpr(fr3, fcall, "=");
						meth.body.statements.add(ModelCreation.createExprStmt(infix));
					}
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.OBJECT_ARRAY)
					{
						// this.field = CPP.copyArray(right.field);
						MFieldReferenceExpression fr1 = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						MFieldReferenceExpression fr2 = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression fr3 = ModelCreation.createFieldReference("CPP", "copyArray");

						MFunctionCallExpression fcall = new MFunctionCallExpression();
						fcall.name = fr3;
						fcall.args.add(fr2);

						MExpression infix = ModelCreation.createInfixExpr(fr1, fcall, "=");						
						meth.body.statements.add(ModelCreation.createExprStmt(infix));
					}
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.BASIC_ARRAY)
					{
						// this.field = CPP.copy*Array(right.field);
						MFieldReferenceExpression fr1 = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						MFieldReferenceExpression fr2 = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression fr3 = ModelCreation.createFieldReference("CPP", ctx.exprEvaluator.getArraySizeExpressions(fieldInfo.field.getType()).size() > 1 ? "copyMultiArray" : "copyBasicArray");

						MFunctionCallExpression fcall = new MFunctionCallExpression();
						fcall.name = fr3;
						fcall.args.add(fr2);
						
						MExpression infix = ModelCreation.createInfixExpr(fr1, fcall, "=");						
						meth.body.statements.add(ModelCreation.createExprStmt(infix));
//						CastExpression cast = ast.newCastExpression();
//						//cast.setType(cppToJavaType(fieldInfo.field.getType()));
//						cast.setExpression(meth3);
					}
					else
					{
						// this.field = right.field;
						MFieldReferenceExpression fr1 = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						MFieldReferenceExpression fr2 = ModelCreation.createFieldReference("right", fieldInfo.field.getName());

						MExpression infix = ModelCreation.createInfixExpr(fr1, fr2, "=");						
						meth.body.statements.add(ModelCreation.createExprStmt(infix));
					}
				}
				tyd.declarations.add(meth);
			}
			
			// Add a copy method that calls the copy constructor.
			CppFunction meth = new CppFunction();
			meth.retType = tyd.name;
			meth.name = "copy";

			MClassInstanceCreation create = new MClassInstanceCreation();
			create.name = ModelCreation.createLiteral(tyd.name);
			create.args.add(ModelCreation.createLiteral("this"));
			
			MReturnStmt stmt = new MReturnStmt();
			stmt.expr = create;
			
			MCompoundStmt blk = new MCompoundStmt();
			blk.statements.add(stmt);
			meth.body = blk;

			tyd.declarations.add(meth);	
			popDeclaration();
		}
		else if (declSpecifier instanceof IASTElaboratedTypeSpecifier)
		{
			IASTElaboratedTypeSpecifier elaboratedTypeSpecifier = (IASTElaboratedTypeSpecifier)declSpecifier;
			MyLogger.log("elaborated type specifier" + elaboratedTypeSpecifier.getRawSignature());

			//evaluateElaborated(elaboratedTypeSpecifier.getKind());
			TypeHelpers.getSimpleName(elaboratedTypeSpecifier.getName());

			if (declSpecifier instanceof ICPPASTElaboratedTypeSpecifier)
			{
				//				ICPPASTElaboratedTypeSpecifier elaborated = (ICPPASTElaboratedTypeSpecifier) declSpecifier;
				MyLogger.log("cpp elaborated");
			}
		}
		else if (declSpecifier instanceof IASTEnumerationSpecifier)
		{
			IASTEnumerationSpecifier enumerationSpecifier = (IASTEnumerationSpecifier)declSpecifier;
			ctx.enumMngr.evalDeclEnum(enumerationSpecifier);
		}
		else if (declSpecifier instanceof IASTNamedTypeSpecifier)
		{
			IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier)declSpecifier;
			MyLogger.log("named type");
			TypeHelpers.getSimpleName(namedTypeSpecifier.getName());

			if (declSpecifier instanceof ICPPASTNamedTypeSpecifier)
			{
				//				ICPPASTNamedTypeSpecifier named = (ICPPASTNamedTypeSpecifier) declSpecifier;
				MyLogger.log("cpp named");
			}
		}
		else if (declSpecifier instanceof IASTSimpleDeclSpecifier)
		{
			IASTSimpleDeclSpecifier simple = (IASTSimpleDeclSpecifier) declSpecifier;
			MyLogger.log("simple decl specifier");

			if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier)
			{
				//				ICPPASTSimpleDeclSpecifier simple2 = (ICPPASTSimpleDeclSpecifier) declSpecifier;
				MyLogger.log("cpp simple");
			}

			TypeHelpers.evaluateSimpleType(simple.getType(), simple.isShort(), simple.isLong(), simple.isUnsigned());
		}
		else if (declSpecifier instanceof ICASTDeclSpecifier)
		{
			//			ICASTDeclSpecifier spec = (ICASTDeclSpecifier) declSpecifier;
			MyLogger.log("C declaration specifier (unimplemented)");
		}

		return null;
	}


//	private MExpression generateArrayCreationExpression(IType tp, List<MExpression> sizeExprs) throws DOMException
//	{
//		Type jtp = null;
//		boolean isBasic = false;
//		
//		if ((getTypeEnum(tp) == TypeEnum.ARRAY))
//		{
//			jtp = cppToJavaType(getArrayBaseType(tp));
//			TypeEnum te = getTypeEnum(getArrayBaseType(tp));
//			isBasic = te == TypeEnum.BOOLEAN || te == TypeEnum.CHAR || te == TypeEnum.NUMBER; 
//		}
//		else if ((getTypeEnum(tp) == TypeEnum.POINTER))
//		{
//			jtp = cppToJavaType(getPointerBaseType(tp));
//			TypeEnum te = getTypeEnum(getPointerBaseType(tp));
//			isBasic = te == TypeEnum.BOOLEAN || te == TypeEnum.CHAR || te == TypeEnum.NUMBER; 
//		}
//		else
//		{
//			printerr("unexpected type here: " + tp.getClass().getCanonicalName());
//			System.exit(-1);
//		}
//
//		if (!isBasic)
//		{
//			TypeLiteral tl = ast.newTypeLiteral();
//			tl.setType(jtp);
//
//			MethodInvocation meth = jast.newMethod()
//					.on("CreateHelper")
//					.call("allocateArray")
//					.with(tl)
//					.withArguments(sizeExprs).toAST();
//
//			CastExpression cast = ast.newCastExpression();
//			cast.setExpression(meth);
//			cast.setType(cppToJavaType(tp));
//
//			return cast;
//		}
//		else
//		{
//			ArrayCreation create = ast.newArrayCreation();
//			if ((jtp instanceof ArrayType))
//			{
//				create.setType((ArrayType) jtp);
//			}
//			else
//			{
//				ArrayType arr = ast.newArrayType(jtp);
//				create.setType(arr);
//			}
//			
//			create.dimensions().addAll(sizeExprs);
//			return create;
//		}
//	}
	
	/**
	 * Returns the names contained in a declaration.
	 * Eg. int a, b, * c; will return [a, b, c].
	 */
	List<String> evaluateDeclarationReturnNames(IASTDeclaration declaration) throws DOMException
	{
		List<String> ret = new ArrayList<String>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			MyLogger.log("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
				ret.add(TypeHelpers.getSimpleName(decl.getName()));
		}
		else if (declaration instanceof ICPPASTExplicitTemplateInstantiation)
		{
			ICPPASTExplicitTemplateInstantiation explicitTemplateInstantiation = (ICPPASTExplicitTemplateInstantiation)declaration;
			MyLogger.logImportant("explicit template instantiation");
			MyLogger.exitOnError();
			//evaluate(explicitTemplateInstantiation.getDeclaration());
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			MyLogger.logImportant("Problem declaration" + p.getProblem().getMessageWithLocation());
			MyLogger.exitOnError();
		}
		return ret;
	}
	
	MExpression callCopyIfNeeded(MExpression expr, IASTExpression cppExpr) throws DOMException
	{
//		TypeEnum te = getTypeEnum(cppExpr.getExpressionType());

		if (expr instanceof MClassInstanceCreation)
			return expr;
		
		return expr;
//		if (te != TypeEnum.POINTER &&
//			te != TypeEnum.REFERENCE)
//		{
//			MFunctionCallExpression copy = new MFunctionCallExpression();
//			MFieldReferenceExpression field = ModelCreation.createFieldReference(expr, "copy");
//			copy.name = field;
//			return copy;
//		}
//		else
//		{
//			MFunctionCallExpression copy = new MFunctionCallExpression();
//			MFieldReferenceExpression field = ModelCreation.createFieldReference(expr, "ptrCopy");
//			copy.name = field;
//			return copy;
//		}
	}

	MExpression eval1Init(IASTInitializer initializer) throws DOMException 
	{
		IASTEqualsInitializer eq = (IASTEqualsInitializer) initializer;
		IASTInitializerClause clause = eq.getInitializerClause();
		IASTExpression expr = (IASTExpression) clause;
		
		return ctx.exprEvaluator.eval1Expr(expr);
	}

	/**
	 * Given a C++ initializer, returns a list of Java expressions.
	 * 
	 * @param iastDeclaration Initializer
	 */
	MSimpleDecl eval1Decl(IASTDeclaration decla) throws DOMException
	{
		MSimpleDecl dec = new MSimpleDecl();

		IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) decla;
		MyLogger.log("simple declaration");
			
		IASTDeclarator decl = simpleDeclaration.getDeclarators()[0];

		IASTName nm = decl.getName();
		IBinding binding = nm.resolveBinding();
		IVariable var = (IVariable) binding;
			
//		TypeEnum type = getTypeEnum(var.getType());

		dec.name = TypeHelpers.getSimpleName(decl.getName());
		dec.initExpr = eval1Init(decl.getInitializer());
		dec.type = TypeHelpers.cppToJavaType(var.getType());
		
		return dec;
		
//		if (type == TypeEnum.OBJECT || type == TypeEnum.REFERENCE)
//		{
//			MClassInstanceCreation create = new MClassInstanceCreation();
//			//create.args.addAll(evaluate(decl.getInitializer()));
//			ret.add(create);
//		}
//		else if (type == TypeEnum.ARRAY)
//		{
//			//						print("Found array");
//			//						//Expression ex = jast.newType(generateArrayCreationExpression(var.getType(), getArraySizeExpressions(var.getType())));
//			//						Expression ex = jast.newNumber(0);
//			//						TypeEnum te = getTypeEnum(getArrayBaseType(var.getType()));
//			//						
//			//						wrap = false;
//			//						if ((te == TypeEnum.OBJECT || te == TypeEnum.REFERENCE || te == TypeEnum.POINTER) &&
//			//							wrap)
//			//						{
//			////							MethodInvocation meth = createAddItemCall(ex);
//			////							ret.set(ret.size() - 1, meth);
//			//						}
//			//						else
//			//							ret.set(ret.size() - 1, ex);
//		}
//		else
//		{
//			//						evaluate(decl.getInitializer());
//			//						if (!exprs.isEmpty())
//			//							ret.set(ret.size() - 1, evaluate(decl.getInitializer()).get(0));	
//		}
	}

	/**
	 * Given a typeId returns a Java type. Used in sizeof, etc. 
	 */
	String evalTypeId(IASTTypeId typeId) throws DOMException
	{
		if (typeId != null)
		{
			return evaluateDeclSpecifierReturnType(typeId.getDeclSpecifier());
		}
		return null;
	}

	/**
	 * Given a declaration like: int a = 5;
	 * Returns an infix expression: a.set(5);
	 * Optionally the return expression is made into a boolean expression.
	 * Used to split up the declaration into two parts for if, while,
	 * switch and for condition declarations.
	 * Example:
	 *   while (int a = 10) {}
	 * becomes:
	 *   MInteger a = new MInteger();
	 *   while ((a.set(10)) != 0) {}
	 */
	MExpression makeInfixFromDecl(String varName, MExpression initExpr, IType tp, boolean makeBoolean) throws DOMException
	{
		TypeEnum te = TypeHelpers.getTypeEnum(tp);
		MInfixExpression infix = null;
		
		if (te == TypeEnum.CHAR || te == TypeEnum.BOOLEAN || te == TypeEnum.NUMBER)
		{
			infix = new MInfixAssignmentWithNumberOnLeft();
			infix.left = ModelCreation.createNumberId(varName);
		}
		else
		{
			infix = new MInfixExpressionPlain();
			infix.left = ModelCreation.createLiteral(varName);
		}
		// TODO pointers.
		
		infix.right = initExpr;
		infix.operator = "=";

		return makeBoolean ? ExpressionHelpers.makeExpressionBoolean(infix, te) : ExpressionHelpers.bracket(infix);
	}


	/**
	 * Attempts to convert a C++ catch clause to Java.
	 */
//	private CatchClause evaluateCatchClause(IASTStatement statement) throws DOMException
//	{
//		if (statement instanceof ICPPASTCatchHandler)
//		{
//			ICPPASTCatchHandler catchHandler = (ICPPASTCatchHandler)statement;
//			print("catch");
//
//			CatchClause catchcls = ast.newCatchClause();
//			catchcls.setBody((Block) evalStmt(catchHandler.getCatchBody()));
//			catchcls.setException(evaluateDeclarationReturnSingleVariable(catchHandler.getDeclaration()));
//			return catchcls;
//		}
//		return null;
//	}

	/**
	 * Given a declaration, returns a single variable. This is used for 
	 * function arguments.
	 */
	private MSimpleDecl evaluateDeclarationReturnSingleVariable(IASTDeclaration declaration) throws DOMException
	{
		MSimpleDecl decl = new MSimpleDecl();
		
		List<String> names = evaluateDeclarationReturnNames(declaration);
		decl.name = (names.get(0));
		decl.type = (evaluateDeclarationReturnTypes(declaration).get(0));
		return decl;
	}

	static class FieldInfo
	{
		FieldInfo(IASTDeclarator declaratorArg, MExpression initArg, IField fieldArg)
		{
			declarator = declaratorArg;
			init = initArg;
			field = fieldArg;
		}
		
		final IASTDeclarator declarator;
		final IField field;
		MExpression init;
		boolean isBitfield;
		boolean isStatic;
	}
}
