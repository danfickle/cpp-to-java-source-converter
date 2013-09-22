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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppAssign;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppBitfield;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppClass;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppCtor;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppDeclaration;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppDtor;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppEnum;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppEnumerator;
import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppFunction;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MAddItemCall;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MAddressOfExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MAddressOfExpressionArrayItem;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MArrayExpressionPtr;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MBracketExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MCastExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MClassInstanceCreation;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MCompoundWithBitfieldOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MCompoundWithDerefOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MCompoundWithNumberOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MDeleteObjectMultiple;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MDeleteObjectSingle;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MEmptyExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MFieldReferenceExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MFieldReferenceExpressionBitfield;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MFieldReferenceExpressionEnumerator;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MFieldReferenceExpressionPlain;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MFieldReferenceExpressionPtr;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MFunctionCallExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MFunctionCallExpressionParent;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MIdentityExpressionBitfield;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MIdentityExpressionEnumerator;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MIdentityExpressionNumber;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MIdentityExpressionPlain;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MIdentityExpressionPtr;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixAssignmentWithBitfieldOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixAssignmentWithDerefOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixAssignmentWithNumberOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixAssignmentWithPtrOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixExpressionPlain;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixExpressionWithBitfieldOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixExpressionWithDerefOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixExpressionWithPtrOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixExpressionWithPtrOnRight;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MInfixWithNumberOnLeft;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MLiteralExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MNewArrayExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MNewArrayExpressionObject;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MNewExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MNewExpressionObject;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPostfixExpressionBitfieldDec;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPostfixExpressionBitfieldInc;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPostfixExpressionNumberDec;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPostfixExpressionNumberInc;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPostfixExpressionPlain;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPostfixExpressionPointerDec;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPostfixExpressionPointerInc;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionBitfield;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionBitfieldDec;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionBitfieldInc;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionNumberDec;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionNumberInc;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionPlain;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionPointer;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionPointerDec;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionPointerInc;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MPrefixExpressionPointerStar;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MTernaryExpression;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.MValueOfExpressionNumber;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MBreakStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MCaseStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MCompoundStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MContinueStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MDeclarationStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MDefaultStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MDoStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MEmptyStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MExprStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MForStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MGotoStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MIfStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MLabelStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MProblemStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MReturnStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MSuperAssignStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MSuperDtorStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MSuperStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MSwitchStmt;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.MWhileStmt;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.MSimpleDecl;




/**
 * Second stage of the C++ to Java source converter.
 * @author DanFickle
 * http://github.com/danfickle/cpp-to-java-source-converter/
 */
public class SourceConverterStage2
{
	private List<CppDeclaration> decls2 = new ArrayList<CppDeclaration>();
	private CompositeInfo currentInfo = null;
	private Deque<CompositeInfo> currentInfoStack = new ArrayDeque<CompositeInfo>();
	
	private boolean addDeclaration(CppDeclaration decl)
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
	
	private CompositeInfo getCurrentCompositeInfo()
	{
		return currentInfo;
	}
	
	private void popDeclaration()
	{
		currentInfoStack.pop();
		currentInfo = currentInfoStack.peekFirst();
	}
	
