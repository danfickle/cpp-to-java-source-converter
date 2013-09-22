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

import com.github.danfickle.cpptojavasourceconverter.TypeHelpers.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.*;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.*;

/**
 * Second stage of the C++ to Java source converter.
 * @author DanFickle
 * http://github.com/danfickle/cpp-to-java-source-converter/
 */
public class SourceConverterStage2
{
	static class GlobalContext
	{
		SourceConverterStage2 converter;
		StackManager stackMngr;
		ExpressionEvaluator exprEvaluator;
		StmtEvaluator stmtEvaluator;
		BitfieldHelpers bitfieldHelpers;
	}
	
	private GlobalContext ctx;
	
	/**
	 * This class keeps track of all the info on a C++ class
	 * or struct.
	 */
	private static class CompositeInfo
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
	
	private List<CppDeclaration> decls2 = new ArrayList<CppDeclaration>();

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
			decls2.add(decl);
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

	private CompositeInfo getCurrentCompositeInfo()
	{
		return currentInfo;
	}
	
	/**
	 * Builds default argument function calls.
	 * For example:
	 *   int func_with_defaults(int one, int two = 5);
	 * would generate:
	 * 	 int func_with_defaults(int one) {
	 *	     return func_with_defaults(one, 5);
	 *   }
	 */
	private void makeDefaultCalls(IASTFunctionDeclarator func, IBinding funcBinding) throws DOMException
	{
		// getDefaultValues will return an item for every parameter.
		// ie. null for params without a default value.
		List<MExpression> defaultValues = getDefaultValues(func);

		// We start from the right because C++ can only have default values at the
		// right and we can stop when we get to a null (meaning there is no more defaults).
		for (int k = defaultValues.size() - 1; k >= 0; k--)
		{
			if (defaultValues.get(k) == null)
				break;

			// Create the method declaration.
			// This will handle void return types.
			CppFunction methodDef = new CppFunction();
			methodDef.name = TypeHelpers.getSimpleName(func.getName());
			methodDef.retType = evalReturnType(funcBinding);
			
			// This gets a parameter variable declaration for each param.
			List<MSimpleDecl> list = evalParameters(funcBinding);

			// Add a param declaration for each param up until we want to use
			// default values (which won't have a param).
			for (int k2 = 0; k2 < k; k2++)
				methodDef.args.add(list.get(k2));

			// This code block simple calls the original method with
			// passed in arguments plus default arguments.
			MFunctionCallExpression method = ModelCreation.createFuncCall(TypeHelpers.getSimpleName(func.getName()));

			// Add the passed in params by name.
			List<String> names = getArgumentNames(funcBinding);
			for (int k3 = 0; k3 < k; k3++)
			{
				method.args.add(ModelCreation.createLiteral(names.get(k3)));
			}

			// Now add the default values.
			List<MExpression> vals = getDefaultValues(func);
			for (int k4 = k; k4 < defaultValues.size(); k4++)
				method.args.add(vals.get(k4));

			MCompoundStmt block = new MCompoundStmt();
			
			// If not a void function, return the result of the method call.
			if (evalReturnType(funcBinding).toString().equals("void"))
			{
				MExprStmt exprStmt = ModelCreation.createExprStmt(method);
				block.statements.add(exprStmt);
			}
			else
			{
				MReturnStmt retu = new MReturnStmt();
				retu.expr = method;
				block.statements.add(retu);
			}

			methodDef.body = block;
			
			addDeclaration(methodDef);
			popDeclaration();
		}
	}
	
