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
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;

import com.github.danfickle.cpptojavasourceconverter.InitializationManager.InitType;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.NameType;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.TypeType;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.*;

/**
 * @author DanFickle
 * http://github.com/danfickle/cpp-to-java-source-converter/
 */
public class SourceConverter
{
	private final TranslationUnitContext ctx;
	
	SourceConverter(TranslationUnitContext con) {
		ctx = con;
	}
	
	/**
	 * This class keeps track of all the info on a C++ class,
	 * struct or union.
	 */
	static class CompositeInfo
	{
		CompositeInfo(CppClass tydArg)
		{
			tyd = tydArg;
		}
		
		CppClass tyd;
		IASTDeclSpecifier declSpecifier;
		String superClass;
		boolean hasCtor;
		boolean hasDtor;
		boolean hasCopy;
		boolean hasAssign;
		boolean hasSuper;
	}
	
	/**
	 * The stack of classes/structs.
	 */
	Deque<CompositeInfo> currentInfoStack = new ArrayDeque<CompositeInfo>();
	
	/**
	 * Generates a Java field, given a C++ field.
	 */
	private void generateField(IBinding binding, IASTDeclarator declarator, MExpression init) throws DOMException
	{
		IField ifield = (IField) binding;

		MSimpleDecl frag = ctx.declModels.new MSimpleDecl();
		frag.name = TypeManager.cppNameToJavaName(ifield.getName(), NameType.CAMEL_CASE);

		if (ifield.isStatic())
		{
			frag.isStatic = true;
			frag.initExpr = init;
		}

		frag.type = ctx.typeMngr.cppToJavaType(ifield.getType(), TypeType.INTERFACE);
		frag.isPublic = true;
		
		if (currentInfoStack.peekFirst() != null)
		{
			// We are inside a class decl so add it directly.
			currentInfoStack.peekFirst().tyd.declarations.add(frag);
		}
		else
		{
			// We are outside the class decl, so we need to find the previous
			// declaration of this field and update it.
			// This can only happen for static fields being
			// initialized outside the class decl.
			CppClass cls = (CppClass) ctx.typeMngr.getDeclFromType(evalBindingReturnType(ifield.getOwner()));

			boolean found = false;
			
			for (CppDeclaration decl : cls.declarations)
			{
				if (decl instanceof MSimpleDecl &&
					frag.name.equals(decl.name))
				{
					((MSimpleDecl) decl).initExpr = init;
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				MyLogger.logImportant("Field not found: " + ifield.getName());
			}
		}
	}
	
	/**
	 * Generates a Java field, given a C++ top-level (global) variable.
	 */
	private void generateVariable(IBinding binding, IASTDeclarator declarator, MExpression init) throws DOMException
	{
		IVariable ifield = (IVariable) binding;

		MSimpleDecl frag = ctx.declModels.new MSimpleDecl();
		frag.name = TypeManager.cppNameToJavaName(ifield.getName(), NameType.CAMEL_CASE);
		frag.initExpr = init;
		frag.isStatic = true;
		
		frag.type = ctx.typeMngr.cppToJavaType(ifield.getType(), TypeType.INTERFACE);
		frag.isPublic = true;
		
		// TODO: Add somewhere.
		// TODO: Global variables can be seen multiple times.
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
				// We've found a function definition (ie. includes a body).
				if (((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding() instanceof ICPPConstructor)
				{
					info.hasCtor = true;

					ICPPConstructor ctor = (ICPPConstructor) ((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding(); 
					ICPPParameter[] params = ctor.getParameters();

					if (params.length == 1 && TypeManager.isOneOf(params[0].getType(), TypeEnum.OBJECT_REFERENCE))
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
			else if (decl instanceof IASTSimpleDeclaration)
			{
				IASTSimpleDeclaration simple = (IASTSimpleDeclaration) decl;
				
				// We've found a declarator, which maybe a function declarator.
				if (simple.getDeclarators().length != 0)
				{
					if (simple.getDeclarators()[0].getName().resolveBinding() instanceof ICPPConstructor)
					{
						info.hasCtor = true;
						
						ICPPConstructor ctor = (ICPPConstructor) simple.getDeclarators()[0].getName().resolveBinding();
						ICPPParameter[] params = ctor.getParameters();

						if (params.length == 1 && TypeManager.isOneOf(params[0].getType(), TypeEnum.OBJECT_REFERENCE))
						{
							// TODO: We should check there are no params or others have default values...
							info.hasCopy = true;
						}
					}
					else if (simple.getDeclarators()[0].getName().resolveBinding() instanceof ICPPMethod)
					{
						ICPPMethod meth = (ICPPMethod) simple.getDeclarators()[0].getName().resolveBinding();
						
						if (meth.isDestructor())
							info.hasDtor = true;
						else if (meth.getName().equals("operator ="))
							info.hasAssign = true;
					}
				}
			}
		}
	}
	
	/**
	 * This method creates a list of fields present in the class.
	 * This is used to generate ctor, dtor, assign and copy statements.
	 * Note: Includes static fields.
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

				List<MExpression> exprs = evaluateDeclarationReturnInitializers(simple, InitType.WRAPPED);				
				
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
	
	/**
	 * Returns one initializer for each declarator.
	 * This can be the provided C++ initializer or it can be a generated
	 * initializer for objects.
	 * 
	 * Example:
	 *   int i, j = 5, * p;
	 * returns:
	 *   [MInteger.valueOf(0), MInteger.valueOf(5), MInteger.valueOf(0)]
	 */
	List<MExpression> evaluateDeclarationReturnInitializers(IASTSimpleDeclaration simple, InitType initType) throws DOMException 
	{
		List<MExpression> exprs = new ArrayList<MExpression>();
		List<IType> types = evaluateDeclarationReturnCppTypes(simple);

		int i = 0;
		for (IASTDeclarator decl : simple.getDeclarators())
		{
			exprs.add(ctx.initMngr.eval1Init(decl.getInitializer(), types.get(i), decl.getName(), initType));
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
				MyLogger.logImportant("Problem function: " + ((IProblemBinding) bind).getFileName() + ((IProblemBinding) bind).getLineNumber());
			else
				MyLogger.logImportant("Function with unknown binding: " + bind.getClass().getCanonicalName());
		}
		else if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simple = (IASTSimpleDeclaration) declaration;
			evalDeclSpecifier(simple.getDeclSpecifier());

			List<MExpression> exprs = evaluateDeclarationReturnInitializers(simple, InitType.WRAPPED);
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
					CppFunction func = (CppFunction) ctx.typeMngr.getDeclFromTypeName(evalBindingReturnType(binding), declarator.getName()); 
							
					if (func == null)
					{
						func = ctx.declModels.new CppFunction();
						ctx.typeMngr.registerDecl(func, evalBindingReturnType(binding), 
								declarator.getName(), NameType.CAMEL_CASE, declarator.getContainingFilename(),
								declarator.getFileLocation().getStartingLineNumber());
					}
					
					ctx.funcMngr.makeDefaultCalls((IASTFunctionDeclarator) declarator, binding, func.parent);
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
			MyLogger.logImportant("Problem declaration: " + ":" + p.getRawSignature());
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
				ret.add(ctx.typeMngr.cppToJavaType(evalBindingReturnType(binding), TypeType.INTERFACE));
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

	/**
	 * Given a semantic binding returns the C++ IType.
	 * Note: Not all cases are handled, so add as needed.
	 */
	IType evalBindingReturnType(IBinding binding)
	{
		if (binding instanceof IVariable)
		{
			return (((IVariable) binding).getType());
		}
		else if (binding instanceof IFunction)
		{
			return (((IFunction) binding).getType());
		}
		else if (binding instanceof IParameter)
		{
			return (((IParameter) binding).getType());
		}
		else if (binding instanceof IField)
		{
			return (((IField) binding).getType());
		}
		else if (binding instanceof ICompositeType)
		{
			return ((ICompositeType) binding);
		}
		else if (binding instanceof IEnumeration)
		{
			return (((IEnumeration) binding));
		}
		else if (binding instanceof ICPPNamespace)
		{
			return null;
		}
		else if (binding == null)
		{
			return null;
		}
		else
		{
			MyLogger.logImportant("binding not a variable, field or function:" + binding.getName());
			return (null);
		}
	}
	
	/**
	 * Given a declaration, returns a IType for each declarator.
	 * For example int * i, j; would return [int *, int].
	 */
	private List<IType> evaluateDeclarationReturnCppTypes(IASTDeclaration declaration) throws DOMException
	{
		List<IType> ret = new ArrayList<IType>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			MyLogger.log("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
			{
				IBinding binding = decl.getName().resolveBinding();
				ret.add(evalBindingReturnType(binding));
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

	/**
	 * Convenience method for when only one declarator is supported
	 * such as inside while, if, for and switch.
	 */
	IType eval1DeclReturnCppType(IASTDeclaration decl) throws DOMException
	{
		assert(evaluateDeclarationReturnCppTypes(decl).size() == 1);
		return evaluateDeclarationReturnCppTypes(decl).get(0);
	}

	/**
	 * Attempts to evaluate the given declaration specifier
	 */
	private void evalDeclSpecifier(IASTDeclSpecifier declSpecifier) throws DOMException
	{
		if (declSpecifier instanceof IASTCompositeTypeSpecifier)
		{
			IASTCompositeTypeSpecifier compositeTypeSpecifier = (IASTCompositeTypeSpecifier)declSpecifier;
			MyLogger.log("composite type specifier");

			IType myType = evalBindingReturnType(compositeTypeSpecifier.getName().resolveBinding());
			
			// Check that this decl specifier has not
			// already been registered.
			CppDeclaration myDecl = ctx.typeMngr.getDeclFromTypeName(myType, compositeTypeSpecifier.getName());
			if (myDecl != null)
				return;

			CppClass tyd = ctx.declModels.new CppClass();
			
			ctx.typeMngr.registerDecl(tyd, myType, compositeTypeSpecifier.getName(), NameType.CAPITALIZED, compositeTypeSpecifier.getContainingFilename(), compositeTypeSpecifier.getFileLocation().getStartingLineNumber());
			
			CompositeInfo info = new CompositeInfo(tyd);
			currentInfoStack.addFirst(info);

			if (compositeTypeSpecifier.getKey() == IASTCompositeTypeSpecifier.k_union)
				tyd.isUnion = true;
			
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
					info.superClass = tyd.superclass = TypeManager.cppNameToJavaName(cppCompositeTypeSpecifier.getBaseSpecifiers()[0].getName().resolveBinding().getName(), NameType.CAPITALIZED);
				}
				
				for (int i = 0; i < cppCompositeTypeSpecifier.getBaseSpecifiers().length; i++)
					tyd.additionalSupers.add(TypeManager.getSimpleName(cppCompositeTypeSpecifier.getBaseSpecifiers()[i].getName()));
			}

			for (IASTDeclaration decl : compositeTypeSpecifier.getMembers())
			{
				evalDeclaration(decl);
			}
			
			if (!info.hasCtor)
			{
				// Generate a constructor.
				CppCtor ctor = ctx.declModels.new CppCtor();
				ctor.type = tyd.name;
				
				MCompoundStmt blk = ctx.stmtModels.new MCompoundStmt();
				ctor.body = blk;
				
				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);
				ctx.specialGenerator.generateCtorStatements(fields, ctor.body);

				if (info.hasSuper)
				{
					MSuperStmt sup = ctx.stmtModels.new MSuperStmt();
					blk.statements.add(0, sup);
				}
				
				tyd.declarations.add(ctor);
			}
			
			if (!info.hasDtor)
			{
				// Generate desctructor.
				CppDtor dtor = ctx.declModels.new CppDtor();
				
				MCompoundStmt blk = ctx.stmtModels.new MCompoundStmt();
				dtor.body = blk;
				
				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);
				ctx.specialGenerator.generateDtorStatements(fields, dtor.body, info.hasSuper);
				tyd.declarations.add(dtor);
			}
			
			if (!info.hasAssign)
			{
				CppAssign ass = ctx.specialGenerator.generateAssignMethod(info, tyd, declSpecifier);
				tyd.declarations.add(ass);
			}
			
			if (!info.hasCopy)
			{
				CppFunction meth = ctx.specialGenerator.generateCopyCtor(info, tyd, declSpecifier);
				tyd.declarations.add(meth);
			}
			
			// Add a copy method that calls the copy constructor.
			CppFunction meth = ctx.declModels.new CppFunction();
			meth.retType = tyd.name;
			meth.name = "copy";
			meth.isOverride = true;

			MClassInstanceCreation create = new MClassInstanceCreation();
			create.name = ModelCreation.createLiteral(tyd.name);
			create.args.add(ModelCreation.createLiteral("this"));
			
			MReturnStmt stmt = ctx.stmtModels.new MReturnStmt();
			stmt.expr = create;
			
			MCompoundStmt blk = ctx.stmtModels.new MCompoundStmt();
			blk.statements.add(stmt);
			meth.body = blk;

			tyd.declarations.add(meth);	
			currentInfoStack.removeFirst();
		}
		else if (declSpecifier instanceof IASTElaboratedTypeSpecifier)
		{
			IASTElaboratedTypeSpecifier elaboratedTypeSpecifier = (IASTElaboratedTypeSpecifier)declSpecifier;
			MyLogger.log("elaborated type specifier" + elaboratedTypeSpecifier.getRawSignature());

			//evaluateElaborated(elaboratedTypeSpecifier.getKind());
			TypeManager.getSimpleName(elaboratedTypeSpecifier.getName());

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
			TypeManager.getSimpleName(namedTypeSpecifier.getName());

			if (declSpecifier instanceof ICPPASTNamedTypeSpecifier)
			{
				//				ICPPASTNamedTypeSpecifier named = (ICPPASTNamedTypeSpecifier) declSpecifier;
				MyLogger.log("cpp named");
			}
		}
		else if (declSpecifier instanceof IASTSimpleDeclSpecifier)
		{
			//IASTSimpleDeclSpecifier simple = (IASTSimpleDeclSpecifier) declSpecifier;
			MyLogger.log("simple decl specifier");

			if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier)
			{
				//				ICPPASTSimpleDeclSpecifier simple2 = (ICPPASTSimpleDeclSpecifier) declSpecifier;
				MyLogger.log("cpp simple");
			}

			//TypeHelpers.evaluateSimpleType(simple.getType(), simple.isShort(), simple.isLong(), simple.isUnsigned());
		}
		else if (declSpecifier instanceof ICASTDeclSpecifier)
		{
			//			ICASTDeclSpecifier spec = (ICASTDeclSpecifier) declSpecifier;
			MyLogger.log("C declaration specifier (unimplemented)");
		}
	}

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
				ret.add(TypeManager.getSimpleName(decl.getName()));
		}
		else if (declaration instanceof ICPPASTExplicitTemplateInstantiation)
		{
			//ICPPASTExplicitTemplateInstantiation explicitTemplateInstantiation = (ICPPASTExplicitTemplateInstantiation)declaration;
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

	/**
	 * Given a declaration creates one Java declaration.
	 */
	MSimpleDecl eval1Decl(IASTDeclaration decla, InitType initType) throws DOMException
	{
		MyLogger.log("simple declaration");
		
		IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) decla;
		assert(simpleDeclaration.getDeclarators().length == 1);
		
		List<MExpression> inits = evaluateDeclarationReturnInitializers(simpleDeclaration, initType);
		List<String> names = evaluateDeclarationReturnNames(simpleDeclaration);
		List<String> types = evaluateDeclarationReturnTypes(simpleDeclaration);

		MSimpleDecl dec = ctx.declModels.new MSimpleDecl();
		dec.name = names.get(0);
		dec.initExpr = inits.get(0);
		dec.type = types.get(0);
		
		return dec;
	}

	/**
	 * Given a typeId returns a binding. Used in sizeof, new, etc. 
	 */
	IBinding eval1TypeIdReturnBinding(IASTTypeId typeId) throws DOMException
	{
		IASTDeclSpecifier dspec = typeId.getDeclSpecifier();
		
		if (dspec instanceof ICPPASTNamedTypeSpecifier)
		{
			ICPPASTNamedTypeSpecifier comp = (ICPPASTNamedTypeSpecifier) dspec;
			return comp.getName().resolveBinding();
		}
		else
		{
			assert(false);
			return null;
		}
	}

	/**
	 * Given a typeId returns a binding. Used in sizeof, new, etc. 
	 */
	IASTName evalTypeIdReturnName(IASTTypeId typeId) throws DOMException
	{
		IASTDeclSpecifier dspec = typeId.getDeclSpecifier();
		
		if (dspec instanceof ICPPASTNamedTypeSpecifier)
		{
			ICPPASTNamedTypeSpecifier comp = (ICPPASTNamedTypeSpecifier) dspec;
			return comp.getName();
		}
		else
		{
			// TODO: Other decl spec types such as simple (int, short, etc).
			assert(false);
			return null;
		}
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
		MInfixExpression infix = null;
		
		if (TypeManager.isBasicType(tp))
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

		if (TypeManager.isOneOf(tp, TypeEnum.BOOLEAN))
			infix.right = ExpressionHelpers.makeExpressionBoolean(infix.right, tp);
		else
			infix.right = ExpressionHelpers.bracket(infix.right);

		infix.operator = "=";

		return (makeBoolean && !TypeManager.isOneOf(tp, TypeEnum.BOOLEAN)) ?
				ExpressionHelpers.makeExpressionBoolean(infix, tp) :
				ExpressionHelpers.bracket(infix);
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