	private void evalDeclBitfield(IField field, IASTDeclarator declarator) throws DOMException
	{
		CppBitfield bitfield = new CppBitfield();
		bitfield.name = field.getName();
		bitfield.bits = eval1Expr(((IASTFieldDeclarator) declarator).getBitFieldSize());
		bitfield.type = cppToJavaType(field.getType());
		addDeclaration(bitfield);
		popDeclaration();
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
			methodDef.name = getSimpleName(func.getName());
			methodDef.retType = evalReturnType(funcBinding);
			
			// This gets a parameter variable declaration for each param.
			List<MSimpleDecl> list = evalParameters(funcBinding);

			// Add a param declaration for each param up until we want to use
			// default values (which won't have a param).
			for (int k2 = 0; k2 < k; k2++)
				methodDef.args.add(list.get(k2));

			// This code block simple calls the original method with
			// passed in arguments plus default arguments.
			MFunctionCallExpression method = ModelCreation.createFuncCall(getSimpleName(func.getName()));

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
			else if (getTypeEnum(fields.get(i).field.getType()) == TypeEnum.OBJECT)
			{
				// Call this.field.destruct()
				MStmt stmt = ModelCreation.createMethodCall("this", fields.get(i).field.getName(), "destruct");
				method.statements.add(stmt);
			}
			else if (getTypeEnum(fields.get(i).field.getType()) == TypeEnum.ARRAY &&
					getTypeEnum(getArrayBaseType(fields.get(i).field.getType())) == TypeEnum.OBJECT)
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
		method.name = getSimpleName(func.getDeclarator().getName());
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
		
		// These class vars track the number of local variables in this function
		// during the recursive evaluation.
		m_localVariableMaxId = null;
		m_nextVariableId = 0;

		// We need this so we know if the return expression needs to be made
		// java boolean.
		currentReturnType = method.retType;
		
		// eval1Stmt work recursively to generate the function body.
		method.body = (MCompoundStmt) eval1Stmt(func.getBody());

		// For debugging purposes, we put return type back to null.
		currentReturnType = null;
		
		// If we have any local variables that are objects, or arrays of objects,
		// we must create an explicit stack so they can be added to it and explicity
		// cleaned up at termination points (return, break, continue, end block).
		if (m_localVariableMaxId != null)
		{
			MLiteralExpression expr = new MLiteralExpression();
			expr.literal = String.valueOf(m_localVariableMaxId);

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
								ModelCreation.createLiteral(cppToJavaType(((IVariable) chain.getMemberInitializerId().resolveBinding()).getType()));
						
						MClassInstanceCreation create = new MClassInstanceCreation();
						create.name = lit;
						create.args.addAll(evalExpr(chain.getInitializerValue()));
						
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
							superInit = evalExpr(chain.getInitializerValue());
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
			
			if (getTypeEnum(ifield.getType()) == TypeEnum.OBJECT ||
				getTypeEnum(ifield.getType()) == TypeEnum.ARRAY)
			{
				frag.initExpr = init;
			}
		}

		if (ifield.getType().toString().isEmpty())
			frag.type = "AnonClass" + (m_anonClassCount - 1);
		else
			frag.type = cppToJavaType(ifield.getType());

		frag.isPublic = true;
		
		addDeclaration(frag);
		popDeclaration();
	}
	
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
			frag.type = "AnonClass" + (m_anonClassCount - 1);
		else
			frag.type = cppToJavaType(ifield.getType());

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
						getTypeEnum(params[0].getType()) == TypeEnum.REFERENCE &&
						cppToJavaType(params[0].getType()).toString().equals(ctor.getName()))
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
							addBitfield(declarator.getName());
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
	
	
	private List<MExpression> evaluateDeclarationReturnInitializers(IASTSimpleDeclaration simple) throws DOMException 
	{
		List<MExpression> exprs = new ArrayList<MExpression>();
		
		for (IASTDeclarator decl : simple.getDeclarators())
		{
			if (decl.getInitializer() == null)
				exprs.add(null);
			else if (decl.getInitializer() instanceof IASTEqualsInitializer)
				exprs.add(eval1Expr((IASTExpression) ((IASTEqualsInitializer) decl.getInitializer()).getInitializerClause()));
			else if (decl.getInitializer() instanceof ICPPASTConstructorInitializer)
				exprs.add(eval1Expr((IASTExpression) ((ICPPASTConstructorInitializer) decl.getInitializer()).getExpression()));
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
					evalDeclBitfield((IField) binding, declarator);
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
				var.type = cppToJavaType(param.getType());

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
	 * @return A JDT Type.
	 */
	private String evalReturnType(IBinding funcBinding) throws DOMException
	{
		if (funcBinding instanceof IFunction)
		{
			IFunction func = (IFunction) funcBinding;
			IFunctionType funcType = func.getType();
			return cppToJavaType(funcType.getReturnType(), true, false);
		}

		printerr("Unexpected binding for return type: " + funcBinding.getClass().getCanonicalName());
		exitOnError();
		return null;
	}
	
	
	/**
	 * Evaluates a variable declaration and returns types for each variable.
	 * For example int a, b, * c; would return two ints and an int *. 
	 */
	private List<String> evaluateDeclarationReturnTypes(IASTDeclaration declaration) throws DOMException
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
					ret.add(cppToJavaType(((IVariable) binding).getType()));
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

	private IType eval1DeclReturnCppType(IASTDeclaration decl) throws DOMException
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
			print("named type");

			return getSimpleName(namedTypeSpecifier.getName());

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

			return evaluateSimpleType(simple.getType(), simple.isShort(), simple.isLong(), simple.isUnsigned());
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
		enumModel.simpleName = getSimpleName(enumerationSpecifier.getName());
		enumModel.qualified = getQualifiedPart(enumerationSpecifier.getName()); 

		String first = enumerators[0].getName().toString();		
		m_anonEnumMap.put(first, enumModel.simpleName);
		
		int nextValue = 0;
		int sinceLastValue = 1;
		MExpression lastValue = null;

		
		for (IASTEnumerator e : enumerators)
		{
			CppEnumerator enumerator = new CppEnumerator();
			enumerator.name = getSimpleName(e.getName());

			if (e.getValue() != null)
			{
				enumerator.value = eval1Expr(e.getValue());
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
			
			if (getSimpleName(compositeTypeSpecifier.getName()).equals("MISSING"))
				tyd.name = "AnonClass" + m_anonClassCount++;
			else
				tyd.name = getSimpleName(compositeTypeSpecifier.getName());

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
					info.superClass = tyd.superclass = getSimpleName(cppCompositeTypeSpecifier.getBaseSpecifiers()[0].getName());
				}
				

				for (int i = 0; i < cppCompositeTypeSpecifier.getBaseSpecifiers().length; i++)
					tyd.additionalSupers.add(getSimpleName(cppCompositeTypeSpecifier.getBaseSpecifiers()[i].getName()));
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
							getTypeEnum(fieldInfo.field.getType()) != TypeEnum.ARRAY)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						ifBlock.statements.add(ModelCreation.createMethodCall("this", fieldInfo.field.getName(), "opAssign", right));
					}
					else if (getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY &&
							getTypeEnum(getArrayBaseType(fieldInfo.field.getType())) == TypeEnum.OBJECT)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression left = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						ifBlock.statements.add(ModelCreation.createMethodCall("CPP", "assignArray", left, right));
					}
					else if (getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY)
					{
						MFieldReferenceExpression right = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression left = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						String methodName = "assignBasicArray";

						if (getArraySizeExpressions(fieldInfo.field.getType()).size() > 1)
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
						getTypeEnum(fieldInfo.field.getType()) != TypeEnum.ARRAY)
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
					else if (getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY &&
							getTypeEnum(getArrayBaseType(fieldInfo.field.getType())) == TypeEnum.OBJECT)
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
					else if (getTypeEnum(fieldInfo.field.getType()) == TypeEnum.ARRAY)
					{
						// this.field = CPP.copy*Array(right.field);
						MFieldReferenceExpression fr1 = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
						MFieldReferenceExpression fr2 = ModelCreation.createFieldReference("right", fieldInfo.field.getName());
						MFieldReferenceExpression fr3 = ModelCreation.createFieldReference("CPP", getArraySizeExpressions(fieldInfo.field.getType()).size() > 1 ? "copyMultiArray" : "copyBasicArray");

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
			getSimpleName(elaboratedTypeSpecifier.getName());

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
			getSimpleName(namedTypeSpecifier.getName());

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

			evaluateSimpleType(simple.getType(), simple.isShort(), simple.isLong(), simple.isUnsigned());
		}
		else if (declSpecifier instanceof ICASTDeclSpecifier)
		{
			//			ICASTDeclSpecifier spec = (ICASTDeclSpecifier) declSpecifier;
			print("C declaration specifier (unimplemented)");
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
	
	private MExpression createAddItemCall(MExpression item)
	{
		MAddItemCall add = new MAddItemCall();
		add.operand = item;
		add.nextFreeStackId = m_nextVariableId;
		incrementLocalVariableId();
		return add;
	}
	
	/**
	 * Returns the names contained in a declaration.
	 * Eg. int a, b, * c; will return [a, b, c].
	 */
	private List<String> evaluateDeclarationReturnNames(IASTDeclaration declaration) throws DOMException
	{
		List<String> ret = new ArrayList<String>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			print("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
				ret.add(getSimpleName(decl.getName()));
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

	/**
	 * Attempts to get the boxed type for a primitive type.
	 * We need the boxed type to use for Ptr and Ref parametized
	 * types.
	 */
	private String evaluateSimpleTypeBoxed(int type, boolean isShort, boolean isLongLong, boolean isUnsigned)
	{
		switch (type)
		{
		case IASTSimpleDeclSpecifier.t_char:
			print("char");
			return "Byte";
		case IASTSimpleDeclSpecifier.t_int:
			print("int");
			if (isShort)
				return "Short";
			else if (isLongLong)
				return "Long";
			else
				return "MInteger";
		case IASTSimpleDeclSpecifier.t_float:
			print("float");
			return "Float";
		case IASTSimpleDeclSpecifier.t_double:
			print("double");
			return "Double";
		case IASTSimpleDeclSpecifier.t_unspecified:
			print("unspecified");
			if (isUnsigned)
				return "Integer";
			else
				return null;
		case IASTSimpleDeclSpecifier.t_void:
			print("void");
			return "Void";
		case ICPPASTSimpleDeclSpecifier.t_bool:
			print("bool");
			return "Boolean";
		case ICPPASTSimpleDeclSpecifier.t_wchar_t:
			print("wchar_t");
			return "Character";
		default:
			return null;
		}
	}
	
	/**
	 * Returns the Java simple type for the corresponding C++ type. 
	 */
	private String evaluateSimpleType(int type, boolean isShort, boolean isLongLong, boolean isUnsigned)
	{
		switch (type)
		{
		case IASTSimpleDeclSpecifier.t_char:
			print("char");
			return "byte";
		case IASTSimpleDeclSpecifier.t_int:
			print("int");
			if (isShort)
				return "short";
			else if (isLongLong)
				return "long";
			else
				return "int";
		case IASTSimpleDeclSpecifier.t_float:
			print("float");
			return "float";
		case IASTSimpleDeclSpecifier.t_double:
			print("double");
			return "double";
		case IASTSimpleDeclSpecifier.t_unspecified:
			print("unspecified");
			if (isUnsigned)
				return "int";
			else
				return "int";
		case IASTSimpleDeclSpecifier.t_void:
			print("void");
			return "void";
		case ICPPASTSimpleDeclSpecifier.t_bool:
			print("bool");
			return "boolean";
		case ICPPASTSimpleDeclSpecifier.t_wchar_t:
			print("wchar_t");
			return "char";
		default:
			return null;
		}
	}

	private MExpression eval1Expr(IASTExpression expr) throws DOMException
	{
		List<MExpression> exprs = evalExpr(expr);
		assert(exprs.size() == 1);
		return exprs.get(0);
	}
	
	private MExpression callCopyIfNeeded(MExpression expr, IASTExpression cppExpr) throws DOMException
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

	/**
	 * Given a C++ expression, attempts to convert it into one or more Java expressions.
	 */
	private List<MExpression> evalExpr(IASTExpression expression) throws DOMException
	{
		List<MExpression> ret = new ArrayList<MExpression>();
		
		if (expression instanceof IASTLiteralExpression)
		{
			evalExprLiteral((IASTLiteralExpression) expression, ret);
		}
		else if (expression instanceof IASTIdExpression)
		{
			evalExprId((IASTIdExpression) expression, ret);
		}
		else if (expression instanceof IASTFieldReference)
		{
			evalExprFieldReference((IASTFieldReference) expression, ret);
		}
		else if (expression instanceof IASTUnaryExpression)
		{
			evalExprUnary((IASTUnaryExpression) expression, ret);
		}
		else if (expression instanceof IASTConditionalExpression)
		{
			evalExprConditional((IASTConditionalExpression) expression, ret);
		}
		else if (expression instanceof IASTArraySubscriptExpression)
		{
			evalExprArraySubscript((IASTArraySubscriptExpression) expression, ret);
		}
		else if (expression instanceof IASTBinaryExpression)
		{
			evalExprBinary((IASTBinaryExpression) expression, ret);
		}
		else if (expression instanceof ICPPASTDeleteExpression)
		{
			evalExprDelete((ICPPASTDeleteExpression) expression, ret);
		}
		else if (expression instanceof ICPPASTNewExpression)
		{
			evalExprNew((ICPPASTNewExpression) expression, ret);
		}
		else if (expression instanceof IASTFunctionCallExpression)
		{
			evalExprFuncCall((IASTFunctionCallExpression) expression, ret);
		}
		else if (expression instanceof IASTCastExpression)
		{
			evalCastExpression((IASTCastExpression) expression, ret);
		}
		else if (expression instanceof IASTTypeIdExpression)
		{
			//evalTypeIdExpression((IASTTypeIdExpression) expression, ret, flags);
		}
		else if (expression instanceof ICASTTypeIdInitializerExpression)
		{
			ICASTTypeIdInitializerExpression typeIdInitializerExpression = (ICASTTypeIdInitializerExpression)expression;

			evalTypeId(typeIdInitializerExpression.getTypeId());
			//evaluate(typeIdInitializerExpression.getInitializer());
		}
		else if (expression instanceof ICPPASTSimpleTypeConstructorExpression)
		{
			ICPPASTSimpleTypeConstructorExpression simpleTypeConstructorExpression = (ICPPASTSimpleTypeConstructorExpression)expression;
			evalExpr(simpleTypeConstructorExpression.getInitialValue());
		}
		else if (expression instanceof IGNUASTCompoundStatementExpression)
		{
			IGNUASTCompoundStatementExpression compoundStatementExpression = (IGNUASTCompoundStatementExpression)expression;
			evalStmt(compoundStatementExpression.getCompoundStatement());
		}
		else if (expression instanceof IASTExpressionList)
		{
			IASTExpressionList list = (IASTExpressionList) expression;

			for (IASTExpression childExpression : list.getExpressions())
				ret.addAll(evalExpr(childExpression));
		}
		else if (expression == null)
		{
			ret.add(new MEmptyExpression());
		}

		if (ret.isEmpty())
			printerr(expression.getClass().getCanonicalName());

		if (expression != null)
			print(expression.getClass().getCanonicalName());

		if (!ret.isEmpty())
			expressions.add(ret.get(0));
		return ret;
	}

	private MExpression eval1Init(IASTInitializer initializer) throws DOMException 
	{
		IASTEqualsInitializer eq = (IASTEqualsInitializer) initializer;
		IASTInitializerClause clause = eq.getInitializerClause();
		IASTExpression expr = (IASTExpression) clause;
		
		return eval1Expr(expr);
	}

	private void evalExprNew(ICPPASTNewExpression expr, List<MExpression> ret) throws DOMException
	{
		if (expr.isArrayAllocation() && !isObjectPtr(expr.getExpressionType()))
		{
			MNewArrayExpression ptr = new MNewArrayExpression();
			
			for (IASTExpression arraySize : expr.getNewTypeIdArrayExpressions())
				ptr.sizes.add(eval1Expr(arraySize));
			
			ptr.type = cppToJavaType(expr.getExpressionType());
			ret.add(ptr);
		}
		else if (expr.isArrayAllocation() && isObjectPtr(expr.getExpressionType()))
		{
			MNewArrayExpressionObject ptr = new MNewArrayExpressionObject();
			
			for (IASTExpression arraySize : expr.getNewTypeIdArrayExpressions())
				ptr.sizes.add(eval1Expr(arraySize));
			
			ptr.type = cppToJavaType(expr.getExpressionType());
			ret.add(ptr);
		}
		else if (!isObjectPtr(expr.getExpressionType()))
		{
			MNewExpression ptr = new MNewExpression();
			ptr.type = cppToJavaType(expr.getExpressionType());
			
			if (expr.getNewInitializer() != null)
				ptr.argument = eval1Expr(expr.getNewInitializer());
			else
			{
				MLiteralExpression lit = new MLiteralExpression();
				lit.literal = "0";
				ptr.argument = lit;
			}
			ret.add(ptr);
		}
		else
		{
			MNewExpressionObject ptr = new MNewExpressionObject();
			ptr.type = cppToJavaType(expr.getExpressionType());

			if (expr.getNewInitializer() instanceof IASTExpressionList)
			{
				for (IASTExpression arg : ((IASTExpressionList) expr.getNewInitializer()).getExpressions())
					ptr.arguments.addAll(evalExpr(arg));
			}
			else if (expr.getNewInitializer() instanceof IASTExpression)
			{
				ptr.arguments.addAll(evalExpr((IASTExpression) expr.getNewInitializer()));
			}
			
			ret.add(ptr);
		}
	}

	private void evalExprDelete(ICPPASTDeleteExpression expr, List<MExpression> ret) throws DOMException
	{
		if (isObjectPtr(expr.getOperand().getExpressionType()))
		{
			if (expr.isVectored())
			{
				MDeleteObjectMultiple del = new MDeleteObjectMultiple();
				del.operand = eval1Expr(expr.getOperand());
				ret.add(del);
			}
			else
			{
				MDeleteObjectSingle del = new MDeleteObjectSingle();
				del.operand = eval1Expr(expr.getOperand());
				ret.add(del);
			}
		}
		else
		{
			MEmptyExpression emp = new MEmptyExpression();
			ret.add(emp);
		}
	}
	
	
	private String getEnumerationName(IEnumerator enumerator) throws DOMException
	{
		String enumeration = getSimpleType(((IEnumeration) enumerator.getType()).getName());

		if (enumeration.equals("MISSING"))
		{
			String first = ((IEnumeration) enumerator.getOwner()).getEnumerators()[0].getName();
			String enumName = m_anonEnumMap.get(first);
			
			if (enumName == null)
				exitOnError();
			
			return enumName;
		}
		
		return enumeration;
	}
	
	

	private void evalExprId(IASTIdExpression expr, List<MExpression> ret) throws DOMException
	{
		if (isBitfield(expr.getName()))
		{
			MIdentityExpressionBitfield ident = new MIdentityExpressionBitfield();
			ident.ident = getSimpleName(expr.getName());
			ret.add(ident);
		}
		else if (expr.getName().resolveBinding() instanceof IEnumerator)
		{
			MIdentityExpressionEnumerator ident = new MIdentityExpressionEnumerator();
			ident.enumName = getEnumerationName((IEnumerator) expr.getName().resolveBinding());
			ident.ident = getSimpleName(expr.getName());
			ret.add(ident);
		}
		else if (isEventualPtr(expr.getExpressionType()))
		{
			MIdentityExpressionPtr ident = new MIdentityExpressionPtr();
			ident.ident = getSimpleName(expr.getName());
			ret.add(ident);
		}
		else if (isNumberExpression(expr))
		{
			MIdentityExpressionNumber ident = new MIdentityExpressionNumber();
			ident.ident = getSimpleName(expr.getName());
			ret.add(ident);
		}
		else
		{
			MIdentityExpressionPlain ident = new MIdentityExpressionPlain();
			ident.ident = getSimpleName(expr.getName());
			ret.add(ident);
		}
	}

	private void evalExprFieldReference(IASTFieldReference expr, List<MExpression> ret) throws DOMException
	{
		if (isBitfield(expr.getFieldName()))
		{
			MFieldReferenceExpressionBitfield field = new MFieldReferenceExpressionBitfield();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = getSimpleName(expr.getFieldName());
			ret.add(field);
		}
		else if (expr.getFieldName().resolveBinding() instanceof IEnumerator)
		{
			MFieldReferenceExpressionEnumerator field = new MFieldReferenceExpressionEnumerator();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = getSimpleName(expr.getFieldName());
			ret.add(field);
		}
		else if (isEventualPtr(expr.getExpressionType()) && expr.isPointerDereference())
		{
			MFieldReferenceExpressionPtr field = new MFieldReferenceExpressionPtr();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = getSimpleName(expr.getFieldName());
			ret.add(field);
		}
		else
		{
			MFieldReferenceExpressionPlain field = new MFieldReferenceExpressionPlain();
			field.object = eval1Expr(expr.getFieldOwner());
			field.field = getSimpleName(expr.getFieldName());
			ret.add(field);
		}
	}


	private void evalExprConditional(IASTConditionalExpression expr, List<MExpression> ret) throws DOMException 
	{
		MTernaryExpression ternary = new MTernaryExpression();
		
		ternary.condition = eval1Expr(expr.getLogicalConditionExpression());
		ternary.condition = makeExpressionBoolean(ternary.condition, expr.getLogicalConditionExpression());
		ternary.positive = eval1Expr(expr.getPositiveResultExpression());
		ternary.negative = eval1Expr(expr.getNegativeResultExpression());
		
		ret.add(ternary);
	}

	private void evalCastExpression(IASTCastExpression expr, List<MExpression> ret) throws DOMException
	{
		MCastExpression cast = new MCastExpression();
		cast.operand = eval1Expr(expr.getOperand());
		// TODO cast.setType(evalTypeId(castExpression.getTypeId()));
	}

	private void evalExprArraySubscript(IASTArraySubscriptExpression expr, List<MExpression> ret) throws DOMException
	{
		//if (isEventualPtr(expr.getArrayExpression().getExpressionType()))
		{
			MArrayExpressionPtr ptr = new MArrayExpressionPtr();
			ptr.operand = eval1Expr(expr.getArrayExpression());
			ptr.subscript.addAll(evalExpr(expr.getSubscriptExpression()));
			ret.add(ptr);
		}
	}

	private void evalExprLiteral(IASTLiteralExpression lit, List<MExpression> ret) throws DOMException 
	{
		MLiteralExpression out = new MLiteralExpression();
		
		switch (lit.getKind())
		{
		case IASTLiteralExpression.lk_false:
			out.literal = "false";
			break;
		case IASTLiteralExpression.lk_true:
			out.literal = "true";
			break;
		case IASTLiteralExpression.lk_char_constant:
		case IASTLiteralExpression.lk_float_constant:
		case IASTLiteralExpression.lk_string_literal:
		case IASTLiteralExpression.lk_integer_constant:
			out.literal = String.valueOf(lit.getValue());
			break;
		case IASTLiteralExpression.lk_this:
			out.literal = "this";
			break;
		}
		
		ret.add(out);
	}

	List<MExpression> expressions = new ArrayList<MExpression>();

	private MExpression bracket(MExpression expr)
	{
		MBracketExpression bra = new MBracketExpression();
		bra.operand = expr;
		return bra;
	}
	
	private void evalExprUnary(IASTUnaryExpression expr, List<MExpression> ret) throws DOMException
	{
		if (expr.getOperator() == IASTUnaryExpression.op_bracketedPrimary)
		{
			MBracketExpression bra = new MBracketExpression();
			bra.operand = eval1Expr(expr.getOperand());
			ret.add(bra);
		}
		else if (expr.getOperator() == IASTUnaryExpression.op_amper)
		{
			if (isEventualPtrDeref(expr.getOperand()))
			{
				MAddressOfExpressionArrayItem add = new MAddressOfExpressionArrayItem();
				add.operand = eval1Expr(expr.getOperand());
				ret.add(add);
			}
			else
			{
				MAddressOfExpression add = new MAddressOfExpression();
				add.operand = eval1Expr(expr.getOperand());
				ret.add(add);
			}
		}
		else if (expr.getOperator() == IASTUnaryExpression.op_star)
		{
			MPrefixExpressionPointerStar pre = new MPrefixExpressionPointerStar();
			pre.operand = eval1Expr(expr.getOperand());
			ret.add(pre);
		}
		else if (isEventualPtr(expr.getExpressionType()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionPointerInc post = new MPostfixExpressionPointerInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionPointerDec post = new MPostfixExpressionPointerDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionPointerDec pre = new MPrefixExpressionPointerDec();
				pre.operand = eval1Expr(expr.getOperand());
				ret.add(pre);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionPointerInc pre = new MPrefixExpressionPointerInc();
				pre.operand = eval1Expr(expr.getOperand());
				ret.add(pre);
			}
			else if (isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionPointer pre = new MPrefixExpressionPointer();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = evalUnaryPrefixOperator(expr.getOperator());
				ret.add(pre);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_amper)
			{
				MAddressOfExpression add = new MAddressOfExpression();
				add.operand = eval1Expr(expr.getOperand());
				ret.add(add);
			}
		}
		else if (isBitfield(expr.getOperand()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionBitfieldInc post = new MPostfixExpressionBitfieldInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionBitfieldDec post = new MPostfixExpressionBitfieldDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionBitfieldInc post = new MPrefixExpressionBitfieldInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionBitfieldDec post = new MPrefixExpressionBitfieldDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionBitfield pre = new MPrefixExpressionBitfield();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = evalUnaryPrefixOperator(expr.getOperator());
				ret.add(pre);
			}
		}
		else if (isNumberExpression(expr.getOperand()))
		{
			if (expr.getOperator() == IASTUnaryExpression.op_postFixIncr)
			{
				MPostfixExpressionNumberInc post = new MPostfixExpressionNumberInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_postFixDecr)
			{
				MPostfixExpressionNumberDec post = new MPostfixExpressionNumberDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			if (expr.getOperator() == IASTUnaryExpression.op_prefixIncr)
			{
				MPrefixExpressionNumberInc post = new MPrefixExpressionNumberInc();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (expr.getOperator() == IASTUnaryExpression.op_prefixDecr)
			{
				MPrefixExpressionNumberDec post = new MPrefixExpressionNumberDec();
				post.operand = eval1Expr(expr.getOperand());
				ret.add(post);
			}
			else if (isPrefixExpression(expr.getOperator()))
			{
				MPrefixExpressionPlain pre = new MPrefixExpressionPlain();
				pre.operand = eval1Expr(expr.getOperand());
				pre.operator = evalUnaryPrefixOperator(expr.getOperator());
				ret.add(pre);
			}
		}
		// TODO else if (isEnumerator())
		else if (isPostfixExpression(expr.getOperator()))
		{
			MPostfixExpressionPlain postfix = new MPostfixExpressionPlain();
			postfix.operand = eval1Expr(expr.getOperand());
			postfix.operator = evalUnaryPostfixOperator(expr.getOperator());
			ret.add(postfix);
		}
		else if (isPrefixExpression(expr.getOperator()))
		{
			MPrefixExpressionPlain pre = new MPrefixExpressionPlain();
			pre.operand = eval1Expr(expr.getOperand());
			pre.operator = evalUnaryPrefixOperator(expr.getOperator());
			ret.add(pre);
		}
	}


	private void evalExprFuncCall(IASTFunctionCallExpression expr, List<MExpression> ret) throws DOMException
	{
		MFunctionCallExpressionParent func;
		
		if (expr.getFunctionNameExpression() instanceof IASTIdExpression &&
			((IASTIdExpression) expr.getFunctionNameExpression()).getName().resolveBinding() instanceof ICPPClassType)
		{
			func = new MClassInstanceCreation();
		}
		else
		{
			func = new MFunctionCallExpression();
		}

		func.name = eval1Expr(expr.getFunctionNameExpression());

		if (expr.getParameterExpression() instanceof IASTExpressionList)
		{
			IASTExpressionList list = (IASTExpressionList) expr.getParameterExpression();
			for (IASTExpression arg : list.getExpressions())
			{
				if (isNumberExpression(arg))
				{
					MExpression exarg = eval1Expr(arg);
					MValueOfExpressionNumber valOfExpr = new MValueOfExpressionNumber();
					valOfExpr.operand = exarg;
					valOfExpr.type = "MInteger"; // TODO
					func.args.add(valOfExpr);
				}
				else
				{
					MExpression exarg = eval1Expr(arg);
					exarg = callCopyIfNeeded(exarg, arg);
					func.args.add(exarg);
				}
			}
		}
		else if (expr.getParameterExpression() instanceof IASTExpression)
		{
			IASTExpression arg = expr.getParameterExpression();
			
			if (isNumberExpression(arg))
			{
				MExpression exarg = eval1Expr(arg);
				MValueOfExpressionNumber valOfExpr = new MValueOfExpressionNumber();
				valOfExpr.operand = exarg;
				valOfExpr.type = "MInteger"; // TODO
				func.args.add(valOfExpr);
			}
			else
			{
				MExpression exarg = eval1Expr(expr.getParameterExpression());
				exarg = callCopyIfNeeded(exarg, expr.getParameterExpression());
				func.args.add(exarg);
			}
		}

		ret.add(func);
	}


	private void evalExprBinary(IASTBinaryExpression expr, List<MExpression> ret) throws DOMException 
	{
		if (isBitfield(expr.getOperand1()))
		{
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				MInfixAssignmentWithBitfieldOnLeft infix = new MInfixAssignmentWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				ret.add(infix);
			}
			else if (isAssignmentExpression(expr.getOperator()))
			{
				MCompoundWithBitfieldOnLeft infix = new MCompoundWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = compoundAssignmentToInfixOperator(expr.getOperator());
				ret.add(infix);
			}
			else
			{
				MInfixExpressionWithBitfieldOnLeft infix = new MInfixExpressionWithBitfieldOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = evaluateBinaryOperator(expr.getOperator());
				ret.add(infix);
			}
		}
		else if(isEventualPtrDeref(expr.getOperand1()))
		{
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				MInfixAssignmentWithDerefOnLeft infix = new MInfixAssignmentWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				ret.add(infix);
			}
			else if (isAssignmentExpression(expr.getOperator()))
			{
				MCompoundWithDerefOnLeft infix = new MCompoundWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = compoundAssignmentToInfixOperator(expr.getOperator());
				ret.add(infix);
			}
			else
			{
				MInfixExpressionWithDerefOnLeft infix = new MInfixExpressionWithDerefOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = evaluateBinaryOperator(expr.getOperator());
				ret.add(infix);
			}
		}
		else if (expr.getOperator() == IASTBinaryExpression.op_assign &&
				isEventualPtr(expr.getOperand1().getExpressionType()))
		{
			MInfixAssignmentWithPtrOnLeft infix = new MInfixAssignmentWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			ret.add(infix);
		}
		else if (isEventualPtr(expr.getOperand1().getExpressionType()))
		{
			MInfixExpressionWithPtrOnLeft infix = new MInfixExpressionWithPtrOnLeft();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = evaluateBinaryOperator(expr.getOperator());
			ret.add(infix);
		}
		else if (isEventualPtr(expr.getOperand2().getExpressionType()))
		{
			MInfixExpressionWithPtrOnRight infix = new MInfixExpressionWithPtrOnRight();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = evaluateBinaryOperator(expr.getOperator());
			ret.add(infix);
		}
		else if (isNumberExpression(expr.getOperand1()))
		{
			MInfixExpression infix = null;
			
			if (expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				infix = new MInfixAssignmentWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				ret.add(infix);
			}
			else if (isAssignmentExpression(expr.getOperator()))
			{
				infix = new MCompoundWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = compoundAssignmentToInfixOperator(expr.getOperator());
				ret.add(infix);
			}
			else
			{
				infix = new MInfixWithNumberOnLeft();
				infix.left = eval1Expr(expr.getOperand1());
				infix.right = eval1Expr(expr.getOperand2());
				infix.operator = evaluateBinaryOperator(expr.getOperator());
				ret.add(infix);
			}
			
			if (needBooleanExpressions(expr.getOperator()))
			{
				infix.left = makeExpressionBoolean(infix.left, expr.getOperand1());
				infix.right = makeExpressionBoolean(infix.right, expr.getOperand2());
			}
			else if (isBooleanExpression(expr.getOperand1()) && expr.getOperator() == IASTBinaryExpression.op_assign)
			{
				infix.right = makeExpressionBoolean(infix.right, expr.getOperand2());
			}
		}
		else
		{
			MInfixExpressionPlain infix = new MInfixExpressionPlain();
			infix.left = eval1Expr(expr.getOperand1());
			infix.right = eval1Expr(expr.getOperand2());
			infix.operator = evaluateBinaryOperator(expr.getOperator());


			
			ret.add(infix);
		}
		
//		
//		
//		
//		
//		
//		
//		
//		
//		//		if (expr instanceof IASTImplicitNameOwner &&
////				((IASTImplicitNameOwner) expr).getImplicitNames().length != 0 &&
////				((IASTImplicitNameOwner) expr).getImplicitNames()[0].isOperator())
////		{
////			String name = ((IASTImplicitNameOwner) expr).getImplicitNames()[0].resolveBinding().getName();
////			//TODO Unknown functions.
////			String replace = normalizeName(name);
////
////			if (((IASTImplicitNameOwner) expr).getImplicitNames()[0].resolveBinding() instanceof ICPPMethod)
////			{
////				MethodInvocation method = jast.newMethod()
////						.on(eval1Expr(expr.getOperand1()))
////						.call(replace)
////						.with(eval1Expr(expr.getOperand2())).toAST();
////				ret.add(method);
////			}
////			else if (((IASTImplicitNameOwner) expr).getImplicitNames()[0].resolveBinding() instanceof ICPPFunction)
////			{
////				MethodInvocation method = jast.newMethod()
////						.on("Globals")
////						.call(replace)
////						.with(eval1Expr(expr.getOperand1()))
////						.with(eval1Expr(expr.getOperand2())).toAST();
////				ret.add(method);
////			}
////			else
////			{
////				assert(false);
////			}
////		}
//		else if (isAssignmentExpression(expr.getOperator()))
//		{
//			if (isEventualPtrDeref(expr.getOperand1()) &&
//				expr.getOperator() == IASTBinaryExpression.op_assign)
//			{
//				// ptr[0] = 5;  or  *ptr = 5; 
//				//  convert to:
//				// ptr.set(5);
//				MethodInvocation meth = jast.newMethod()
//						.on(eval1Expr(expr.getOperand1(), EnumSet.of(Flag.ASSIGN_LEFT_SIDE)))
//						.call("set")
//						.with(eval1Expr(expr.getOperand2())).toAST();
//				
//				ret.add(meth);
//			}
//			else if (isEventualPtrDeref(expr.getOperand1()))
//			{
//				// (*ptr) += 5  or  ptr[0] += 5
//				//  convert to:
//				// ptr.set(ptr.get() + 5);
//				InfixExpression infix = ast.newInfixExpression();
//				infix.setOperator(compoundAssignmentToInfixOperator(expr.getOperator()));
//				infix.setRightOperand(eval1Expr(expr.getOperand2()));
//				infix.setLeftOperand(eval1Expr(expr.getOperand1()));
//				
//				MethodInvocation meth = jast.newMethod()
//						.on(eval1Expr(expr.getOperand1(), EnumSet.of(Flag.ASSIGN_LEFT_SIDE)))
//						.call("set")
//						.with(infix).toAST();
//				
//				ret.add(meth);
//			}
//			else if (isBitfield(expr.getOperand1()) &&
//					 expr.getOperator() == IASTBinaryExpression.op_assign)
//			{
//				// test_with_bit_field = 4;
//				//  converts to:
//				// set__test_with_bit_field(4)
//				MethodInvocation meth = jast.newMethod()
//						.call("set__" + getBitfieldSimpleName(expr.getOperand1()))
//						.with(eval1Expr(expr.getOperand2())).toAST();
//				
//				ret.add(meth);
//			}
//			else if (isBitfield(expr.getOperand1()))
//			{
//				// test_with_bit_field += 3;
//				//  converts to:
//				// set__test_with_bit_field(get__test_with_bit_field() + 3);
//				InfixExpression infix = ast.newInfixExpression();
//				infix.setOperator(compoundAssignmentToInfixOperator(expr.getOperator()));
//				infix.setRightOperand(eval1Expr(expr.getOperand2()));
//				infix.setLeftOperand(eval1Expr(expr.getOperand1()));
//				
//				MethodInvocation meth = jast.newMethod()
//						.on(eval1Expr(expr.getOperand1(), EnumSet.of(Flag.ASSIGN_LEFT_SIDE)))
//						.call("set__" + getBitfieldSimpleName(expr.getOperand1()))
//						.with(infix).toAST();
//				
//				ret.add(meth);
//			}
//			else
//			{
//				// Normal assignment.
//				Assignment assign = jast.newAssign()
//						.left(eval1Expr(expr.getOperand1()))
//						.right(eval1Expr(expr.getOperand2()))
//						.op(evaluateBinaryAssignmentOperator(expr.getOperator())).toAST();
//
//				ret.add(assign);
//			}
//		}
//		else // Non assignment, non-overloaded binary expression.
//		{
//			if (isEventualPtr(expr.getOperand1().getExpressionType()))
//			{
//				MethodInvocation meth = jast.newMethod()
//						.on(eval1Expr(expr.getOperand1()))
//						.call("add")
//						.with(eval1Expr(expr.getOperand2())).toAST();
//				ret.add(meth);
//			}
//			else
//			{
//				boolean fSubsNeedBooleans = needBooleanExpressions(expr.getOperator());
//				InfixExpression infix = jast.newInfix()
//						.left(eval1Expr(expr.getOperand1(), fSubsNeedBooleans ? EnumSet.of(Flag.NEED_BOOLEAN) : EnumSet.noneOf(Flag.class)))
//						.right(eval1Expr(expr.getOperand2(), fSubsNeedBooleans ? EnumSet.of(Flag.NEED_BOOLEAN) : EnumSet.noneOf(Flag.class)))
//						.op(evaluateBinaryOperator(expr.getOperator())).toAST();
//
//				ret.add(infix);
//			}
//		}

		
	}


	/**
	 * Given a binary operator, returns whether we need boolean expression with it.
	 */
	private boolean needBooleanExpressions(int operator)
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_logicalAnd:
		case IASTBinaryExpression.op_logicalOr:
			return true;
		}
		return false;
	}

	/**
	 * Converts a C++ postfix operator to a Java one.
	 */
	private String evalUnaryPostfixOperator(int operator) 
	{
		switch(operator)
		{
		case IASTUnaryExpression.op_postFixDecr:
			return "--";
		case IASTUnaryExpression.op_postFixIncr:
			return "++";
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Converts a C++ prefix operator to a C++ one.
	 */
	private String evalUnaryPrefixOperator(int operator) 
	{
		switch(operator)
		{
		case IASTUnaryExpression.op_prefixDecr:
			return "--";
		case IASTUnaryExpression.op_prefixIncr:
			return "++";
		case IASTUnaryExpression.op_not:
			return "!";
		case IASTUnaryExpression.op_plus:
			return "+";
		case IASTUnaryExpression.op_minus:
			return "-";
		case IASTUnaryExpression.op_tilde:
			return "~";
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Whether a C++ unary expression operator is prefix or postfix.
	 */
	private boolean isPrefixExpression(int operator)
	{
		switch (operator)
		{
		case IASTUnaryExpression.op_prefixDecr:
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_not:
		case IASTUnaryExpression.op_plus:
		case IASTUnaryExpression.op_minus:
		case IASTUnaryExpression.op_tilde:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Whether a C++ unary expression operator is prefix or postfix.
	 */
	private boolean isPostfixExpression(int operator)
	{
		switch (operator)
		{
		case IASTUnaryExpression.op_postFixDecr:
		case IASTUnaryExpression.op_postFixIncr:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Converts a C++ binary assignment operator to a Java one.
	 */
	private String evaluateBinaryAssignmentOperator(int operator) 
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_assign:
			return "=";
		case IASTBinaryExpression.op_binaryAndAssign:
			return "&=";
		case IASTBinaryExpression.op_binaryOrAssign:
			return "|=";
		case IASTBinaryExpression.op_binaryXorAssign:
			return "^=";
		case IASTBinaryExpression.op_divideAssign:
			return "/=";
		case IASTBinaryExpression.op_plusAssign:
			return "+=";
		case IASTBinaryExpression.op_minusAssign:
			return "-=";
		case IASTBinaryExpression.op_multiplyAssign:
			return "*=";
		case IASTBinaryExpression.op_shiftLeftAssign:
			return "<<=";
		case IASTBinaryExpression.op_shiftRightAssign:
			return ">>="; // VERIFY
		default:
			return null;
		}
	}

	/**
	 * Returns whether a C++ binary expression operator is an assignment operator.
	 */
	private boolean isAssignmentExpression(int operator)
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_assign:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXorAssign:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_plusAssign:
		case IASTBinaryExpression.op_minusAssign:
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_shiftLeftAssign:
		case IASTBinaryExpression.op_shiftRightAssign:
			return true;
		default:
			return false;
		}
	}

	private String compoundAssignmentToInfixOperator(int op)
	{
		switch (op)
		{
		case IASTBinaryExpression.op_binaryAndAssign:
			return "&";
		case IASTBinaryExpression.op_binaryOrAssign:
			return "|";
		case IASTBinaryExpression.op_binaryXorAssign:
			return "^"; 
		case IASTBinaryExpression.op_divideAssign:
			return "/";
		case IASTBinaryExpression.op_plusAssign:
			return "+";
		case IASTBinaryExpression.op_minusAssign:
			return "-";
		case IASTBinaryExpression.op_multiplyAssign:
			return "*";
		case IASTBinaryExpression.op_shiftLeftAssign:
			return "<<";
		case IASTBinaryExpression.op_shiftRightAssign:
			return ">>"; // VERIFY
		default:
			return null;
		}
	}
	
	/**
	 * Converts the CDT binary operator to a JDT binary operator.
	 */
	private String evaluateBinaryOperator(int operator)
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_assign:
			return "=";
		case IASTBinaryExpression.op_binaryAnd:
			return "&";
		case IASTBinaryExpression.op_binaryOr:
			return "|";
		case IASTBinaryExpression.op_binaryXor:
			return "^";
		case IASTBinaryExpression.op_divide:
			return "/";
		case IASTBinaryExpression.op_equals:
			return "==";
		case IASTBinaryExpression.op_plus:
			return "+";
		case IASTBinaryExpression.op_minus:
			return "-";
		case IASTBinaryExpression.op_multiply:
			return "*";
		case IASTBinaryExpression.op_notequals:
			return "!=";
		case IASTBinaryExpression.op_greaterEqual:
			return ">=";
		case IASTBinaryExpression.op_greaterThan:
			return ">";
		case IASTBinaryExpression.op_lessEqual:
			return "<=";
		case IASTBinaryExpression.op_lessThan:
			return "<";
		case IASTBinaryExpression.op_logicalAnd:
			return "&&";
		case IASTBinaryExpression.op_logicalOr:
			return "||";
		case IASTBinaryExpression.op_modulo:
			return "%";
		case IASTBinaryExpression.op_shiftLeft:
			return "<<";
		case IASTBinaryExpression.op_shiftRight:
			return ">>";
		default:
			return evaluateBinaryAssignmentOperator(operator);
		}
	}

	/**
	 * Given a C++ initializer, returns a list of Java expressions.
	 * 
	 * @param iastDeclaration Initializer
	 */
	private MSimpleDecl eval1Decl(IASTDeclaration decla) throws DOMException
	{
		MSimpleDecl dec = new MSimpleDecl();

		IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) decla;
		print("simple declaration");
			
		IASTDeclarator decl = simpleDeclaration.getDeclarators()[0];

		IASTName nm = decl.getName();
		IBinding binding = nm.resolveBinding();
		IVariable var = (IVariable) binding;
			
//		TypeEnum type = getTypeEnum(var.getType());

		dec.name = getSimpleName(decl.getName());
		dec.initExpr = eval1Init(decl.getInitializer());
		dec.type = cppToJavaType(var.getType());
		
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
	 * Gets a simple type. Eg. WebCore::RenderObject becomes
	 * RenderObject.
	 */
	private String getSimpleType(String qualifiedType)
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
	private String normalizeName(String name)
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
	private String getSimpleName(IASTName name) throws DOMException
	{
		String nm = name.resolveBinding().getName();
		nm = normalizeName(nm);

		print("name: " + name.resolveBinding().getName() + ":" + nm);
		return nm;
	}

	/**
	 * Gets the complete C++ qualified name.
	 */
	private String getCompleteName(IASTName name) throws DOMException
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
			print("Complete Name: " + ret);
			return ret;
		}

		return binding.getName();
	}

	/**
	 * Gets the qualifier part of a name.
	 */
	private String getQualifiedPart(IASTName name) throws DOMException
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
			print("Qualified Name found: " + ret);
			return ret;
		}

		return "";
	}

	private MStmt createCleanupCall(int until)
	{
		MStmt fcall = ModelCreation.createMethodCall("StackHelper", "cleanup", 
				ModelCreation.createLiteral("null"),
				ModelCreation.createLiteral("__stack"),
				ModelCreation.createLiteral(String.valueOf(until)));
		
		return fcall;
	}
	
	private MStmt eval1Stmt(IASTStatement stmt) throws DOMException
	{
		List<MStmt> ret = evalStmt(stmt);
		assert(ret.size() == 1);
		return ret.get(0);
	}
	
	private boolean isTerminatingStatement(MStmt mStmt)
	{
		if (mStmt instanceof MBreakStmt ||
			mStmt instanceof MReturnStmt ||
			mStmt instanceof MContinueStmt)
			return true;
		
		return false;
	}
	
	private void startNewCompoundStmt(boolean isLoop, boolean isSwitch)
	{
		m_localVariableStack.push(
				new ScopeVar(m_nextVariableId,
						isLoop,
						isSwitch));
	}
	
	private void endCompoundStmt()
	{
		m_localVariableStack.pop();
	}
	
	private MCompoundStmt surround(List<MStmt> stmts) throws DOMException
	{
		if (stmts.size() == 1 && stmts.get(0) instanceof MCompoundStmt)
			return (MCompoundStmt) stmts.get(0);
		
		MCompoundStmt compound = new MCompoundStmt();
		compound.statements.addAll(stmts);
		return compound;
	}

	List<MStmt> globalStmts = new ArrayList<MStmt>();

	/**
	 * Attempts to convert the given C++ statement to one or more Java statements.
	 */
	private List<MStmt> evalStmt(IASTStatement statement) throws DOMException
	{
		List<MStmt> stmts = new ArrayList<MStmt>();
		
		if (statement instanceof IASTBreakStatement)
		{
			print("break");

			Integer temp = findLastSwitchOrLoopId();

			MBreakStmt brk = new MBreakStmt();
			stmts.add(brk);
			
			if (temp != null) // Cleanup back to the closest loop...
				brk.cleanup = createCleanupCall(temp);
		}
		else if (statement instanceof IASTCaseStatement)
		{
			print("case");
			
			IASTCaseStatement caseStatement = (IASTCaseStatement) statement;

			MCaseStmt cs = new MCaseStmt();
			stmts.add(cs);
			
			cs.expr = eval1Expr(caseStatement.getExpression());
		}
		else if (statement instanceof IASTContinueStatement)
		{
			print("continue");

			Integer temp = findLastLoopId();

			MContinueStmt con = new MContinueStmt();
			stmts.add(con);
			
			if (temp != null) // Cleanup back to the closest loop...
				con.cleanup = createCleanupCall(temp);
		}
		else if (statement instanceof IASTDefaultStatement)
		{
			print("default");

			MDefaultStmt def = new MDefaultStmt();
			stmts.add(def);
		}
		else if (statement instanceof IASTGotoStatement)
		{
			print("goto");

			IASTGotoStatement gotoStatement = (IASTGotoStatement) statement;

			MGotoStmt go = new MGotoStmt();
			stmts.add(go);
			
			go.lbl = gotoStatement.getName().toString();
		}
		else if (statement instanceof IASTNullStatement)
		{
			print("Empty statement");
			
			MEmptyStmt empty = new MEmptyStmt();
			stmts.add(empty);
		}
		else if (statement instanceof IASTProblemStatement)
		{
			IASTProblemStatement probStatement = (IASTProblemStatement) statement;

			print("problem: " + probStatement.getProblem().getMessageWithLocation());

			MProblemStmt prob = new MProblemStmt();
			stmts.add(prob);
			
			prob.problem = probStatement.getProblem().getMessageWithLocation();
		}
		else if (statement instanceof IASTCompoundStatement)
		{
			IASTCompoundStatement compoundStatement = (IASTCompoundStatement)statement;
			print("Compound");
			startNewCompoundStmt(false, false);
//			startNewCompoundStmt(
//					isLocationDirectly(Location.DoBody, Location.ForBody, Location.WhileBody),  
//					isLocationDirectly(Location.SwitchBody)
//				);
// TODO
			MCompoundStmt compound = new MCompoundStmt();
			stmts.add(compound);

			for (IASTStatement s : compoundStatement.getStatements())
				compound.statements.add(eval1Stmt(s));
			
			int cnt = m_localVariableStack.peek().cnt;
			m_nextVariableId = m_localVariableStack.peek().id;
			endCompoundStmt();
			
			if (cnt != 0 &&
			    !compound.statements.isEmpty() &&
			    !isTerminatingStatement(compound.statements.get(compound.statements.size() - 1)))
			{
				compound.cleanup = createCleanupCall(m_nextVariableId);
			}
		}
		else if (statement instanceof IASTDeclarationStatement)
		{
			IASTDeclarationStatement declarationStatement = (IASTDeclarationStatement)statement;
			print("Declaration");

			List<String> types = evaluateDeclarationReturnTypes(declarationStatement.getDeclaration());
			List<String> names = evaluateDeclarationReturnNames(declarationStatement.getDeclaration());
			List<MExpression> exprs = evaluateDeclarationReturnInitializers((IASTSimpleDeclaration) declarationStatement.getDeclaration());
			
			for (int i = 0; i < types.size(); i++)
			{
				MSimpleDecl simple = new MSimpleDecl();
				simple.type = types.get(i);
				simple.name = names.get(i); 
				simple.initExpr = exprs.get(i);

				MDeclarationStmt stmt = new MDeclarationStmt();
				stmt.simple = simple;
			
				stmts.add(stmt);
			}
		}
		else if (statement instanceof IASTDoStatement)
		{
			print("Do");

			IASTDoStatement doStatement = (IASTDoStatement)statement;

			MDoStmt dos = new MDoStmt();
			stmts.add(dos);

			dos.body = surround(evalStmt(doStatement.getBody()));
			dos.expr = eval1Expr(doStatement.getCondition());
			dos.expr = makeExpressionBoolean(dos.expr, doStatement.getCondition());
		}
		else if (statement instanceof IASTExpressionStatement)
		{
			print("Expression");

			IASTExpressionStatement expressionStatement = (IASTExpressionStatement)statement;

			MExprStmt exprStmt = new MExprStmt();
			stmts.add(exprStmt);
			exprStmt.expr = eval1Expr(expressionStatement.getExpression());
		}
		else if (statement instanceof IASTForStatement)
		{
			print("For");

			IASTForStatement forStatement = (IASTForStatement)statement;

			MForStmt fs = new MForStmt();
			stmts.add(fs);
			
			if (forStatement.getInitializerStatement() != null)
				fs.initializer = eval1Stmt(forStatement.getInitializerStatement());
			
			if (forStatement.getConditionExpression() != null)
			{
				fs.condition = eval1Expr(forStatement.getConditionExpression());
				fs.condition = makeExpressionBoolean(fs.condition, forStatement.getConditionExpression());
			}

			if (forStatement.getIterationExpression() != null)
				fs.updater = eval1Expr(forStatement.getIterationExpression());
			
			fs.body = surround(evalStmt(forStatement.getBody()));
			
			if (forStatement instanceof ICPPASTForStatement &&
				((ICPPASTForStatement) forStatement).getConditionDeclaration() != null)
			{
				// I really doubt any C++ programmer puts a declaration in the condition space
				// of a for loop but the language seems to allow it.
				// eg. for (int a = 1; int b = 3; a++)
				IType tp = eval1DeclReturnCppType(((ICPPASTForStatement) forStatement).getConditionDeclaration());
				
				fs.decl = eval1Decl( ((ICPPASTForStatement) forStatement).getConditionDeclaration() );
				fs.condition = makeInfixFromDecl(fs.decl.name, fs.decl.initExpr, tp, true);
				fs.decl.initExpr = makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof IASTIfStatement)
		{
			print("If");

			IASTIfStatement ifStatement = (IASTIfStatement)statement;

			MIfStmt ifs = new MIfStmt();
			stmts.add(ifs);

			ifs.condition = eval1Expr(ifStatement.getConditionExpression());
			ifs.condition = makeExpressionBoolean(ifs.condition, ifStatement.getConditionExpression());
			ifs.body = surround(evalStmt(ifStatement.getThenClause()));
			
			if (ifStatement.getElseClause() != null)
				ifs.elseBody = eval1Stmt(ifStatement.getElseClause());
			
			if (ifStatement instanceof ICPPASTIfStatement &&
				((ICPPASTIfStatement) ifStatement).getConditionDeclaration() != null)
			{
				IType tp = eval1DeclReturnCppType(((ICPPASTIfStatement) ifStatement).getConditionDeclaration());

				ifs.decl = eval1Decl(((ICPPASTIfStatement) ifStatement).getConditionDeclaration());
				ifs.condition = makeInfixFromDecl(ifs.decl.name, ifs.decl.initExpr, tp, true);
				ifs.decl.initExpr = makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof IASTLabelStatement)
		{
			print("Label");
			
			IASTLabelStatement labelStatement = (IASTLabelStatement)statement;

			MLabelStmt lbl = new MLabelStmt();
			stmts.add(lbl);
			
			lbl.lbl = labelStatement.getName().toString();
			lbl.body = eval1Stmt(labelStatement.getNestedStatement());
		}
		else if (statement instanceof IASTReturnStatement)
		{
			print("return");

			IASTReturnStatement returnStatement = (IASTReturnStatement)statement;

			MReturnStmt retu = new MReturnStmt();
			stmts.add(retu);
			

			if (isNumberExpression(returnStatement.getReturnValue()))
			{
				 MValueOfExpressionNumber valOfExpr = new MValueOfExpressionNumber();
				 valOfExpr.type = "MInteger"; // TODO
				 valOfExpr.operand = eval1Expr(returnStatement.getReturnValue());
				 
				retu.expr = valOfExpr;
			}
			else
			{
				retu.expr = eval1Expr(returnStatement.getReturnValue());
				retu.expr = callCopyIfNeeded(retu.expr, returnStatement.getReturnValue());
			}
			
			if (currentReturnType.equals("Boolean"))
				retu.expr = makeExpressionBoolean(retu.expr, returnStatement.getReturnValue());

			// Only call cleanup if we have something on the stack.
			if (m_nextVariableId != 0)
				retu.cleanup = createCleanupCall(0);
		}
		else if (statement instanceof IASTSwitchStatement)
		{
			print("Switch");
			
			IASTSwitchStatement switchStatement = (IASTSwitchStatement)statement;

			MSwitchStmt swi = new MSwitchStmt();
			stmts.add(swi);
			
			swi.body = surround(evalStmt(switchStatement.getBody()));
			swi.expr = eval1Expr(switchStatement.getControllerExpression());
			
			if (switchStatement instanceof ICPPASTSwitchStatement &&
			    ((ICPPASTSwitchStatement) switchStatement).getControllerDeclaration() != null)
			{
				IType tp = eval1DeclReturnCppType(((ICPPASTSwitchStatement) switchStatement).getControllerDeclaration());

				swi.decl = eval1Decl(((ICPPASTSwitchStatement) switchStatement).getControllerDeclaration());
				swi.expr = makeInfixFromDecl(swi.decl.name, swi.decl.initExpr, tp, false);
				swi.decl.initExpr = makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof IASTWhileStatement)
		{
			print("while");
			
			IASTWhileStatement whileStatement = (IASTWhileStatement)statement;

			MWhileStmt whi = new MWhileStmt();
			stmts.add(whi);
			
			whi.body = surround(evalStmt(whileStatement.getBody()));
			whi.expr = eval1Expr(whileStatement.getCondition());
			whi.expr = makeExpressionBoolean(whi.expr, whileStatement.getCondition());
			
			if (whileStatement instanceof ICPPASTWhileStatement &&
				((ICPPASTWhileStatement) whileStatement).getConditionDeclaration() != null)
			{
				IType tp = eval1DeclReturnCppType(((ICPPASTWhileStatement) whileStatement).getConditionDeclaration());
				
				whi.decl = eval1Decl(((ICPPASTWhileStatement)whileStatement).getConditionDeclaration());
				whi.expr = makeInfixFromDecl(whi.decl.name, whi.decl.initExpr, tp, true);
				whi.decl.initExpr = makeSimpleCreationExpression(tp);
			}
		}
		else if (statement instanceof ICPPASTTryBlockStatement)
		{
//			ICPPASTTryBlockStatement tryBlockStatement = (ICPPASTTryBlockStatement)statement;
//			print("Try");
//
//			TryStatement trys = ast.newTryStatement();
//
//			trys.setBody(surround(eval1Stmt(tryBlockStatement.getTryBody())));
//
//			for (ICPPASTCatchHandler catchHandler : tryBlockStatement.getCatchHandlers())
//				trys.catchClauses().add(evaluateCatchClause(catchHandler));
//
//			ret.add(trys);
		}
		else if (statement != null)
		{
			printerr(statement.getClass().getCanonicalName());
		}
		
		if (stmts.isEmpty())
			stmts.add(new MEmptyStmt()); // TODO
		return stmts;
	}
	
	
	/**
	 * Given a type, creates a new expression.
	 * eg. 'int' becomes 'new MInteger()'
	 */
	private MExpression makeSimpleCreationExpression(IType tp) throws DOMException
	{
		MClassInstanceCreation create = new MClassInstanceCreation();
		create.name = ModelCreation.createLiteral(cppToJavaType(tp));
		return create;
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
	private MExpression makeInfixFromDecl(String varName, MExpression initExpr, IType tp, boolean makeBoolean) throws DOMException
	{
		TypeEnum te = getTypeEnum(tp);
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

		return makeBoolean ? makeExpressionBoolean(infix, te) : bracket(infix);
	}

	private MExpression makeExpressionBoolean(MExpression exp, IASTExpression expcpp) throws DOMException
	{
		return makeExpressionBoolean(exp, expressionGetType(expcpp));
	}
	
	
	/**
	 * Attempts to make a Java boolean expression. Eg. Adds != null, != 0, etc.
	 */
	private MExpression makeExpressionBoolean(MExpression exp, TypeEnum expType) throws DOMException
	{
		if (expType != TypeEnum.BOOLEAN)
		{
			MExpression r = null;
			
			if (expType == TypeEnum.OBJECT ||
				expType == TypeEnum.POINTER ||
				expType == TypeEnum.REFERENCE)
			{
				r = ModelCreation.createLiteral("null");
			}
			else if (expType == TypeEnum.CHAR)
			{
				r = ModelCreation.createLiteral("'\\0'");
			}
			else if (expType == TypeEnum.NUMBER)
			{
				r = ModelCreation.createLiteral("0");
			}

			return bracket(ModelCreation.createInfixExpr(bracket(exp), r, "!="));
		}
		return bracket(exp);
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

	private enum TypeEnum
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
	};

	/**
	 * Given a typeId returns a Java type. Used in sizeof, etc. 
	 */
	private String evalTypeId(IASTTypeId typeId) throws DOMException
	{
		if (typeId != null)
		{
			return evaluateDeclSpecifierReturnType(typeId.getDeclSpecifier());
		}
		return null;
	}

	private TypeEnum expressionGetType(IASTExpression expr) throws DOMException
	{
		if (expr == null || expr.getExpressionType() == null)
			return TypeEnum.BOOLEAN; // FIXME..
		
		return getTypeEnum(expr.getExpressionType());
	}

	/**
	 * Gets our enum TypeEnum of an IType.
	 */
	private TypeEnum getTypeEnum(IType type) throws DOMException
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
			printerr(((IProblemType) type).getMessage());
			return TypeEnum.UNKNOWN;
		}
			
		printerr("Unknown type: " + type.getClass().getInterfaces()[0].toString());
		exitOnError();
		return null;
	}

	/**
	 * Gets the base type of an array.
	 * @return CDT IType of array.
	 */
	private IType getArrayBaseType(IType type) throws DOMException
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
	private IType getPointerBaseType(IType type) throws DOMException
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
	 * Gets the expressions for the array sizes.
	 * Eg. int a[1][2 + 5] returns a list containing expressions
	 * [1, 2 + 5].
	 */
	private List<MExpression> getArraySizeExpressions(IType type) throws DOMException
	{
		List<MExpression> ret = new ArrayList<MExpression>();
//
		IArrayType arr = (IArrayType) type;
		ret.add(eval1Expr(arr.getArraySizeExpression()));

		while (arr.getType() instanceof IArrayType)
		{
			IArrayType arr2 = (IArrayType) arr.getType();
			ret.add(eval1Expr(arr2.getArraySizeExpression()));
			arr = arr2;
		}

		return ret;
	}

	private String cppToJavaType(IType type) throws DOMException
	{
		return cppToJavaType(type, false, false);
	}
	
	private IASTExpression removeDref(IASTExpression expr)
	{
		if (expr instanceof IASTUnaryExpression &&
			((IASTUnaryExpression) expr).getOperator() == IASTUnaryExpression.op_star)
			return ((IASTUnaryExpression) expr).getOperand();
		else
		{
			assert(false);
			return expr;
		}
	}
	
	private boolean isEventualPtrDeref(IASTExpression expr)
	{
		expr = unwrap(expr);

		// Now check if it is the de-reference operator...
		if (expr instanceof IASTUnaryExpression &&
			((IASTUnaryExpression) expr).getOperator() == IASTUnaryExpression.op_star)
			return true;
		
		if (expr instanceof IASTFieldReference &&
			((IASTFieldReference) expr).isPointerDereference())
			return true;
		
		// Finally check for the array access operator on a pointer...
		if (expr instanceof IASTArraySubscriptExpression &&
			isEventualPtr(((IASTArraySubscriptExpression) expr).getArrayExpression().getExpressionType()))
			return true;
		
		return false;
	}
	
	private boolean isNumberExpression(IASTExpression expr) throws DOMException
	{
		TypeEnum te = getTypeEnum(expr.getExpressionType());
		
		if (te == TypeEnum.BOOLEAN ||
			te == TypeEnum.CHAR ||
			te == TypeEnum.NUMBER)
			return true;
		
		return false;
	}
	
	private boolean isBooleanExpression(IASTExpression expr) throws DOMException
	{
		TypeEnum te = getTypeEnum(expr.getExpressionType());
		
		if (te == TypeEnum.BOOLEAN)
			return true;
		
		return false;
	}
	
	
	private boolean isObjectPtr(IType type) throws DOMException
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
	private boolean isEventualPtr(IType type)
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
	
	private boolean isEventualRef(IType type)
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
	
//	/**
//	 * Returns the template arguments as JDT Types.
//	 * Eg. HashMap<int, Foo> would return Integer, Foo.
//	 */
//	private List<Type> getTypeParams(ICPPTemplateArgument[] typeParams) throws DOMException
//	{
//		List<Type> types = new ArrayList<Type>();
////		for (ICPPTemplateArgument param : typeParams)
////		{
////			if (param.getTypeValue() != null)
////				types.add(cppToJavaType(param.getTypeValue(), false, true));
////		}
//		return types;
//	}
	
	
	/**
	 * Attempts to convert a CDT type to a JDT type.
	 */
	private String cppToJavaType(IType type, boolean retValue, boolean needBoxed) throws DOMException
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
				printerr("Too many pointer indirections");
				exitOnError();
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
			printerr("PROBLEM:" + prob.getMessage() + prob.getFileName() + prob.getLineNumber());

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
			printerr("Problem type: " + prob.getMessage());
			//exitOnError();
			return "PROBLEM";
		}
		else if (type instanceof ICPPTemplateTypeParameter)
		{
			ICPPTemplateTypeParameter template = (ICPPTemplateTypeParameter) type;
			print("template type");
			return template.toString();
		}
		else if (type != null)
		{
			printerr("Unknown type: " + type.getClass().getCanonicalName() + type.toString());
			exitOnError();
		}
		return null;
	}
	
	/**
	 * Get rid of enclosing brackets.
	 */
	private IASTExpression unwrap(IASTExpression expr)
	{
		while (expr instanceof IASTUnaryExpression &&
				((IASTUnaryExpression) expr).getOperator() == IASTUnaryExpression.op_bracketedPrimary)
		{
			expr = ((IASTUnaryExpression) expr).getOperand();
		}
		
		return expr;
	}

	private boolean isBitfield(IASTName name) throws DOMException
	{
		String complete = getCompleteName(name);
		return bitfields.contains(complete);
	}
	
	private boolean isBitfield(IASTExpression expr) throws DOMException
	{
		expr = unwrap(expr);
		
		if (expr instanceof IASTIdExpression &&
			isBitfield(((IASTIdExpression) expr).getName()))
			return true;
		
		if (expr instanceof IASTFieldReference &&
			isBitfield(((IASTFieldReference) expr).getFieldName()))
			return true;
		
		return false;
	}
	
	private String getBitfieldSimpleName(IASTExpression expr) throws DOMException
	{
		if (expr instanceof IASTIdExpression)
			return getSimpleName(((IASTIdExpression) expr).getName());
		else
			return getSimpleName(((IASTFieldReference) expr).getFieldName());
	}
	
	private void addBitfield(IASTName name) throws DOMException
	{
		String complete = getCompleteName(name);
		bitfields.add(complete);
	}

	private Integer findLastLoopId()
	{
		int cnt = 0;
		for (ScopeVar sv : m_localVariableStack)
		{
			cnt += sv.cnt;
			if (sv.isLoop)
				return cnt == 0 ? null : sv.id;
		}
		
		return null;
	}
	
	private Integer findLastSwitchOrLoopId()
	{
		int cnt = 0;
		for (ScopeVar sv : m_localVariableStack)
		{
			cnt += sv.cnt;
			if (sv.isSwitch || sv.isLoop)
				return cnt == 0 ? null : sv.id;
		}
		return null;
	}
	
	private void incrementLocalVariableId()
	{
		m_nextVariableId++;

		if (m_localVariableStack.peek() != null)
			m_localVariableStack.peek().cnt++;
		
		if (m_localVariableMaxId == null || m_nextVariableId > m_localVariableMaxId)
			m_localVariableMaxId = m_nextVariableId;
	}

	// A set of qualified names containing the bitfields...
	private Set<String> bitfields = new HashSet<String>();
	
	private int m_anonEnumCount = 0;
	
	private int m_anonClassCount = 0;
	
	private HashMap<String, String> m_anonEnumMap = new HashMap<String, String>();
	
	private static class ScopeVar
	{
		ScopeVar(int idi, boolean isloop, boolean isswitch)
		{
			id = idi;
			isLoop = isloop;
			isSwitch = isswitch;
		}

		final boolean isLoop;
		final boolean isSwitch;
		final int id;
		int cnt;
	}

	private Integer m_localVariableMaxId;
	private int m_nextVariableId;
	private Deque<ScopeVar> m_localVariableStack = new ArrayDeque<ScopeVar>();
	
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
	
	/**
	 * Traverses the AST of the given translation unit
	 * and tries to convert the C++ abstract syntax tree to
	 * a Java AST.
	 */
	String traverse(IASTTranslationUnit translationUnit)
	{
bitfields.add("cls::_b");
bitfields.add("_b");

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
		for (MExpression expr : expressions)
		{
			ST test2 = group.getInstanceOf("expression_tp");
			test2.add("expr_obj", expr);
			//System.err.println("####" + test2.render());
		}
		
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