	/**
	 * Given a list of fields for a class, adds initialization statements
	 * to the constructor for each field as required.
	 * Initializers provided to this function are generated from C++ initializer
	 * lists, and implicit initializers for objects.
	 * Note: We must initialize in order that fields were declared.
	 */
	private void generateCtorStatements(List<FieldInfo> fields, MCompoundStmt method)
	{
		int start = 0;
		for (FieldInfo fieldInfo : fields)
		{
			print(fieldInfo.field.getName());

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
	private void generateDtorStatements(List<FieldInfo> fields, MCompoundStmt method, boolean hasSuper) throws DOMException
	{
		for (int i = fields.size() - 1; i >= 0; i--)
		{
			print(fields.get(i).field.getName());

			if (fields.get(i).isStatic)
				/* Do nothing. */ ;
			else if (TypeHelpers.getTypeEnum(fields.get(i).field.getType()) == TypeEnum.OBJECT)
			{
				// Call this.field.destruct()
				MStmt stmt = ModelCreation.createMethodCall("this", fields.get(i).field.getName(), "destruct");
				method.statements.add(stmt);
			}
			else if (TypeHelpers.getTypeEnum(fields.get(i).field.getType()) == TypeEnum.ARRAY &&
					TypeHelpers.getTypeEnum(TypeHelpers.getArrayBaseType(fields.get(i).field.getType())) == TypeEnum.OBJECT)
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

	// The return type of the function currently being evaluated.
	// We need this so we know if the return expression needs to be made
	// java boolean.
	String currentReturnType = null;
	
	/**
	 * Evaluates a function definition and converts it to Java.
	 */
	private void evalFunction(IASTDeclaration declaration) throws DOMException
	{
		IASTFunctionDefinition func = (IASTFunctionDefinition) declaration;
		IBinding funcBinding = func.getDeclarator().getName().resolveBinding();
		
		CppFunction method = new CppFunction();
		method.name = TypeHelpers.getSimpleName(func.getDeclarator().getName());
		method.isStatic = ((IFunction) funcBinding).isStatic();
		method.retType = evalReturnType(funcBinding);

		boolean isCtor = false, isDtor = false;

		if (method.retType == null || method.retType.equalsIgnoreCase("void"))
		{
			if (method.name.contains("destruct"))
			{
				method.isCtor = true;
			}
			else
			{
				method.isDtor = true;
			}
		}

		method.args.addAll(evalParameters(funcBinding));
		
		ctx.stackMngr.reset();

		// We need this so we know if the return expression needs to be made
		// java boolean.
		currentReturnType = method.retType;
		
		// eval1Stmt work recursively to generate the function body.
		method.body = (MCompoundStmt) ctx.stmtEvaluator.eval1Stmt(func.getBody());

		// For debugging purposes, we put return type back to null.
		currentReturnType = null;
		
		// If we have any local variables that are objects, or arrays of objects,
		// we must create an explicit stack so they can be added to it and explicity
		// cleaned up at termination points (return, break, continue, end block).
		if (ctx.stackMngr.getMaxLocalVariableId() != null)
		{
			MLiteralExpression expr = new MLiteralExpression();
			expr.literal = String.valueOf(ctx.stackMngr.getMaxLocalVariableId());

			MNewArrayExpressionObject array = new MNewArrayExpressionObject();
			array.type = "Object";
			array.sizes.add(expr);

			MSimpleDecl decl = new MSimpleDecl();
			decl.name = "__stack";
			decl.initExpr = array; 
			decl.type = "Object";
			
			MDeclarationStmt stmt = new MDeclarationStmt();
			stmt.simple = decl;
			
			method.body.statements.add(0, stmt);
		}

		CompositeInfo info = getCurrentCompositeInfo();
		List<FieldInfo> fields = collectFieldsForClass(info.declSpecifier);
		List<MExpression> superInit = null;
		
		if (func instanceof ICPPASTFunctionDefinition)
		{
			// Now check for C++ constructor initializers...
			ICPPASTFunctionDefinition funcCpp = (ICPPASTFunctionDefinition) func;
			ICPPASTConstructorChainInitializer[] chains = funcCpp.getMemberInitializers();

			if (chains != null && chains.length != 0)
			{
				for (ICPPASTConstructorChainInitializer chain : chains)
				{
					// We may have to call a constructor... 
					if ((chain.getMemberInitializerId().resolveBinding() instanceof IVariable &&
						((IVariable)chain.getMemberInitializerId().resolveBinding()).getType() instanceof ICompositeType)) // &&
						//!(eval1Expr(chain.getInitializerValue()) instanceof ClassInstanceCreation))
					{
						MLiteralExpression lit = 
								ModelCreation.createLiteral(TypeHelpers.cppToJavaType(((IVariable) chain.getMemberInitializerId().resolveBinding()).getType()));
						
						MClassInstanceCreation create = new MClassInstanceCreation();
						create.name = lit;
						create.args.addAll(ctx.exprEvaluator.evalExpr(chain.getInitializerValue()));
						
						// Match this initializer with the correct field.
						for (FieldInfo fieldInfo : fields)
						{
							if (chain.getMemberInitializerId().resolveBinding().getName().equals(fieldInfo.field.getName()))
								fieldInfo.init = create;
						}
					}
					else if (chain.getInitializerValue() != null)
					{
						// Match this initializer with the correct field.
						for (FieldInfo fieldInfo : fields)
						{
							if (chain.getMemberInitializerId().resolveBinding().getName().equals(fieldInfo.field.getName()))
								; // TODOfieldInfo.init = eval1Expr(chain.getInitializerValue());
						}
						
						if (info.hasSuper && chain.getMemberInitializerId().resolveBinding().getName().equals(info.superClass))
						{
							superInit = ctx.exprEvaluator.evalExpr(chain.getInitializerValue());
						}
					}
					else
					{
						// TODO...
					}
				}
			}
		}
		
		if (isCtor)
		{
			// This function generates an initializer for all fields that need initializing.
			generateCtorStatements(fields, method.body);
			
			// If we have a super class, call super constructor.
			if (info.hasSuper)
			{
				MFunctionCallExpression expr = ModelCreation.createFuncCall("super");
				
				if (superInit != null)
					expr.args.addAll(superInit);
				
				method.body.statements.add(0, ModelCreation.createExprStmt(expr));
			}
		}
		else if (isDtor)
		{
			// This will destruct all fields that need destructing.
			generateDtorStatements(fields, method.body, info.hasSuper);
		}
		
		info.tyd.declarations.add(method);		
		
		// Generates functions for default arguments.
		makeDefaultCalls(func.getDeclarator(), funcBinding);
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
				TypeHelpers.getTypeEnum(ifield.getType()) == TypeEnum.ARRAY)
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
	

	private int anonEnumCount = 0;
	
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
						TypeHelpers.getTypeEnum(params[0].getType()) == TypeEnum.REFERENCE &&
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
	private List<FieldInfo> collectFieldsForClass(IASTDeclSpecifier declSpec) throws DOMException
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
							ctx.bitfieldHelpers.addBitfield(declarator.getName());
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
		
		for (IASTDeclarator decl : simple.getDeclarators())
		{
			if (decl.getInitializer() == null)
				exprs.add(null);
			else if (decl.getInitializer() instanceof IASTEqualsInitializer)
				exprs.add(ctx.exprEvaluator.eval1Expr((IASTExpression) ((IASTEqualsInitializer) decl.getInitializer()).getInitializerClause()));
			else if (decl.getInitializer() instanceof ICPPASTConstructorInitializer)
				exprs.add(ctx.exprEvaluator.eval1Expr((IASTExpression) ((ICPPASTConstructorInitializer) decl.getInitializer()).getExpression()));
		}

		return exprs;
	}

	/**
	 * Attempts to evaluate the given declaration (function, class,
	 * namespace, template, etc).
	 */
	private void evalDeclaration(IASTDeclaration declaration) throws DOMException
	{
		if (declaration instanceof IASTFunctionDefinition &&
			((IASTFunctionDefinition)declaration).getDeclarator().getName().resolveBinding() instanceof IFunction)
		{
			print("function definition");
			evalFunction(declaration);
		}
		else if (declaration instanceof IASTFunctionDefinition)
		{
			IBinding bind = ((IASTFunctionDefinition) declaration).getDeclarator().getName().resolveBinding();
			
			if (bind instanceof IProblemBinding)
				printerr("Problem function: " + ((IProblemBinding) bind).getMessage() + ((IProblemBinding) bind).getLineNumber());
			else
				printerr("Function with unknown binding: " + bind.getClass().getCanonicalName());
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
					print("bit field");
					// We replace bit field fields with getter and setter methods...
					ctx.bitfieldHelpers.evalDeclBitfield((IField) binding, declarator);
				}
				else if (binding instanceof IField)
				{
					print("standard field");
					generateField(binding, declarator, exprs.get(i));
				}
				else if (binding instanceof IFunction &&
						declarator instanceof IASTFunctionDeclarator)
				{
					makeDefaultCalls((IASTFunctionDeclarator) declarator, binding);
				}
				else if (binding instanceof IVariable)
				{
					generateVariable(binding, declarator, exprs.get(i));
				}
				else
				{
					printerr("Unsupported declarator: " + declarator.getClass().getCanonicalName() + ":" + binding.getClass().getName());
				}
				i++;
			}
		}
		else if (declaration instanceof ICPPASTNamespaceDefinition)
		{
			ICPPASTNamespaceDefinition namespace = (ICPPASTNamespaceDefinition) declaration;
			print("namespace definition");

			// We don't care about namespaces but obviously must process the declarations
			// they contain...
			for (IASTDeclaration childDeclaration : namespace.getDeclarations())
				evalDeclaration(childDeclaration);
		}
		else if (declaration instanceof IASTASMDeclaration)
		{
			// Can't do anything with assembly...
			printerr("ASM : " + declaration.getRawSignature());
			exitOnError();
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			printerr("Problem declaration: " + p.getProblem().getMessageWithLocation() + ":" + p.getRawSignature());
			//exitOnError();
		}
		else if (declaration instanceof ICPPASTVisibilityLabel)
		{
			//ICPPASTVisibilityLabel vis = (ICPPASTVisibilityLabel) declaration;
			print("visibility");
			// We currently ignore visibility labels. If you wish to process
			// labels remember friend classes...
		}
		else if (declaration instanceof ICPPASTUsingDirective)
		{
			// ICPPASTUsingDirective usingDirective = (ICPPASTUsingDirective)declaration;
			print("using directive");
			// We ignore using directives, for now everything goes in the one package...
		}
		else if (declaration instanceof ICPPASTNamespaceAlias)
		{
			//ICPPASTNamespaceAlias namespaceAlias = (ICPPASTNamespaceAlias)declaration;
			print("Namespace alias");
			// We ignore namespace aliases...
		}
		else if (declaration instanceof ICPPASTUsingDeclaration)
		{
			// ICPPASTUsingDeclaration usingDeclaration = (ICPPASTUsingDeclaration)declaration;
			print("using declaration");
			// We ignore the using declaration...
		}
		else if (declaration instanceof ICPPASTLinkageSpecification)
		{
			ICPPASTLinkageSpecification linkageSpecification = (ICPPASTLinkageSpecification)declaration;
			print("linkage specification");

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
			print("explicit template instantiation");

			evalDeclaration(explicitTemplateInstantiation.getDeclaration());
		}
		else if (declaration instanceof ICPPASTTemplateSpecialization)
		{
			ICPPASTTemplateSpecialization templateSpecialization = (ICPPASTTemplateSpecialization)declaration;
			print("template specialization");

			evalDeclaration(templateSpecialization.getDeclaration());
		}
		else
		{
			printerr("Unknown declaration: " + declaration.getClass().getCanonicalName());
			exitOnError();
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
	 * Gets the argument names for a function.
	 */
	private List<String> getArgumentNames(IBinding funcBinding) throws DOMException
	{
		List<String> names = new ArrayList<String>();

		if (funcBinding instanceof IFunction)
		{
			IFunction func = (IFunction) funcBinding;
			IParameter[] params = func.getParameters();

			int missingCount = 0;
			for (IParameter param : params)
			{	
				if (param.getName() == null || param.getName().isEmpty())
					names.add("missing" + missingCount++);
				else
					names.add(param.getName());
			}
		}
		return names;
	}

	/**
	 * Gets the default expressions for function arguments (null where default is not provided).
	 */
	private List<MExpression> getDefaultValues(IASTFunctionDeclarator func) throws DOMException
	{
		IASTStandardFunctionDeclarator declarator = (IASTStandardFunctionDeclarator) func;
		IASTParameterDeclaration[] params = declarator.getParameters();

		List<MExpression> exprs = new ArrayList<MExpression>();

		for (IASTParameterDeclaration param : params)
		{
			IASTDeclarator paramDeclarator = param.getDeclarator(); 

			if (paramDeclarator.getInitializer() != null)
				exprs.add(eval1Init(paramDeclarator.getInitializer()));
			else
				exprs.add(null);
		}

		return exprs;
	}

	/**
	 * Evaluates the parameters for a function. A SingleVariableDeclaration
	 * contains a type and a name. 
	 */
	private List<MSimpleDecl> evalParameters(IBinding funcBinding) throws DOMException
	{
		List<MSimpleDecl> ret = new ArrayList<MSimpleDecl>();

		if (funcBinding instanceof IFunction)
		{
			IFunction func = (IFunction) funcBinding;
			IParameter[] params = func.getParameters();

			int missingCount = 0;
			for (IParameter param : params)
			{	
				MSimpleDecl var = new MSimpleDecl();
				var.type = TypeHelpers.cppToJavaType(param.getType());

				print("Found param: " + param.getName());

				// Remember C++ can have missing function argument names...
				if (param.getName() == null || param.getName().isEmpty())
					var.name = "missing" + missingCount++;
				else
					var.name = param.getName();

				ret.add(var);
			}
		}
		return ret;
	}

	/**
	 * Gets the type of the return value of a function.
	 * @return
	 */
	private String evalReturnType(IBinding funcBinding) throws DOMException
	{
		if (funcBinding instanceof IFunction)
		{
			IFunction func = (IFunction) funcBinding;
			IFunctionType funcType = func.getType();
			return TypeHelpers.cppToJavaType(funcType.getReturnType(), true, false);
		}

		printerr("Unexpected binding for return type: " + funcBinding.getClass().getCanonicalName());
		exitOnError();
		return null;
	}
	
	
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
			print("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
			{
				IBinding binding  = decl.getName().resolveBinding();

				if (binding instanceof IVariable)
				{
					ret.add(TypeHelpers.cppToJavaType(((IVariable) binding).getType()));
				}
			}
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			printerr("Problem declaration" + p.getProblem().getMessageWithLocation());
			exitOnError();
		}
		else
		{
			printerr("Unexpected declaration type here: " + declaration.getClass().getCanonicalName());
			exitOnError();
		}
		return ret;
	}

	private List<IType> evaluateDeclarationReturnCppTypes(IASTDeclaration declaration) throws DOMException
	{
		List<IType> ret = new ArrayList<IType>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			print("simple declaration");

			if (simpleDeclaration.getDeclarators().length > 0)
			{
				for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
				{
					IBinding binding  = decl.getName().resolveBinding();

					if (binding instanceof IVariable)
					{
						ret.add(((IVariable) binding).getType());
					}
				}
			}
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			printerr("Problem declaration" + p.getProblem().getMessageWithLocation());
			exitOnError();
		}
		else
		{
			printerr("Unexpected declaration type here: " + declaration.getClass().getCanonicalName());
			exitOnError();
		}
		return ret;
	}

	IType eval1DeclReturnCppType(IASTDeclaration decl) throws DOMException
	{
		return evaluateDeclarationReturnCppTypes(decl).get(0);
	}
	
	private HashMap<String, String> anonEnumMap = new HashMap<String, String>();
	
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
			print("named type");

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
			print("simple decl specifier");
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
			print("C declaration specifier (unimplemented)");
		}

		return null;
	}

	private void evalDeclEnum(IASTEnumerationSpecifier enumerationSpecifier) throws DOMException
	{
		IASTEnumerator[] enumerators = enumerationSpecifier.getEnumerators();
		
		if (enumerators == null || enumerators.length == 0)
			return;

		CppEnum enumModel = new CppEnum();
		enumModel.simpleName = TypeHelpers.getSimpleName(enumerationSpecifier.getName());
		enumModel.qualified = TypeHelpers.getQualifiedPart(enumerationSpecifier.getName()); 

		String first = enumerators[0].getName().toString();		
		anonEnumMap.put(first, enumModel.simpleName);
		
		int nextValue = 0;
		int sinceLastValue = 1;
		MExpression lastValue = null;

		
		for (IASTEnumerator e : enumerators)
		{
			CppEnumerator enumerator = new CppEnumerator();
			enumerator.name = TypeHelpers.getSimpleName(e.getName());

			if (e.getValue() != null)
			{
				enumerator.value = ctx.exprEvaluator.eval1Expr(e.getValue());
				lastValue = enumerator.value;
				sinceLastValue = 1;
			}
			else if (lastValue != null)
			{
				enumerator.value = ModelCreation.createInfixExpr(
						lastValue,
						ModelCreation.createLiteral(String.valueOf(sinceLastValue++)),
						"+");
			}
			else
			{
				enumerator.value = ModelCreation.createLiteral(String.valueOf(nextValue++));
			}
			
			enumModel.enumerators.add(enumerator);
		}
		
		addDeclaration(enumModel);
		popDeclaration();
	}


	/**
	 * Attempts to evaluate the given declaration specifier
	 */
	private CppDeclaration evalDeclSpecifier(IASTDeclSpecifier declSpecifier) throws DOMException
	{
		if (declSpecifier instanceof IASTCompositeTypeSpecifier)
		{
			IASTCompositeTypeSpecifier compositeTypeSpecifier = (IASTCompositeTypeSpecifier)declSpecifier;
			print("composite type specifier");

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
					print(fieldInfo.field.getName());

					if (fieldInfo.isStatic)
						/* Do nothing. */ ;
					else if (fieldInfo.init != null &&
							TypeHelpers.getTypeEnum(fieldInfo.field.getType()) != TypeEnum.ARRAY)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						ifBlock.statements.add(ModelCreation.createMethodCall("this", fieldInfo.field.getName(), "opAssign", right));
					}
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY &&
							TypeHelpers.getTypeEnum(TypeHelpers.getArrayBaseType(fieldInfo.field.getType())) == TypeEnum.OBJECT)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression left = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						ifBlock.statements.add(ModelCreation.createMethodCall("CPP", "assignArray", left, right));
					}
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY)
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
					print(fieldInfo.field.getName());

					if (fieldInfo.isStatic)
						/* Do nothing. */ ;
					else if (fieldInfo.init != null &&
							TypeHelpers.getTypeEnum(fieldInfo.field.getType()) != TypeEnum.ARRAY)
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
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY &&
							TypeHelpers.getTypeEnum(TypeHelpers.getArrayBaseType(fieldInfo.field.getType())) == TypeEnum.OBJECT)
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
					else if (TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY)
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
			print("elaborated type specifier" + elaboratedTypeSpecifier.getRawSignature());

			//evaluateElaborated(elaboratedTypeSpecifier.getKind());
			TypeHelpers.getSimpleName(elaboratedTypeSpecifier.getName());

			if (declSpecifier instanceof ICPPASTElaboratedTypeSpecifier)
			{
				//				ICPPASTElaboratedTypeSpecifier elaborated = (ICPPASTElaboratedTypeSpecifier) declSpecifier;
				print("cpp elaborated");
			}
		}
		else if (declSpecifier instanceof IASTEnumerationSpecifier)
		{
			IASTEnumerationSpecifier enumerationSpecifier = (IASTEnumerationSpecifier)declSpecifier;
			evalDeclEnum(enumerationSpecifier);
		}
		else if (declSpecifier instanceof IASTNamedTypeSpecifier)
		{
			IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier)declSpecifier;
			print("named type");
			TypeHelpers.getSimpleName(namedTypeSpecifier.getName());

			if (declSpecifier instanceof ICPPASTNamedTypeSpecifier)
			{
				//				ICPPASTNamedTypeSpecifier named = (ICPPASTNamedTypeSpecifier) declSpecifier;
				print("cpp named");
			}
		}
		else if (declSpecifier instanceof IASTSimpleDeclSpecifier)
		{
			IASTSimpleDeclSpecifier simple = (IASTSimpleDeclSpecifier) declSpecifier;
			print("simple decl specifier");

			if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier)
			{
				//				ICPPASTSimpleDeclSpecifier simple2 = (ICPPASTSimpleDeclSpecifier) declSpecifier;
				print("cpp simple");
			}

			TypeHelpers.evaluateSimpleType(simple.getType(), simple.isShort(), simple.isLong(), simple.isUnsigned());
		}
		else if (declSpecifier instanceof ICASTDeclSpecifier)
		{
			//			ICASTDeclSpecifier spec = (ICASTDeclSpecifier) declSpecifier;
			print("C declaration specifier (unimplemented)");
		}

		return null;
	}

	String getEnumerationName(IEnumerator enumerator) throws DOMException
	{
		String enumeration = TypeHelpers.getSimpleType(((IEnumeration) enumerator.getType()).getName());

		if (enumeration.equals("MISSING"))
		{
			String first = ((IEnumeration) enumerator.getOwner()).getEnumerators()[0].getName();
			String enumName = anonEnumMap.get(first);
			
			if (enumName == null)
				MyLogger.exitOnError();
			
			return enumName;
		}
		
		return enumeration;
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
			print("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
				ret.add(TypeHelpers.getSimpleName(decl.getName()));
		}
		else if (declaration instanceof ICPPASTExplicitTemplateInstantiation)
		{
			ICPPASTExplicitTemplateInstantiation explicitTemplateInstantiation = (ICPPASTExplicitTemplateInstantiation)declaration;
			printerr("explicit template instantiation");
			exitOnError();
			//evaluate(explicitTemplateInstantiation.getDeclaration());
		}
		else if (declaration instanceof IASTProblemDeclaration)
		{
			IASTProblemDeclaration p = (IASTProblemDeclaration) declaration;
			printerr("Problem declaration" + p.getProblem().getMessageWithLocation());
			exitOnError();
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



	private MExpression eval1Init(IASTInitializer initializer) throws DOMException 
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
		print("simple declaration");
			
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

	private void print(String arg)
	{
		// Comment out for big speed improvement...
		System.out.println(arg);
	}

	private void printerr(String arg)
	{
		System.err.println(arg);
	}
	
	private void exitOnError()
	{
		// Comment out if you wish to continue on error...
		try
		{
			throw new RuntimeException();
		} catch(Exception e)
		{
			e.printStackTrace();
		}

		System.exit(-1);
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

	

	


	List<MStmt> globalStmts = new ArrayList<MStmt>();


	
	


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

	private static class FieldInfo
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
	

	/**
	 * Traverses the AST of the given translation unit
	 * and tries to convert the C++ abstract syntax tree to
	 * a Java AST.
	 */
	String traverse(IASTTranslationUnit translationUnit)
	{
		GlobalContext con = new GlobalContext();

		con.converter = this;
		con.exprEvaluator = new ExpressionEvaluator(con);
		con.stmtEvaluator = new StmtEvaluator(con);
		con.bitfieldHelpers = new BitfieldHelpers(con);
		con.stackMngr = new StackManager(con);
		
		ctx = con;
		ctx.bitfieldHelpers.add("cls::_b");
		ctx.bitfieldHelpers.add("_b");

		//compositeMap.put("", new CompositeInfo(global));
		CppClass global = new CppClass();
		global.name = "Global";
		addDeclaration(global);
	
		for (IASTProblem prob : translationUnit.getPreprocessorProblems())
		{
			printerr(prob.getRawSignature());
		}

		try
		{
			for (IASTDeclaration declaration : translationUnit.getDeclarations())
			{
				printerr(declaration.getFileLocation().getEndingLineNumber() + ":" + declaration.getContainingFilename());
				evalDeclaration(declaration);
			}
		}
		catch (Exception e)
		{
			printerr(e.getMessage());
			e.printStackTrace();
		}
		popDeclaration();
		String output = "";
		STGroup group = new STGroupDir("/home/daniel/workspace/cpp-to-java-source-converter/templates");
		//System.err.println("!!!!" + decls2.get(0).declarations.get(0).getClass().toString());
//		for (MExpression expr : expressions)
//		{
//			ST test2 = group.getInstanceOf("expression_tp");
//			test2.add("expr_obj", expr);
//			//System.err.println("####" + test2.render());
//		}
		
		for (CppDeclaration decl : decls2)
		{
			ST test3 = group.getInstanceOf("declaration_tp");
			test3.add("decl", decl);
			output += test3.render();
		}
		
		for (MStmt stmt : globalStmts)
		{
			ST test4 = group.getInstanceOf("statement_tp");
			test4.add("stmt_obj", stmt);
			//System.err.println("$$$" + test4.render());
		}

		return output;
	}
}
