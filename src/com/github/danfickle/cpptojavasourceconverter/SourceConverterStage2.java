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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
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
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
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
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPQualifierType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

/*
 * TODO
 * pointers. TICK.
 * references. TICK.
 * destructors. TICK.
 * enums. TICK.
 * variable declarations inside while, if. TICK.
 * != null, != 0, etc... for return, variable assignment, etc. TICK.
 * default arguments. TICK.
 * bit fields. TICK, need mask and shift calculation.
 * array initializers. TICK.
 * What information do we need from first pass:
 * bit-fields so instance access can be converted to function call.
 * When should copy constructor be called? TICK.
 * Raw types in Ref<> and Ptr<>. TICK.
 * multiple inheritance.
 * Put enums in correct place. TICK.
 * Working with enum values. TICK.
 * Replace Ptr<Integer> with PtrInteger. TICK.
 * Default arguments for function declarations. TICK.
 * If isEventualPtrOrRef then add .val to expression. TICK.
 * TypeDeclarations losing type param info. TICK.
 * Rename String to something. TICK.
 * Address of operator. TICK.
 * Anonymous stuff. TICK.
 * - Anonymous enums. TICK.
 * Switch not working. TICK.
 * Fix wrong type for anon classes (nested).
 * Fix empty stuff in for statement. TICK.
 * Fix new[] and delete[]. TICK.
 * Call destructors on return or end brace. TICK.
 * Copy constructor not always being called. TICK.
 * Inject stack creation at top of method. TICK.
 * Add arguments to stack. TICK.
 * Add return values to stack. TICK.
 * Call destruct directly on delete variables. TICK.
 * Cleanup cleanup mechanism for classes. TICK.
 * static function with default arguments.
 * Call cleanup after function call.
 * Don't cleanup return value. TICK.
 * Cleanup before break statement in switch. TICK.
 * Top level stuff should be static.
 * Generate constructor. TICK.
 * Generate destructor. TICK.
 * Generate default constructors/destructors. TICK.
 * 
 * Generate default copy constructor.
 * Cast allocateArray.
 * Comma operator.
 * Add static modifier to nested classes.
 * Deal with typedef.
 * - Anonymous classes, unions, structs. REGRESSION
 * templates.
 * order of evaluation.
 * operator overloading. 
 * re-parenting.
 * Overuse of op_assign.
 * Copy/delete constructor being called on references.
 */

/**
 * Second stage of the C++ to Java source converter.
 * @author DanFickle
 * http://github.com/danfickle/cpp-to-java-source-converter/
 */
public class SourceConverterStage2
{
	/**
	 * Builds default argument function calls.
	 * For example:
	 *   int func_with_defaults(int one, int two = 5);
	 * would generate:
	 * 	 int func_with_defaults(int one) {
	 *	     return func_with_defaults(one, 5);
	 *   }
	 */
	private void makeDefaultCalls(IASTFunctionDeclarator func, IBinding funcBinding, TypeDeclaration decl) throws DOMException
	{
		List<Expression> defaultValues = getDefaultValues(func);

		for (int k = defaultValues.size() - 1; k >= 0; k--)
		{
			if (defaultValues.get(k) == null)
				break;

			JASTHelper.MethodDecl methodDef = jast.newMethodDecl()
					.name(getSimpleName(func.getName()))
					.returnType(evalReturnType(funcBinding));

			List<SingleVariableDeclaration> list = evalParameters(funcBinding);

			for (int k2 = 0; k2 < k; k2++)
				methodDef.toAST().parameters().add(list.get(k2));

			Block funcBlockDef = ast.newBlock();
			JASTHelper.Method method = jast.newMethod()
					.call(getSimpleName(func.getName()));

			List<SimpleName> names = getArgumentNames(funcBinding);
			for (int k3 = 0; k3 < k; k3++)
				method.with(names.get(k3));

			List<Expression> vals = getDefaultValues(func);
			for (int k4 = k; k4 < defaultValues.size(); k4++)
				method.with(vals.get(k4));

			if (evalReturnType(funcBinding).toString().equals("void"))
				funcBlockDef.statements().add(ast.newExpressionStatement(method.toAST()));
			else
				funcBlockDef.statements().add(jast.newReturn(method.toAST()));

			methodDef.toAST().setBody(funcBlockDef);
			
			if (decl == null)
				decl = compositeMap.get(getQualifiedPart(func.getName())).tyd;

			decl.bodyDeclarations().add(methodDef.toAST());
		}
	}
	
	private void generateCtorStatements(List<FieldInfo> fields, MethodDeclaration method)
	{
		int start = 0;
		for (FieldInfo fieldInfo : fields)
		{
			print(fieldInfo.field.getName());

			if (fieldInfo.init != null)
			{
				Assignment assign = jast.newAssign()
						.left(ast.newSimpleName(fieldInfo.field.getName()))
						.right(fieldInfo.init)
						.op(Assignment.Operator.ASSIGN).toAST();

				// Add assignment statements to start of generated method...
				method.getBody().statements().add(start, ast.newExpressionStatement(assign));
				start++;
			}
		}
	}

	private void generateDtorStatements(List<FieldInfo> fields, MethodDeclaration method) throws DOMException
	{
		for (int i = fields.size() - 1; i >= 0; i--)
		{
			print(fields.get(i).field.getName());

			if (getTypeEnum(fields.get(i).field.getType()) == TypeEnum.OTHER)
			{
				MethodInvocation meth = jast.newMethod()
						.on(ast.newSimpleName(fields.get(i).field.getName()))
						.call("destruct").toAST();
				method.getBody().statements().add(ast.newExpressionStatement(meth));					
			}
			else if (getTypeEnum(fields.get(i).field.getType()) == TypeEnum.ARRAY &&
					getTypeEnum(getArrayBaseType(fields.get(i).field.getType())) == TypeEnum.OTHER)
			{
				MethodInvocation meth = jast.newMethod()
						.on("DestructHelper")
						.call("destructArray")
						.with(ast.newSimpleName(fields.get(i).field.getName())).toAST();
				method.getBody().statements().add(ast.newExpressionStatement(meth));					
			}
		}
	}
	
	/**
	 * Evaluates a function definition and converts it to Java.
	 */
	private void evalFunction(IASTDeclaration declaration) throws DOMException
	{
		IASTFunctionDefinition func = (IASTFunctionDefinition) declaration;
		IBinding funcBinding = func.getDeclarator().getName().resolveBinding();
		
		JASTHelper.MethodDecl method = jast.newMethodDecl()
				.name(getSimpleName(func.getDeclarator().getName()))
				.setStatic(((IFunction) funcBinding).isStatic())
				.returnType(evalReturnType(funcBinding));
		
		boolean isCtor = false, isDtor = false;

		// Only constructors and destructors can be missing a return type...
		if (method.toAST().getReturnType2() == null)
		{
			if (!method.toAST().getName().getIdentifier().contains("destruct"))
			{
				method.setCtor(true);
				isCtor = true;
			}
			else
			{
				// Need to be public for interface method...
				method.toAST().modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
				isDtor = true;
			}
		}

		method.toAST().parameters().addAll(evalParameters(funcBinding));
		
		m_localVariableMaxId = -1;
		m_nextVariableId = 0;
		method.toAST().setBody(surround(eval1Stmt(func.getBody())));

		if (m_localVariableMaxId != -1)
		{
			Block blk = method.toAST().getBody();

			ArrayCreation arrayCreate = jast.newArray()
						.onType("Object")
						.dim(m_localVariableMaxId).toAST();

			VariableDeclarationStatement stmt2 = jast.newVarDeclStmt()
					.name("__stack")
					.init(arrayCreate)
					.type(ast.newArrayType(jast.newType("Object"))).toAST();

			blk.statements().add(0, stmt2);
		}

		IASTDeclSpecifier declSpecifier = compositeMap.get(getQualifiedPart(func.getDeclarator().getName())).declSpecifier;

		List<FieldInfo> fields = collectFieldsForClass(declSpecifier);
		
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
						ClassInstanceCreation create = jast.newClassCreate()
								.type(cppToJavaType(((IVariable) chain.getMemberInitializerId().resolveBinding()).getType()))
								.withAll(evalExpr(chain.getInitializerValue())).toAST();

						for (FieldInfo fieldInfo : fields)
						{
							if (chain.getMemberInitializerId().resolveBinding().getName().equals(fieldInfo.field.getName()))
								fieldInfo.init = create;
						}
					}
					else if (chain.getInitializerValue() != null)
					{
						for (FieldInfo fieldInfo : fields)
						{
							if (chain.getMemberInitializerId().resolveBinding().getName().equals(fieldInfo.field.getName()))
								fieldInfo.init = eval1Expr(chain.getInitializerValue());
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
			generateCtorStatements(fields, method.toAST());
		}
		else if (isDtor)
		{
			generateDtorStatements(fields, method.toAST());
		}
		
		// Now add the method to the appropriate class declaration...
		CompositeInfo info = compositeMap.get(getQualifiedPart(func.getDeclarator().getName()));

		if (info == null)
		{
			info = new CompositeInfo(ast.newTypeDeclaration());
			String nm = getQualifiedPart(func.getDeclarator().getName()).replace(':', '_');
			print(nm);
			if (nm.isEmpty())
				nm = "Globals";
			
			info.tyd.setName(ast.newSimpleName(nm));
			// TODO get name from db...	
			unit.types().add(info.tyd);
			compositeMap.put(getQualifiedPart(func.getDeclarator().getName()), info);
		}
		
		info.tyd.bodyDeclarations().add(method.toAST());		
		
		// Use this if you want to generate additional
		// functions for default arguments...
		makeDefaultCalls(func.getDeclarator(), funcBinding, info.tyd);
	}
	
	/**
	 * Generates getters and setters for bit fields.
	 * For example:
	 *   int test_with_bit_field : 1;
	 * would generate:
	 * 	 int get__test_with_bit_field()
	 *   {
	 *	   return __bitfields & 1;
	 *   }
	 *   void set__test_with_bit_field(int val)
	 *   {
	 *     __bitfields &= ~1;
	 *	   __bitfields |= (val << 0) & 1;
	 *   }
	 */
	private void generateBitField(IBinding binding, IASTDeclarator declarator) throws DOMException
	{
		IField ifield = (IField) binding;

		JASTHelper.MethodDecl methodBit = jast.newMethodDecl()
				.returnType(cppToJavaType(ifield.getType()))
				.name("get__" + ifield.getName());

		InfixExpression infix = jast.newInfix()
				.left(ast.newSimpleName("__bitfields"))
				.right(jast.newNumber(1))
				.op(InfixExpression.Operator.AND).toAST();
		
		ReturnStatement ret = jast.newReturn(infix);

		Block funcBlock = ast.newBlock();
		funcBlock.statements().add(ret);
		methodBit.toAST().setBody(funcBlock);

		TypeDeclaration decl = compositeMap.get(getQualifiedPart(declarator.getName())).tyd;
		decl.bodyDeclarations().add(methodBit.toAST());

		JASTHelper.MethodDecl methodSet = jast.newMethodDecl()
				.returnType(ast.newPrimitiveType(PrimitiveType.VOID))
				.name("set__" + ifield.getName());
		
		SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
		var.setType(cppToJavaType(ifield.getType()));
		var.setName(ast.newSimpleName("val"));
		methodSet.toAST().parameters().add(var);

		PrefixExpression prefix = ast.newPrefixExpression();
		prefix.setOperator(PrefixExpression.Operator.COMPLEMENT);
		prefix.setOperand(ast.newNumberLiteral(String.valueOf(1)));

		Assignment assign = jast.newAssign()
				.left(ast.newSimpleName("__bitfields"))
				.right(prefix)
				.op(Assignment.Operator.BIT_AND_ASSIGN).toAST();
				
		ExpressionStatement exprStmt = ast.newExpressionStatement(assign);

		InfixExpression shift = jast.newInfix()
				.left(ast.newSimpleName("val"))
				.right(jast.newNumber(0))
				.op(InfixExpression.Operator.LEFT_SHIFT).toAST();

		ParenthesizedExpression paren = jast.newParen(shift);

		InfixExpression mask = jast.newInfix()
				.left(paren)
				.right(jast.newNumber(1))
				.op(InfixExpression.Operator.AND).toAST();

		Assignment orequal = jast.newAssign()
				.left(ast.newSimpleName("__bitfields"))
				.right(mask)
				.op(Assignment.Operator.BIT_OR_ASSIGN).toAST();

		Block funcBlockSet = ast.newBlock();
		funcBlockSet.statements().add(exprStmt);
		funcBlockSet.statements().add(ast.newExpressionStatement(orequal));
		methodSet.toAST().setBody(funcBlockSet);

		decl.bodyDeclarations().add(methodSet.toAST());
	}
	
	/**
	 * Generates a Java field, given a C++ field.
	 */
	private void generateField(IBinding binding, IASTDeclarator declarator, Expression init) throws DOMException
	{
		IField ifield = (IField) binding;

		VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(ifield.getName()));
		
		if (getTypeEnum(ifield.getType()) == TypeEnum.OTHER ||
			getTypeEnum(ifield.getType()) == TypeEnum.ARRAY)
		{
			frag.setInitializer(init);
		}

		FieldDeclaration field = ast.newFieldDeclaration(frag);

		if (ifield.getType().toString().isEmpty())
			field.setType(jast.newType("AnonClass" + (m_anonClassCount - 1)));
		else
			field.setType(cppToJavaType(ifield.getType()));

		TypeDeclaration decl = compositeMap.get(getQualifiedPart(declarator.getName())).tyd;
		decl.bodyDeclarations().add(field);
	}
	
	/**
	 * Generates a Java field, given a C++ variable.
	 */
	private void generateVariable(IBinding binding, IASTDeclarator declarator, Expression init) throws DOMException
	{
		IVariable ifield = (IVariable) binding;

		VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(ifield.getName()));
		frag.setInitializer(init);

		FieldDeclaration field = ast.newFieldDeclaration(frag);

		if (ifield.getType().toString().isEmpty())
			field.setType(jast.newType("AnonClass" + (m_anonClassCount - 1)));
		else
			field.setType(cppToJavaType(ifield.getType()));

		CompositeInfo info = compositeMap.get(getQualifiedPart(declarator.getName()));
		
		if (info != null)
			info.tyd.bodyDeclarations().add(field);
		else
			compositeMap.get("").tyd.bodyDeclarations().add(field);
	}
	
	private void lookForConstructors(IASTDeclSpecifier declSpec, CompositeInfo info) throws DOMException
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

					// TODO: check if copy constructor...
				}
				else if (((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding() instanceof ICPPMethod)
				{
					ICPPMethod meth = (ICPPMethod) ((IASTFunctionDefinition)decl).getDeclarator().getName().resolveBinding();
					
					if (meth.isDestructor())
						info.hasDtor = true;
				}
			}
		}
	}
	
	
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

				List<Expression> exprs = evaluateDeclarationReturnInitializers(simple, false);				
				
				int i = 0;
				for (IASTDeclarator declarator : simple.getDeclarators())
				{
					IBinding binding = declarator.getName().resolveBinding();
				
					if (binding instanceof IField)
					{
						FieldInfo field = new FieldInfo(declarator, exprs.get(i), (IField) binding);
						fields.add(field);

						if (declarator instanceof IASTFieldDeclarator &&
							((IASTFieldDeclarator) declarator).getBitFieldSize() != null)
							field.isBitfield = true;
					}
					i++;
				}
			}
		}
		return fields;
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

			List<Expression> exprs = evaluateDeclarationReturnInitializers(simple, false);
			int i = 0;

			for (IASTDeclarator declarator : simple.getDeclarators())
			{
				IBinding binding = declarator.getName().resolveBinding();

				if (declarator instanceof IASTFieldDeclarator &&
					((IASTFieldDeclarator) declarator).getBitFieldSize() != null)
				{
					print("bit field");
					// We replace bit field fields with getter and setter methods...
					generateBitField(binding, declarator);
				}
				else if (binding instanceof IField)
				{
					print("standard field");
					generateField(binding, declarator, null);
				}
				else if (binding instanceof IFunction &&
						declarator instanceof IASTFunctionDeclarator)
				{
					makeDefaultCalls((IASTFunctionDeclarator) declarator, binding, null);
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
			print("template declaration");
			printerr(templateDeclaration.getDeclaration().getClass().getCanonicalName());
			List<TypeParameter> templateTypes = getTemplateParams(templateDeclaration.getTemplateParameters());
			templateParamsQueue = templateTypes;
			
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

	private List<TypeParameter> templateParamsQueue = new ArrayList<TypeParameter>(); // TODO...
	
	/**
	 * Gets a list of template type parameters.
	 */
	private List<TypeParameter> getTemplateParams(ICPPASTTemplateParameter[] templateParams) throws DOMException
	{
		List<TypeParameter> ret = new ArrayList<TypeParameter>();
		
		for (ICPPASTTemplateParameter parameter : templateParams)
		{
			if (parameter instanceof ICPPASTParameterDeclaration)
			{
				ICPPASTParameterDeclaration parameterDeclaration = (ICPPASTParameterDeclaration)parameter;
				printerr("parameterDeclaration: " + parameter.getRawSignature() + parameterDeclaration.getDeclarator().getName().resolveBinding().getClass().getCanonicalName());

				// Not much we can do with this...
				String str = parameterDeclaration.getDeclarator().getName().resolveBinding().getName();
				TypeParameter typeParam = ast.newTypeParameter();
				typeParam.setName(ast.newSimpleName(normalizeName(str)));
				ret.add(typeParam);
			}
			else if (parameter instanceof ICPPASTSimpleTypeTemplateParameter)
			{
				ICPPASTSimpleTypeTemplateParameter simpleTypeTemplateParameter = (ICPPASTSimpleTypeTemplateParameter)parameter;
				print("simpletypeTemplateparameter");

				TypeParameter typeParam = ast.newTypeParameter();
				typeParam.setName(ast.newSimpleName(simpleTypeTemplateParameter.getName().resolveBinding().getName()));
				ret.add(typeParam);
			}
			else if (parameter instanceof ICPPASTTemplatedTypeTemplateParameter)
			{
				//ICPPASTTemplatedTypeTemplateParameter templatedTypeTemplateParameter = (ICPPASTTemplatedTypeTemplateParameter)parameter;
				printerr("templatedtypetemplate: " + parameter.getRawSignature());
				TypeParameter typeParam = ast.newTypeParameter();
				typeParam.setName(ast.newSimpleName("PROBLEM"));
				ret.add(typeParam);
				// We don't support nested templates at this stage...
				//exitOnError();

				//for (ICPPASTTemplateParameter childParameter : templatedTypeTemplateParameter.getTemplateParameters())
				//	evaluate(childParameter);
			}
		}
	
		return ret;
	}
	
	
	/**
	 * Gets the argument names for a function.
	 */
	private List<SimpleName> getArgumentNames(IBinding funcBinding) throws DOMException
	{
		List<SimpleName> names = new ArrayList<SimpleName>();

		if (funcBinding instanceof IFunction)
		{
			IFunction func = (IFunction) funcBinding;
			IParameter[] params = func.getParameters();

			int missingCount = 0;
			for (IParameter param : params)
			{	
				if (param.getName() == null || param.getName().isEmpty())
					names.add(ast.newSimpleName("missing" + missingCount++));
				else
					names.add(ast.newSimpleName(param.getName()));
			}
		}
		return names;
	}

	/**
	 * Gets the default expressions for function arguments.
	 */
	private List<Expression> getDefaultValues(IASTFunctionDeclarator func) throws DOMException
	{
		IASTStandardFunctionDeclarator declarator = (IASTStandardFunctionDeclarator) func;
		IASTParameterDeclaration[] params = declarator.getParameters();

		List<Expression> exprs = new ArrayList<Expression>();

		for (IASTParameterDeclaration param : params)
		{
			IASTDeclarator paramDeclarator = param.getDeclarator(); 

			if (paramDeclarator.getInitializer() != null)
				exprs.add(evaluate(paramDeclarator.getInitializer()).get(0));
			else
				exprs.add(null);
		}

		return exprs;
	}

	/**
	 * Evaluates the parameters for a function. A SingleVariableDeclaration
	 * contains a type and a name. 
	 * @return A list of JDT SingleVariableDeclaration (in order).
	 */
	private List<SingleVariableDeclaration> evalParameters(IBinding funcBinding) throws DOMException
	{
		List<SingleVariableDeclaration> ret = new ArrayList<SingleVariableDeclaration>();

		if (funcBinding instanceof IFunction)
		{
			IFunction func = (IFunction) funcBinding;
			IParameter[] params = func.getParameters();

			int missingCount = 0;
			for (IParameter param : params)
			{	
				SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
				var.setType(cppToJavaType(param.getType()));

				print("Found param: " + param.getName());

				// Remember C++ can have missing function argument names...
				if (param.getName() == null || param.getName().isEmpty())
					var.setName(ast.newSimpleName("missing" + missingCount++));
				else
					var.setName(ast.newSimpleName(param.getName()));

				ret.add(var);
			}
		}
		return ret;
	}

	/**
	 * Gets the type of the return value of a function.
	 * @return A JDT Type.
	 */
	private Type evalReturnType(IBinding funcBinding) throws DOMException
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
	private List<Type> evaluateDeclarationReturnTypes(IASTDeclaration declaration) throws DOMException
	{
		List<Type> ret = new ArrayList<Type>();

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
						ret.add(cppToJavaType(((IVariable) binding).getType()));
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

	private Type evaluateDeclSpecifierReturnType(IASTDeclSpecifier declSpecifier) throws DOMException
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

			return jast.newType(getSimpleName(namedTypeSpecifier.getName()));

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

	/**
	 * Given a C++ enumeration, converts it to the Java equivalent.
	 */
	private void generateEnumeration(IASTEnumerationSpecifier enumerationSpecifier) throws DOMException
	{
		IASTEnumerator[] enumerators = enumerationSpecifier.getEnumerators();
		
		if (enumerators.length == 0)
			return;
		
		EnumDeclaration enumd = ast.newEnumDeclaration();
		CompositeInfo info = compositeMap.get(getQualifiedPart(enumerationSpecifier.getName()));

		if (info != null)
			info.tyd.bodyDeclarations().add(enumd);
		else
			unit.types().add(enumd);
		
		String finalName;

		if (!getSimpleName(enumerationSpecifier.getName()).equals("MISSING"))
		{
			finalName = getSimpleName(enumerationSpecifier.getName());
			enumd.setName(ast.newSimpleName(finalName));
		}
		else
		{
			// If the enum is anonymous we save the first enum value name
			// in a map so we can use it as a key to lookup the given name of this
			// enumeration later...
			IASTEnumerator first = enumerators[0];
			String nm = first.getName().resolveBinding().getName();
			finalName = "AnonEnum" + m_anonEnumCount;
			enumd.setName(ast.newSimpleName(finalName));
			m_anonEnumMap.put(nm, finalName);
			m_anonEnumCount++;
		}

		IASTExpression lastValue = null;
		int sinceLastValue = 1;
		for (int i = 0; i < enumerators.length; i++)
		{
			EnumConstantDeclaration enumc = ast.newEnumConstantDeclaration();
			enumc.setName(ast.newSimpleName(getSimpleName(enumerators[i].getName())));
			enumd.enumConstants().add(enumc);

			if (enumerators[i].getValue() != null)
			{
				enumc.arguments().add(eval1Expr(enumerators[i].getValue()));
				lastValue = enumerators[i].getValue();
				sinceLastValue = 1;
			}
			else if (lastValue != null)
			{
				ParenthesizedExpression paren = jast.newParen(eval1Expr(lastValue));

				InfixExpression plus = jast.newInfix()
						.left(paren)
						.right(jast.newNumber(sinceLastValue))
						.op(InfixExpression.Operator.PLUS).toAST();

				enumc.arguments().add(plus);
				sinceLastValue++;
			}
			else
			{
				enumc.arguments().add(jast.newNumber(i));
			}
		}

		VariableDeclarationFragment var = ast.newVariableDeclarationFragment();
		var.setName(ast.newSimpleName("val"));
		FieldDeclaration field = ast.newFieldDeclaration(var);
		field.setType(ast.newPrimitiveType(PrimitiveType.INT));
		field.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
		enumd.bodyDeclarations().add(field);

		MethodDeclaration con = jast.newMethodDecl()
				.name(finalName)
				.setCtor(true).toAST();
		
		SingleVariableDeclaration var2 = ast.newSingleVariableDeclaration();
		var2.setType(ast.newPrimitiveType(PrimitiveType.INT));
		var2.setName(ast.newSimpleName("enumVal"));
		var2.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
		con.parameters().add(var2);

		Assignment expr = jast.newAssign()
				.left(ast.newSimpleName("val"))
				.right(ast.newSimpleName("enumVal"))
				.op(Assignment.Operator.ASSIGN).toAST();
		ExpressionStatement exprStmt = ast.newExpressionStatement(expr);

		Block conblk = ast.newBlock();
		conblk.statements().add(exprStmt);
		con.setBody(conblk);
		enumd.bodyDeclarations().add(con);

		MethodDeclaration method = jast.newMethodDecl()
				.name("fromValue")
				.setStatic(true)
				.returnType(jast.newType(finalName)).toAST();

		enumd.bodyDeclarations().add(method);

		SingleVariableDeclaration var3 = ast.newSingleVariableDeclaration();
		var3.setType(ast.newPrimitiveType(PrimitiveType.INT));
		var3.setName(ast.newSimpleName("enumVal"));
		var3.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
		method.parameters().add(var3);

		Block methodblk = ast.newBlock();
		method.setBody(methodblk);
		SwitchStatement switchStmt = ast.newSwitchStatement();
		methodblk.statements().add(switchStmt);
		switchStmt.setExpression(ast.newSimpleName("enumVal"));

		lastValue = null;
		sinceLastValue = 1;
		for (int i = 0; i < enumerators.length; i++)
		{
			SwitchCase cs = ast.newSwitchCase();
			switchStmt.statements().add(cs);

			if (enumerators[i].getValue() != null)
			{
				cs.setExpression(eval1Expr(enumerators[i].getValue()));				
				lastValue = enumerators[i].getValue();
				sinceLastValue = 1;
			}
			else if (lastValue != null)
			{
				ParenthesizedExpression paren = jast.newParen(eval1Expr(lastValue));
				
				InfixExpression plus = jast.newInfix()
						.left(paren)
						.right(jast.newNumber(sinceLastValue))
						.op(InfixExpression.Operator.PLUS).toAST();

				cs.setExpression(plus);
				sinceLastValue++;
			}
			else
			{
				cs.setExpression(ast.newNumberLiteral(String.valueOf(i)));
			}

			ReturnStatement ret = jast.newReturn(ast.newSimpleName(getSimpleName(enumerators[i].getName())));
			switchStmt.statements().add(ret);
		}

		ThrowStatement throwStmt = ast.newThrowStatement();

		ClassInstanceCreation create = jast.newClassCreate()
				.type(jast.newType("ClassCastException")).toAST();
				
		throwStmt.setExpression(create);

		SwitchCase def = ast.newSwitchCase();
		def.setExpression(null);
		switchStmt.statements().add(def);
		switchStmt.statements().add(throwStmt);
	}


	/**
	 * Attempts to evaluate the given declaration specifier
	 */
	private TypeDeclaration evalDeclSpecifier(IASTDeclSpecifier declSpecifier) throws DOMException
	{
		if (declSpecifier instanceof IASTCompositeTypeSpecifier)
		{
			IASTCompositeTypeSpecifier compositeTypeSpecifier = (IASTCompositeTypeSpecifier)declSpecifier;
			print("composite type specifier");

			TypeDeclaration tyd = ast.newTypeDeclaration();
			
			CompositeInfo info = compositeMap.get(getQualifiedPart(compositeTypeSpecifier.getName()));

			if (info != null)
				info.tyd.bodyDeclarations().add(tyd);
			else
				unit.types().add(tyd);

			info = new CompositeInfo(tyd);
			
			if (compositeTypeSpecifier.getKey() == IASTCompositeTypeSpecifier.k_union)
			{
				Javadoc jd = ast.newJavadoc();
				TagElement tg = ast.newTagElement();
				tg.setTagName("@union");
				jd.tags().add(tg);
				tyd.setJavadoc(jd);
			}
			
			String finalName;
			if (getSimpleName(compositeTypeSpecifier.getName()).equals("MISSING"))
			{
				finalName = "AnonClass" + m_anonClassCount;
				compositeMap.put(getCompleteName(compositeTypeSpecifier.getName()), info);
				m_anonClassCount++;
			}
			else
			{
				finalName = getSimpleName(compositeTypeSpecifier.getName());
				compositeMap.put(getCompleteName(compositeTypeSpecifier.getName()), info);
			}

			info.declSpecifier = declSpecifier;
			lookForConstructors(declSpecifier, info);
		
			tyd.setName(ast.newSimpleName(finalName));

			tyd.typeParameters().addAll(templateParamsQueue);			
			templateParamsQueue.clear();
			
			ParameterizedType type = ast.newParameterizedType(jast.newType("CppType"));
			type.typeArguments().add(jast.newType(finalName));
			tyd.superInterfaceTypes().add(type);
			
			if (compositeTypeSpecifier instanceof ICPPASTCompositeTypeSpecifier)
			{
				ICPPASTCompositeTypeSpecifier cppCompositeTypeSpecifier = (ICPPASTCompositeTypeSpecifier)compositeTypeSpecifier;

				if (cppCompositeTypeSpecifier.getBaseSpecifiers() != null && cppCompositeTypeSpecifier.getBaseSpecifiers().length != 0)
				{
					tyd.setSuperclassType(jast.newType(getSimpleName(cppCompositeTypeSpecifier.getBaseSpecifiers()[0].getName())));
				}
				
//				for (ICPPASTBaseSpecifier base : cppCompositeTypeSpecifier.getBaseSpecifiers())
//					getSimpleName(base.getName());
			}

			for (IASTDeclaration decl : compositeTypeSpecifier.getMembers())
			{
				evalDeclaration(decl);
			}
			
			if (!info.hasCtor)
			{
				MethodDeclaration meth = jast.newMethodDecl()
						.returnType(null)
						.name(finalName)
						.setCtor(true).toAST();
				
				Block blk = ast.newBlock();
				meth.setBody(blk);
			
				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);
				generateCtorStatements(fields, meth);
				tyd.bodyDeclarations().add(meth);
			}
			
			if (!info.hasDtor)
			{
				MethodDeclaration meth = jast.newMethodDecl()
						.returnType(ast.newPrimitiveType(PrimitiveType.VOID))
						.name("destruct").toAST();
				
				// Overriden interface methods must be public...
				meth.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));

				Block blk = ast.newBlock();
				meth.setBody(blk);

				List<FieldInfo> fields = collectFieldsForClass(declSpecifier);
				generateDtorStatements(fields, meth);
				tyd.bodyDeclarations().add(meth);
			}
			
			// Add a copy method...
			MethodDeclaration meth = jast.newMethodDecl()
					.returnType(jast.newType(finalName))
					.name("copy").toAST();
			
			// Overriden interface methods must be public...
			meth.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));

			ClassInstanceCreation create = ast.newClassInstanceCreation();
			create.setType(jast.newType(finalName));
			create.arguments().add(ast.newThisExpression());
			ReturnStatement stmt = jast.newReturn(create);

			Block blk = ast.newBlock();
			blk.statements().add(stmt);
			meth.setBody(blk);

			tyd.bodyDeclarations().add(meth);			
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
			generateEnumeration(enumerationSpecifier);
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

	/**
	 * Returns the initializer expression for a declaration such as contained 
	 * in while (int a = b) {...  
	 */
	private IASTExpression evalDeclarationReturnFirstInitializerExpression(IASTDeclaration declaration) throws DOMException
	{
		if (declaration instanceof IASTSimpleDeclaration &&
			((IASTSimpleDeclaration) declaration).getDeclarators().length == 1 &&
			((IASTSimpleDeclaration) declaration).getDeclarators()[0].getInitializer() instanceof IASTEqualsInitializer &&
			((IASTEqualsInitializer)((IASTSimpleDeclaration) declaration).getDeclarators()[0].getInitializer()).getInitializerClause() instanceof IASTExpression)
		{
			return (IASTExpression) ((IASTEqualsInitializer)((IASTSimpleDeclaration) declaration).getDeclarators()[0].getInitializer()).getInitializerClause();
		}
		else
		{
			printerr("Unexpected declaration: " + declaration.getClass().getCanonicalName());
			exitOnError();
			return null;
		}
	}
	
	private Expression generateArrayCreationExpression(IType tp, List<Expression> sizeExprs) throws DOMException
	{
		Type jtp = null;
		boolean isBasic = false;
		
		if ((getTypeEnum(tp) == TypeEnum.ARRAY))
		{
			jtp = cppToJavaType(getArrayBaseType(tp));
			TypeEnum te = getTypeEnum(getArrayBaseType(tp));
			isBasic = te == TypeEnum.BOOLEAN || te == TypeEnum.CHAR || te == TypeEnum.NUMBER; 
		}
		else if ((getTypeEnum(tp) == TypeEnum.POINTER))
		{
			jtp = cppToJavaType(getPointerBaseType(tp));
			TypeEnum te = getTypeEnum(getPointerBaseType(tp));
			isBasic = te == TypeEnum.BOOLEAN || te == TypeEnum.CHAR || te == TypeEnum.NUMBER; 
		}
		else
		{
			printerr("unexpected type here: " + tp.getClass().getCanonicalName());
			System.exit(-1);
		}

		if (!isBasic)
		{
			TypeLiteral tl = ast.newTypeLiteral();
			tl.setType(jtp);

			return jast.newMethod()
					.on("CreateHelper")
					.call("allocateArray")
					.with(tl)
					.withArguments(sizeExprs).toAST();
		}
		else
		{
			ArrayCreation create = ast.newArrayCreation();
			if ((jtp instanceof ArrayType))
			{
				create.setType((ArrayType) jtp);
			}
			else
			{
				ArrayType arr = ast.newArrayType(jtp);
				create.setType(arr);
			}
			
			create.dimensions().addAll(sizeExprs);
			return create;
		}
	}
	
	private MethodInvocation createAddItemCall(Expression item)
	{
		MethodInvocation meth = jast.newMethod()
				.on("StackHelper").call("addItem")
				.with(item)
				.with(m_nextVariableId)
				.with("__stack").toAST();
		incrementLocalVariableId();
		return meth;
	}
	
	/**
	 * Given a declaration, returns a list of initializer expressions.
	 * Eg. int a = 1, b = 2, c; will return [1, 2, null].
	 */
	private List<Expression> evaluateDeclarationReturnInitializers(IASTDeclaration declaration, boolean wrap) throws DOMException
	{
		List<Expression> ret = new ArrayList<Expression>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			print("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
			{
				ret.add(null);

				IASTName nm = decl.getName();
				IBinding binding = nm.resolveBinding();

				if (binding instanceof IVariable)
				{
					IVariable var = (IVariable) binding;
					TypeEnum type = getTypeEnum(var.getType());
					if (type == TypeEnum.OTHER || type == TypeEnum.REFERENCE)
					{
						ClassInstanceCreation create = jast.newClassCreate()
								.type(cppToJavaType(var.getType()))
								.withAll(evaluate(decl.getInitializer())).toAST();
						
						if (wrap)
						{
							MethodInvocation meth = createAddItemCall(create);
							ret.set(ret.size() - 1, meth);
						}
						else
							ret.set(ret.size() - 1, create);
					}
					else if (type == TypeEnum.ARRAY)
					{
						print("Found array");
						Expression ex = generateArrayCreationExpression(var.getType(), getArraySizeExpressions(var.getType()));

						TypeEnum te = getTypeEnum(getArrayBaseType(var.getType()));
						
						if ((te == TypeEnum.OTHER || te == TypeEnum.REFERENCE || te == TypeEnum.POINTER) &&
							wrap)
						{
							MethodInvocation meth = createAddItemCall(ex);
							ret.set(ret.size() - 1, meth);
						}
						else
							ret.set(ret.size() - 1, ex);
					}
					else
					{
						List<Expression> exprs = evaluate(decl.getInitializer());
						if (!exprs.isEmpty())
							ret.set(ret.size() - 1, evaluate(decl.getInitializer()).get(0));	
					}
				}
			}
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
	 * Returns the names contained in a declaration.
	 * Eg. int a, b, * c; will return [a, b, c].
	 */
	private List<SimpleName> evaluateDeclarationReturnNames(IASTDeclaration declaration) throws DOMException
	{
		List<SimpleName> ret = new ArrayList<SimpleName>();

		if (declaration instanceof IASTSimpleDeclaration)
		{
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
			print("simple declaration");

			for (IASTDeclarator decl : simpleDeclaration.getDeclarators())
				ret.add(ast.newSimpleName(getSimpleName(decl.getName())));
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
				return  "Integer";
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
	private Type evaluateSimpleType(int type, boolean isShort, boolean isLongLong, boolean isUnsigned)
	{
		switch (type)
		{
		case IASTSimpleDeclSpecifier.t_char:
			print("char");
			return ast.newPrimitiveType(PrimitiveType.BYTE);
		case IASTSimpleDeclSpecifier.t_int:
			print("int");
			if (isShort)
				return ast.newPrimitiveType(PrimitiveType.SHORT);
			else if (isLongLong)
				return ast.newPrimitiveType(PrimitiveType.LONG);
			else
				return ast.newPrimitiveType(PrimitiveType.INT);
		case IASTSimpleDeclSpecifier.t_float:
			print("float");
			return ast.newPrimitiveType(PrimitiveType.FLOAT);
		case IASTSimpleDeclSpecifier.t_double:
			print("double");
			return ast.newPrimitiveType(PrimitiveType.DOUBLE);
		case IASTSimpleDeclSpecifier.t_unspecified:
			print("unspecified");
			if (isUnsigned)
				return ast.newPrimitiveType(PrimitiveType.INT);
			else
				return null;
		case IASTSimpleDeclSpecifier.t_void:
			print("void");
			return ast.newPrimitiveType(PrimitiveType.VOID);
		case ICPPASTSimpleDeclSpecifier.t_bool:
			print("bool");
			return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
		case ICPPASTSimpleDeclSpecifier.t_wchar_t:
			print("wchar_t");
			return ast.newPrimitiveType(PrimitiveType.CHAR);
		default:
			return null;
		}
	}

	private Expression eval1Expr(IASTExpression expr) throws DOMException
	{
		List<Expression> exprs = evalExpr(expr);
		assert(exprs.size() == 1);
		return exprs.get(0);
	}
	
	private Expression eval1Expr(IASTExpression expr, EnumSet<Flag> flags) throws DOMException
	{
		List<Expression> exprs = evalExpr(expr, flags);
		assert(exprs.size() == 1);
		return exprs.get(0);
	}
	
	private List<Expression> evalExpr(IASTExpression expression) throws DOMException
	{
		return evalExpr(expression, EnumSet.noneOf(Flag.class));
	}
	
	private Expression callCopyIfNeeded(Expression expr, IASTExpression cppExpr, EnumSet<Flag> flags) throws DOMException
	{
		TypeEnum te = getTypeEnum(cppExpr.getExpressionType());
		if (flags.contains(Flag.IS_RET_VAL) && expr instanceof ClassInstanceCreation)
		{
			return expr;
		}
		else if (flags.contains(Flag.IS_RET_VAL))
		{
			MethodInvocation method = jast.newMethod()
					.on(expr)
					.call("copy").toAST();			
			return method;
		}
		else if (te == TypeEnum.OTHER && !(expr instanceof ClassInstanceCreation))
		{
			MethodInvocation method = jast.newMethod()
					.on(expr)
					.call("copy").toAST();
			MethodInvocation addItem = createAddItemCall(method);
			return addItem;
		}
		else if (te == TypeEnum.OTHER)
		{
			MethodInvocation addItem = createAddItemCall(expr);
			return addItem;
		}
		return expr;
	}
	
	/**
	 * Given a C++ expression, attempts to convert it into one
	 * or more Java expressions.
	 */
	private List<Expression> evalExpr(IASTExpression expression, EnumSet<Flag> flags) throws DOMException
	{
		List<Expression> ret = new ArrayList<Expression>();
		boolean fNeedBooleans = flags.contains(Flag.NEED_BOOLEAN);

		if (expression instanceof IASTLiteralExpression)
		{
			IASTLiteralExpression literal = (IASTLiteralExpression) expression;

			switch (literal.getKind())
			{
			case IASTLiteralExpression.lk_char_constant:
				CharacterLiteral cl = ast.newCharacterLiteral();
				cl.setEscapedValue(new String(literal.getValue()));
				ret.add(cl);
				break;
			case IASTLiteralExpression.lk_false:
				BooleanLiteral bl = ast.newBooleanLiteral(false);
				ret.add(bl);
				break;
			case IASTLiteralExpression.lk_true:
				BooleanLiteral bl2 = ast.newBooleanLiteral(true);
				ret.add(bl2);
				break;
			case IASTLiteralExpression.lk_float_constant:
				NumberLiteral fl = ast.newNumberLiteral();
				fl.setToken(new String(literal.getValue()));
				ret.add(fl);
				break;
			case IASTLiteralExpression.lk_string_literal:
				StringLiteral sl = ast.newStringLiteral();
				sl.setLiteralValue(new String(literal.getValue()));
				ret.add(sl);
				break;
			case IASTLiteralExpression.lk_integer_constant:
				NumberLiteral il = ast.newNumberLiteral();
				il.setToken(new String(literal.getValue()));
				ret.add(il);
				break;
			case IASTLiteralExpression.lk_this:
				ThisExpression te = ast.newThisExpression();
				ret.add(te);
				break;
			}
		}
		else if (expression instanceof IASTArraySubscriptExpression)
		{
			IASTArraySubscriptExpression arraySubscriptExpression = (IASTArraySubscriptExpression)expression;
			print("Array subscript");

			ArrayAccess array = ast.newArrayAccess(); 
			array.setArray(eval1Expr(arraySubscriptExpression.getArrayExpression()));
			array.setIndex(eval1Expr(arraySubscriptExpression.getSubscriptExpression()));
			ret.add(array);
		}
		else if (expression instanceof IASTBinaryExpression)
		{
			IASTBinaryExpression binaryExpression = (IASTBinaryExpression)expression;
			print("binary expression" + binaryExpression.getRawSignature());
			boolean fSubsNeedBooleans = needBooleanExpressions(binaryExpression.getOperator());

			if (expression instanceof IASTImplicitNameOwner &&
					((IASTImplicitNameOwner) expression).getImplicitNames().length != 0 &&
					((IASTImplicitNameOwner) expression).getImplicitNames()[0].isOperator())
			{


				String name = ((IASTImplicitNameOwner) expression).getImplicitNames()[0].resolveBinding().getName();
				String replace = "";
//TODO Unknown functions.
				if (name.equals("operator =") &&
					((IASTImplicitNameOwner)expression).getImplicitNames()[0].resolveBinding() instanceof ICPPMethod)
				{
					ICPPMethod bind = (ICPPMethod) ((IASTImplicitNameOwner)expression).getImplicitNames()[0].resolveBinding();

					Assignment ass = ast.newAssignment();
					if (!(eval1Expr(binaryExpression.getOperand2()) instanceof ClassInstanceCreation))
					{
						ClassInstanceCreation create = jast.newClassCreate()
								.type(cppToJavaType(bind.getParameters()[0].getType()))
								.with(eval1Expr(binaryExpression.getOperand2())).toAST();

						ass.setRightHandSide(create);
					}
					else
					{
						ass.setRightHandSide(eval1Expr(binaryExpression.getOperand2()));
					}

					ass.setLeftHandSide(eval1Expr(binaryExpression.getOperand1()));
					ret.add(ass);
				}
				else
				{
					replace = normalizeName(name);

					MethodInvocation method = jast.newMethod()
							.on(eval1Expr(binaryExpression.getOperand1()))
							.call(replace)
							.with(eval1Expr(binaryExpression.getOperand2())).toAST();

					ret.add(method);
				}
			}
			else if (isAssignmentExpression(binaryExpression.getOperator()))
			{
				Assignment assign = jast.newAssign()
						.left(eval1Expr(binaryExpression.getOperand1()))
						.right(eval1Expr(binaryExpression.getOperand2()))
						.op(evaluateBinaryAssignmentOperator(binaryExpression.getOperator())).toAST();

				ret.add(assign);
			}
			else
			{
				InfixExpression infix = jast.newInfix()
						.left(eval1Expr(binaryExpression.getOperand1(), fSubsNeedBooleans ? EnumSet.of(Flag.NEED_BOOLEAN) : EnumSet.noneOf(Flag.class)))
						.right(eval1Expr(binaryExpression.getOperand2(), fSubsNeedBooleans ? EnumSet.of(Flag.NEED_BOOLEAN) : EnumSet.noneOf(Flag.class)))
						.op(evaluateBinaryOperator(binaryExpression.getOperator())).toAST();

				ret.add(infix);
			}
		}
		else if (expression instanceof IASTCastExpression)
		{
			IASTCastExpression castExpression = (IASTCastExpression)expression;
			print("cast");

			CastExpression cast = ast.newCastExpression();
			cast.setExpression(eval1Expr(castExpression.getOperand()));
			cast.setType(evalTypeId(castExpression.getTypeId()));
			ret.add(cast);
		}
		else if (expression instanceof IASTConditionalExpression)
		{
			IASTConditionalExpression conditionalExpression = (IASTConditionalExpression)expression;
			print("conditional");

			ConditionalExpression conditional = ast.newConditionalExpression();
			conditional.setExpression(eval1Expr(conditionalExpression.getLogicalConditionExpression()));
			conditional.setThenExpression(eval1Expr(conditionalExpression.getPositiveResultExpression()));
			conditional.setElseExpression(eval1Expr(conditionalExpression.getNegativeResultExpression()));
			ret.add(conditional);
		}

		else if (expression instanceof IASTFieldReference)
		{
			IASTFieldReference fieldReference = (IASTFieldReference)expression;
			print("field reference");

			IBinding binding = fieldReference.getFieldName().resolveBinding();
			boolean isBitField = false;

			print(binding.getName());
			
			FieldAccess field = jast.newField()
					.on(eval1Expr(fieldReference.getFieldOwner()))
					.field(getSimpleName(fieldReference.getFieldName())).toAST();

			if (binding instanceof IEnumerator)
			{
				FieldAccess access = jast.newField()
						.on(field)
						.field("val").toAST();

				ret.add(access);
			}
			else
			{
				ret.add(field);
			}
		}
		else if (expression instanceof IASTFunctionCallExpression)
		{
			IASTFunctionCallExpression functionCallExpression = (IASTFunctionCallExpression)expression;
			print("function call");
			Expression funcCallExpr;
			
			if (functionCallExpression.getFunctionNameExpression() instanceof IASTIdExpression &&
				((IASTIdExpression) functionCallExpression.getFunctionNameExpression()).getName().resolveBinding() instanceof ICPPClassType)
			{
				ICPPClassType con = (ICPPClassType) ((IASTIdExpression) functionCallExpression.getFunctionNameExpression()).getName().resolveBinding();

				ClassInstanceCreation create = ast.newClassInstanceCreation();
				create.setType(jast.newType(con.getName()));
				
				funcCallExpr = (create);
				
				if (functionCallExpression.getParameterExpression() instanceof IASTExpressionList)
				{
					IASTExpressionList list = (IASTExpressionList) functionCallExpression.getParameterExpression();
					for (IASTExpression arg : list.getExpressions())
					{
						Expression exarg = eval1Expr(arg, EnumSet.of(Flag.IN_METHOD_ARGS));
						exarg = callCopyIfNeeded(exarg, arg, flags);
						create.arguments().add(exarg);
					}
				}
				else if (functionCallExpression.getParameterExpression() instanceof IASTExpression)
				{
					Expression arg = eval1Expr((IASTExpression) functionCallExpression.getParameterExpression(), EnumSet.of(Flag.IN_METHOD_ARGS));
					arg = callCopyIfNeeded(arg, (IASTExpression) functionCallExpression.getParameterExpression(), flags);
					create.arguments().add(arg);
				}
				
				if ((getTypeEnum(expression.getExpressionType()) == TypeEnum.OTHER) &&
					!(flags.contains(Flag.IS_RET_VAL)) &&
					!(flags.contains(Flag.IN_METHOD_ARGS)))
				{
					MethodInvocation method2 = createAddItemCall(funcCallExpr); 
					ret.add(method2);
				}
				else
					ret.add(funcCallExpr);
			}
			else
			{
				JASTHelper.Method method = jast.newMethod();
				if (functionCallExpression.getFunctionNameExpression() instanceof IASTFieldReference)
				{
					IASTFieldReference fr = (IASTFieldReference) functionCallExpression.getFunctionNameExpression();

					method.on(eval1Expr(fr.getFieldOwner()))
						.call(getSimpleName(fr.getFieldName()));
				}
				else if (functionCallExpression.getFunctionNameExpression() instanceof IASTIdExpression)
				{
					IASTIdExpression id = (IASTIdExpression) functionCallExpression.getFunctionNameExpression();

					if (getSimpleName(id.getName()).equals("max") || getSimpleName(id.getName()).equals("min"))
					{
						method.on("Math");
					}

					method.call(getSimpleName(id.getName()));
				}

				if (functionCallExpression.getParameterExpression() instanceof IASTExpressionList)
				{
					IASTExpressionList list = (IASTExpressionList) functionCallExpression.getParameterExpression();
					for (IASTExpression arg : list.getExpressions())
					{
						Expression exarg = eval1Expr(arg, EnumSet.of(Flag.IN_METHOD_ARGS));
						exarg = callCopyIfNeeded(exarg, arg, flags);
						method.with(exarg);
					}
				}
				else if (functionCallExpression.getParameterExpression() instanceof IASTExpression)
				{
					Expression exarg = eval1Expr(functionCallExpression.getParameterExpression(), EnumSet.of(Flag.IN_METHOD_ARGS));
					exarg = callCopyIfNeeded(exarg, functionCallExpression.getParameterExpression(), flags);
					method.with(exarg);
				}
				funcCallExpr = (method.toAST());

				if ((getTypeEnum(expression.getExpressionType()) == TypeEnum.OTHER) &&
					!(flags.contains(Flag.IS_RET_VAL)) &&
					!(flags.contains(Flag.IN_METHOD_ARGS)))
				{
					MethodInvocation method2 = createAddItemCall(funcCallExpr); 
					ret.add(method2);
				}
				else
					ret.add(funcCallExpr);
			}
		}
		else if (expression instanceof IASTIdExpression)
		{
			IASTIdExpression idExpression = (IASTIdExpression)expression;
			print("id expression");

			if (isBitfield(idExpression.getName()))
			{
				print("Got bitifield");
				MethodInvocation methodGet = jast.newMethod().call("get__" + getSimpleName(idExpression.getName())).toAST();
				ret.add(methodGet);
			}
			else
			{
				IBinding bind = idExpression.getName().resolveBinding();
				
				if (bind instanceof IEnumerator)
				{
					IEnumerator enumerator = (IEnumerator) bind;
					String enumeration = getSimpleType(((IEnumeration)enumerator.getType()).getName());
					QualifiedName qual;
					if (enumeration.equals("MISSING"))
					{
						String first = ((IEnumeration)enumerator.getOwner()).getEnumerators()[0].getName();
						String enumName = m_anonEnumMap.get(first);
						
						if (enumName == null)
							exitOnError();
						
						qual = ast.newQualifiedName(ast.newSimpleName(enumName), ast.newSimpleName(getSimpleName(idExpression.getName())));
					}
					else
					{
						qual = ast.newQualifiedName(ast.newSimpleName(enumeration), ast.newSimpleName(getSimpleName(idExpression.getName())));
					}
					FieldAccess fa = jast.newField()
							.on(qual)
							.field("val").toAST();

					ret.add(fa);
				}
				else if (isEventualPtrOrRef(idExpression.getExpressionType()))
				{
					FieldAccess fa = jast.newField()
							.on(getSimpleName(idExpression.getName()))
							.field("val").toAST();

					ret.add(fa);
				}
				else
				{
					SimpleName nm = ast.newSimpleName(getSimpleName(idExpression.getName()));
					ret.add(nm);
				}
			}
		}
		else if (expression instanceof IASTTypeIdExpression)
		{
			IASTTypeIdExpression typeIdExpression = (IASTTypeIdExpression)expression;
			print("type id");
			printerr(typeIdExpression.getRawSignature());

			//TODO
			FieldAccess fa = ast.newFieldAccess();
			fa.setName(ast.newSimpleName("sizeof"));
			ret.add(fa);
			evalTypeId(typeIdExpression.getTypeId());
		}
		else if (expression instanceof IASTUnaryExpression)
		{
			IASTUnaryExpression unaryExpression = (IASTUnaryExpression)expression;
			print("unary");

			if (isPostfixExpression(unaryExpression.getOperator()))
			{
				PostfixExpression post = ast.newPostfixExpression();
				post.setOperator(evalUnaryPostfixOperator(unaryExpression.getOperator()));
				post.setOperand(eval1Expr(unaryExpression.getOperand()));
				ret.add(post);
			}
			else if (isPrefixExpression(unaryExpression.getOperator()))
			{
				PrefixExpression pre = ast.newPrefixExpression();
				pre.setOperator(evalUnaryPrefixOperator(unaryExpression.getOperator()));
				pre.setOperand(eval1Expr(unaryExpression.getOperand()));
				ret.add(pre);
			}
			else if (unaryExpression.getOperator() == IASTUnaryExpression.op_bracketedPrimary)
			{
				ParenthesizedExpression paren = jast.newParen(eval1Expr(unaryExpression.getOperand()));
				ret.add(paren);
			}
			else if (unaryExpression.getOperator() == IASTUnaryExpression.op_star)
			{
				ret.addAll(evalExpr(unaryExpression.getOperand()));
			}
			else if (unaryExpression.getOperator() == IASTUnaryExpression.op_amper)
			{
				ret.addAll(evalExpr(unaryExpression.getOperand()));
			}
			else
			{
				// TODO
				print("todo");
				print("" + unaryExpression.getOperator());
				ret.add(ast.newStringLiteral());

			}
		}
		else if (expression instanceof ICASTTypeIdInitializerExpression)
		{
			ICASTTypeIdInitializerExpression typeIdInitializerExpression = (ICASTTypeIdInitializerExpression)expression;
			print("type id initializer");

			evalTypeId(typeIdInitializerExpression.getTypeId());
			evaluate(typeIdInitializerExpression.getInitializer());
		}
		else if (expression instanceof ICPPASTDeleteExpression)
		{
			ICPPASTDeleteExpression deleteExpression = (ICPPASTDeleteExpression)expression;
			print("delete");

			if (!deleteExpression.isVectored())
			{
				// Call ptr.destruct()...				
				ret.add(jast.newMethod()
						.on(eval1Expr(deleteExpression.getOperand()), true)
						.call("destruct").toAST());
			}
			else
			{
				// Call DestructHelper.destructArray(array)...
				ret.add(jast.newMethod()
						.on("DestructHelper")
						.call("destructArray")
						.with(eval1Expr(deleteExpression.getOperand())).toAST());
			}
		}
		else if (expression instanceof ICPPASTNewExpression)
		{
			ICPPASTNewExpression newExpression = (ICPPASTNewExpression)expression;
			print("new");

			if (!newExpression.isArrayAllocation())
			{
				boolean isBasic = false;
				if (getTypeEnum(newExpression.getExpressionType()) == TypeEnum.POINTER)
				{
					TypeEnum teBase = getTypeEnum(getPointerBaseType(newExpression.getExpressionType()));
					
					if (teBase == TypeEnum.CHAR || teBase == TypeEnum.NUMBER || teBase == TypeEnum.BOOLEAN)
					{
						ret.add(ast.newNumberLiteral("0"));
						isBasic = true;
					}
				}
				if (!isBasic)
				{
					ClassInstanceCreation create = ast.newClassInstanceCreation();
					//create.setExpression(evaluate(newExpression.getPlacement()))
					create.setType(evalTypeId(newExpression.getTypeId()));

					if (newExpression.getNewInitializer() instanceof IASTExpressionList)
					{
						for (IASTExpression arg : ((IASTExpressionList) newExpression.getNewInitializer()).getExpressions())
							create.arguments().addAll(evalExpr(arg));
					}
					else if (newExpression.getNewInitializer() instanceof IASTExpression)
					{
						create.arguments().addAll(evalExpr((IASTExpression) newExpression.getNewInitializer()));
					}

					ret.add(create);
				}
			}
			else
			{
				List<Expression> sizeExprs = new ArrayList<Expression>();

				for (IASTExpression arraySize : newExpression.getNewTypeIdArrayExpressions())
					sizeExprs.add(eval1Expr(arraySize));

				Expression ex = generateArrayCreationExpression(newExpression.getExpressionType(), sizeExprs);
				ret.add(ex);
			}
		}
		else if (expression instanceof ICPPASTSimpleTypeConstructorExpression)
		{
			ICPPASTSimpleTypeConstructorExpression simpleTypeConstructorExpression = (ICPPASTSimpleTypeConstructorExpression)expression;
			printerr("simple type constructor");

			evalExpr(simpleTypeConstructorExpression.getInitialValue());
		}
		else if (expression instanceof IGNUASTCompoundStatementExpression)
		{
			IGNUASTCompoundStatementExpression compoundStatementExpression = (IGNUASTCompoundStatementExpression)expression;
			print("GNU Compound statement");

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
			ret.add(null);
		}

		if (ret.isEmpty())
			printerr(expression.getClass().getCanonicalName());
		
		if (fNeedBooleans)
			ret.set(0, makeExpressionBoolean(ret.get(0), expression));

		if (expression != null)
			print(expression.getClass().getCanonicalName());

		if (ret.isEmpty())
			ret.add(ast.newNumberLiteral("0")); // FIXME

		return ret;
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
	private PostfixExpression.Operator evalUnaryPostfixOperator(int operator) 
	{
		switch(operator)
		{
		case IASTUnaryExpression.op_postFixDecr:
			return PostfixExpression.Operator.DECREMENT;
		case IASTUnaryExpression.op_postFixIncr:
			return PostfixExpression.Operator.INCREMENT;
		default:
			assert(false);
			return null;
		}
	}

	/**
	 * Converts a C++ prefix operator to a C++ one.
	 */
	private PrefixExpression.Operator evalUnaryPrefixOperator(int operator) 
	{
		switch(operator)
		{
		case IASTUnaryExpression.op_prefixDecr:
			return PrefixExpression.Operator.DECREMENT;
		case IASTUnaryExpression.op_prefixIncr:
			return PrefixExpression.Operator.INCREMENT;
		case IASTUnaryExpression.op_not:
			return PrefixExpression.Operator.NOT;
		case IASTUnaryExpression.op_plus:
			return PrefixExpression.Operator.PLUS;
		case IASTUnaryExpression.op_minus:
			return PrefixExpression.Operator.MINUS;
		case IASTUnaryExpression.op_tilde:
			return PrefixExpression.Operator.COMPLEMENT;
		default:
			assert(false);
			return null;
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
	private Assignment.Operator evaluateBinaryAssignmentOperator(int operator) 
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_assign:
			return Assignment.Operator.ASSIGN;
		case IASTBinaryExpression.op_binaryAndAssign:
			return Assignment.Operator.BIT_AND_ASSIGN;
		case IASTBinaryExpression.op_binaryOrAssign:
			return Assignment.Operator.BIT_OR_ASSIGN;
		case IASTBinaryExpression.op_binaryXorAssign:
			return Assignment.Operator.BIT_XOR_ASSIGN;
		case IASTBinaryExpression.op_divideAssign:
			return Assignment.Operator.DIVIDE_ASSIGN;
		case IASTBinaryExpression.op_plusAssign:
			return Assignment.Operator.PLUS_ASSIGN;
		case IASTBinaryExpression.op_minusAssign:
			return Assignment.Operator.MINUS_ASSIGN;
		case IASTBinaryExpression.op_multiplyAssign:
			return Assignment.Operator.TIMES_ASSIGN;
		case IASTBinaryExpression.op_shiftLeftAssign:
			return Assignment.Operator.LEFT_SHIFT_ASSIGN;
		case IASTBinaryExpression.op_shiftRightAssign:
			return Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN; // TODO
		default:
			return Assignment.Operator.ASSIGN; // TODO
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

	
	/**
	 * Converts the CDT binary operator to a JDT binary operator.
	 */
	private Operator evaluateBinaryOperator(int operator)
	{
		switch (operator)
		{
		case IASTBinaryExpression.op_binaryAnd:
			return InfixExpression.Operator.AND;
		case IASTBinaryExpression.op_binaryOr:
			return InfixExpression.Operator.OR;
		case IASTBinaryExpression.op_binaryXor:
			return InfixExpression.Operator.XOR;
		case IASTBinaryExpression.op_divide:
			return InfixExpression.Operator.DIVIDE;
		case IASTBinaryExpression.op_equals:
			return InfixExpression.Operator.EQUALS;
		case IASTBinaryExpression.op_plus:
			return InfixExpression.Operator.PLUS;
		case IASTBinaryExpression.op_minus:
			return InfixExpression.Operator.MINUS;
		case IASTBinaryExpression.op_multiply:
			return InfixExpression.Operator.TIMES;
		case IASTBinaryExpression.op_notequals:
			return InfixExpression.Operator.NOT_EQUALS;
		case IASTBinaryExpression.op_greaterEqual:
			return InfixExpression.Operator.GREATER_EQUALS;
		case IASTBinaryExpression.op_greaterThan:
			return InfixExpression.Operator.GREATER;
		case IASTBinaryExpression.op_lessEqual:
			return InfixExpression.Operator.LESS_EQUALS;
		case IASTBinaryExpression.op_lessThan:
			return InfixExpression.Operator.LESS;
		case IASTBinaryExpression.op_logicalAnd:
			return InfixExpression.Operator.CONDITIONAL_AND;
		case IASTBinaryExpression.op_logicalOr:
			return InfixExpression.Operator.CONDITIONAL_OR;
		case IASTBinaryExpression.op_modulo:
			return InfixExpression.Operator.REMAINDER;
		case IASTBinaryExpression.op_shiftLeft:
			return InfixExpression.Operator.LEFT_SHIFT;
		case IASTBinaryExpression.op_shiftRight:
			return InfixExpression.Operator.RIGHT_SHIFT_SIGNED; // TODO
		default:
			return InfixExpression.Operator.TIMES; // TODO
		}
	}

	/**
	 * Given a C++ initializer, returns a list of Java expressions.
	 * 
	 * @param initializer Initializer
	 */
	private List<Expression> evaluate(IASTInitializer initializer) throws DOMException
	{
		List<Expression> ret = new ArrayList<Expression>();

		if (initializer instanceof IASTEqualsInitializer)
		{
			IASTEqualsInitializer equals = (IASTEqualsInitializer) initializer;
			print("equals initializer");

			if (equals.getInitializerClause() instanceof IASTExpression)
				ret.addAll(evalExpr((IASTExpression) equals.getInitializerClause()));
			else if (equals.getInitializerClause() instanceof IASTInitializerList)
			{
				// Don't yet support this
				printerr("equals intializer list");
				//exitOnError();
			}
		}
		else if (initializer instanceof IASTInitializerList)
		{
			IASTInitializerList initializerList = (IASTInitializerList)initializer;
			print("initializer list");

			for (IASTInitializer childInitializer : initializerList.getInitializers())
				ret.addAll(evaluate(childInitializer));
		}
		else if (initializer instanceof ICPPASTConstructorInitializer)
		{
			ICPPASTConstructorInitializer constructorInitializer = (ICPPASTConstructorInitializer)initializer;
			print("constructor initializer");

			ret.addAll(evalExpr(constructorInitializer.getExpression()));
		}
		else if (initializer != null)
		{
			printerr("Unsupported initializer type: " + initializer.getClass().getCanonicalName());
			exitOnError();
		}
		return ret;
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

	/**
	 * Given a for-statement initializer statement, returns a
	 * Java list of expressions. 
	 */
	private List<Expression> evaluateForInitializer(IASTStatement stmt) throws DOMException
	{
		if (stmt instanceof IASTExpressionStatement)
		{
			IASTExpressionStatement expressionStatement = (IASTExpressionStatement) stmt;
			print("Expression");

			return evalExpr(expressionStatement.getExpression());
		}
		else if (stmt instanceof IASTDeclarationStatement)
		{
			IASTDeclarationStatement decl = (IASTDeclarationStatement) stmt;
			print("declaration");

			List<SimpleName> list = evaluateDeclarationReturnNames(decl.getDeclaration());
			List<Expression> inits = evaluateDeclarationReturnInitializers(decl.getDeclaration(), true);
			List<Type> types = evaluateDeclarationReturnTypes(decl.getDeclaration());

			List<Expression> ret = new ArrayList<Expression>();

			for (int i = 0; i < list.size(); i++)
			{
				VariableDeclarationFragment fr = ast.newVariableDeclarationFragment();
				fr.setName(list.get(i));
				fr.setInitializer(inits.get(i));
				VariableDeclarationExpression expr = ast.newVariableDeclarationExpression(fr);
				expr.setType(types.get(i));
				ret.add(expr);
			}

			return ret;
		}
		else if (stmt instanceof IASTNullStatement)
		{
			return null;
			//
//			List<Expression> ret = new ArrayList<Expression>();			
//				ret.add(ast.newBooleanLiteral(true)); // FIXME.
//				return ret;
		}
		else if (stmt != null)
		{
			printerr("Another kind of intializer:" + stmt.getClass().getCanonicalName());
			exitOnError();
		}
		return null;
	}
	
	/**
	 * Creates a list of VariableDeclarationFragments. Each fragment includes
	 * a name and optionally an initializer (but not a type).
	 */
	private List<VariableDeclarationFragment> getDeclarationFragments(IASTDeclaration decl) throws DOMException
	{
		List<SimpleName> names = evaluateDeclarationReturnNames(decl);
		List<VariableDeclarationFragment> frags = new ArrayList<VariableDeclarationFragment>();
		List<Expression> exprs = evaluateDeclarationReturnInitializers(decl, true);

		int j = 0;
		for (SimpleName nm : names)
		{
			VariableDeclarationFragment fr = ast.newVariableDeclarationFragment();
			print(nm.toString());
			fr.setName(nm);
			fr.setInitializer(exprs.get(j));
			frags.add(fr);
			j++;
		}

		return frags;
	}

	private MethodInvocation createCleanupCall(int until)
	{
		MethodInvocation meth = jast.newMethod()
				.on("StackHelper")
				.call("cleanup")
				.with(ast.newNullLiteral())
				.with("__stack")
				.with(until).toAST();		
		return meth;
	}
	
	private Statement eval1Stmt(IASTStatement stmt) throws DOMException
	{
		List<Statement> ret = evalStmt(stmt, EnumSet.noneOf(Flag.class));
		assert(ret.size() == 1);
		return ret.get(0);
	}

	private Statement eval1Stmt(IASTStatement stmt, EnumSet<Flag> flags) throws DOMException
	{
		List<Statement> ret = evalStmt(stmt, flags);
		assert(ret.size() == 1);
		return ret.get(0);
	}
	
	
	private Expression generateDeclarationForWhileIf(IASTDeclaration declaration, List<Statement> ret) throws DOMException
	{
		List<VariableDeclarationFragment> frags = getDeclarationFragments(declaration);
		frags.get(0).setInitializer(null);
		VariableDeclarationStatement decl = ast.newVariableDeclarationStatement(frags.get(0));

		Type jType = evaluateDeclarationReturnTypes(declaration).get(0);
		decl.setType(jType);
		ret.add(decl);

		List<Expression> exprs = evaluateDeclarationReturnInitializers(declaration, true);

		Assignment assign = jast.newAssign()
				.left(ast.newSimpleName(frags.get(0).getName().getIdentifier()))
				.right(exprs.get(0))
				.op(Assignment.Operator.ASSIGN).toAST();

		IASTExpression expr = evalDeclarationReturnFirstInitializerExpression(declaration);
		Expression finalExpr = makeExpressionBoolean(assign, expr);
		return finalExpr;
	}
	
	private boolean isTerminatingStatement(Statement stmt)
	{
		if (stmt instanceof BreakStatement ||
			stmt instanceof ReturnStatement ||
			stmt instanceof ContinueStatement)
			return true;
		
		if (stmt instanceof Block)
		{
			Block blk = (Block) stmt;

			if (!blk.statements().isEmpty() &&
				isTerminatingStatement((Statement) blk.statements().get(blk.statements().size() - 1)))
				return true;
		}
		
		return false;
	}
	
	private void startNewCompoundStmt(EnumSet<Flag> flags)
	{
		m_localVariableStack.push(
				new ScopeVar(m_nextVariableId,
						flags.contains(Flag.IS_LOOP),
						flags.contains(Flag.IS_SWITCH)));
	}
	
	private void endCompoundStmt()
	{
		m_localVariableStack.pop();
	}
	
	private Block surround(Statement stmt)
	{
		if (!(stmt instanceof Block))
		{
			Block blk = ast.newBlock();
			blk.statements().add(stmt);
			return blk;
		}
		
		return (Block) stmt;
	}

	private List<Statement> evalStmt(IASTStatement statement) throws DOMException
	{
		return evalStmt(statement, EnumSet.noneOf(Flag.class));
	}
	
	/**
	 * Attempts to convert the given C++ statement to one or more Java statements.
	 */
	private List<Statement> evalStmt(IASTStatement statement, EnumSet<Flag> flags) throws DOMException
	{
		List<Statement> ret = new ArrayList<Statement>();

		if (statement instanceof IASTBreakStatement)
		{
			// IASTBreakStatement breakStatement = (IASTBreakStatement) statement;
			print("break");

			int temp = findLastSwitchOrLoopId();

			if (temp != -1)
			{
				// Cleanup back to the closest loop...
				Block blk = ast.newBlock();
				blk.statements().add(ast.newExpressionStatement(createCleanupCall(temp)));
				blk.statements().add(ast.newBreakStatement());
				ret.add(blk);
			}
			else
				ret.add(ast.newBreakStatement());
		}
		else if (statement instanceof IASTCaseStatement)
		{
			IASTCaseStatement caseStatement = (IASTCaseStatement) statement;
			print("case");
			SwitchCase cs = ast.newSwitchCase();
			cs.setExpression(eval1Expr(caseStatement.getExpression()));
			ret.add(cs);
		}
		else if (statement instanceof IASTContinueStatement)
		{
			// IASTContinueStatement contStatement = (IASTContinueStatement) statement;
			print("continue");
			int temp = findLastLoopId();

			if (temp != -1)
			{
				// Cleanup back to the closest loop...
				Block blk = ast.newBlock();
				blk.statements().add(ast.newExpressionStatement(createCleanupCall(temp)));
				blk.statements().add(ast.newContinueStatement());
				ret.add(blk);
			}
			else
				ret.add(ast.newContinueStatement());
		}
		else if (statement instanceof IASTDefaultStatement)
		{
			// IASTDefaultStatement defStatement = (IASTDefaultStatement) statement;
			print("default");
			SwitchCase cs = ast.newSwitchCase();
			cs.setExpression(null);
			ret.add(cs);
		}
		else if (statement instanceof IASTGotoStatement)
		{
			IASTGotoStatement gotoStatement = (IASTGotoStatement) statement;
			print("goto");
			getSimpleName(gotoStatement.getName());
			// TODO
		}
		else if (statement instanceof IASTNullStatement)
		{
			// IASTNullStatement nullStatement = (IASTNullStatement) statement;
			print("Empty statement");
			ret.add(ast.newEmptyStatement());
		}
		else if (statement instanceof IASTProblemStatement)
		{
			IASTProblemStatement probStatement = (IASTProblemStatement) statement;
			print("problem: " + probStatement.getProblem().getMessageWithLocation());
			// TODO
		}
		else if (statement instanceof IASTCompoundStatement)
		{
			IASTCompoundStatement compoundStatement = (IASTCompoundStatement)statement;
			print("Compound");

			Block block = ast.newBlock();
			startNewCompoundStmt(flags);
			
			for (IASTStatement childStatement : compoundStatement.getStatements())
				block.statements().addAll(evalStmt(childStatement));

			int cnt = m_localVariableStack.peek().cnt;
			m_nextVariableId = m_localVariableStack.peek().id;
			endCompoundStmt();
			
			if (cnt != 0 &&
				!block.statements().isEmpty() && 
				!isTerminatingStatement((Statement) block.statements().get(block.statements().size() - 1)))
			{
				block.statements().add(ast.newExpressionStatement(createCleanupCall(m_nextVariableId)));
			}

			ret.add(block);
		}
		else if (statement instanceof IASTDeclarationStatement)
		{
			IASTDeclarationStatement declarationStatement = (IASTDeclarationStatement)statement;
			print("Declaration");
			List<VariableDeclarationFragment> frags = getDeclarationFragments(declarationStatement.getDeclaration());
			List<Type> types = evaluateDeclarationReturnTypes(declarationStatement.getDeclaration());

			for (int i = 0; i < types.size(); i++)
			{
				VariableDeclarationStatement decl = ast.newVariableDeclarationStatement(frags.get(i));				
				decl.setType(types.get(i));
				ret.add(decl);
			}
		}
		else if (statement instanceof IASTDoStatement)
		{
			IASTDoStatement doStatement = (IASTDoStatement)statement;
			print("Do");

			DoStatement dos = ast.newDoStatement();
			dos.setBody(surround(eval1Stmt(doStatement.getBody(), EnumSet.of(Flag.IS_LOOP))));
			dos.setExpression(eval1Expr(doStatement.getCondition(), EnumSet.of(Flag.NEED_BOOLEAN)));
			ret.add(dos);
		}
		else if (statement instanceof IASTExpressionStatement)
		{
			IASTExpressionStatement expressionStatement = (IASTExpressionStatement)statement;
			print("Expression");

			ret.add(ast.newExpressionStatement(eval1Expr(expressionStatement.getExpression())));
		}
		else if (statement instanceof IASTForStatement)
		{
			IASTForStatement forStatement = (IASTForStatement)statement;
			print("For");

			if (forStatement instanceof ICPPASTForStatement)
				;//evaluate(((ICPPASTForStatement)forStatement).getConditionDeclaration());

			ForStatement fs = ast.newForStatement();
			List<Expression> inits = evaluateForInitializer(forStatement.getInitializerStatement());
			Expression expr = eval1Expr(forStatement.getConditionExpression(), EnumSet.of(Flag.NEED_BOOLEAN));
			List<Expression> updaters = evalExpr(forStatement.getIterationExpression());

			if (inits != null)
				fs.initializers().addAll(inits);
			if (expr != null)
				fs.setExpression(expr);
			if (updaters.get(0) != null)
				fs.updaters().addAll(updaters);

			fs.setBody(surround(eval1Stmt(forStatement.getBody(), EnumSet.of(Flag.IS_LOOP))));
			ret.add(fs);
		}
		else if (statement instanceof IASTIfStatement)
		{
			IASTIfStatement ifStatement = (IASTIfStatement)statement;
			print("If");

			IfStatement ifs = ast.newIfStatement();

			if (ifStatement instanceof ICPPASTIfStatement &&
				((ICPPASTIfStatement) ifStatement).getConditionDeclaration() != null)
			{
				ifs.setExpression(generateDeclarationForWhileIf(((ICPPASTIfStatement) ifStatement).getConditionDeclaration(), ret));
			}
			else
			{
				ifs.setExpression(eval1Expr(ifStatement.getConditionExpression(), EnumSet.of(Flag.NEED_BOOLEAN)));
			}

			ifs.setThenStatement(surround(eval1Stmt(ifStatement.getThenClause())));
			List<Statement> elseStmts = evalStmt(ifStatement.getElseClause());
			ifs.setElseStatement(!elseStmts.isEmpty() ? surround(elseStmts.get(0)) : null);
			ret.add(ifs);
		}
		else if (statement instanceof IASTLabelStatement)
		{
			IASTLabelStatement labelStatement = (IASTLabelStatement)statement;
			print("Label");
			evalStmt(labelStatement.getNestedStatement());
		}
		else if (statement instanceof IASTReturnStatement)
		{
			IASTReturnStatement returnStatement = (IASTReturnStatement)statement;
			print("return");

			JASTHelper.Method method = jast.newMethod()
						.on("StackHelper")
						.call("cleanup");
			ReturnStatement ret2 = ast.newReturnStatement();
			
			if (returnStatement.getReturnValue() != null)
			{
				if (((returnStatement.getReturnValue().getExpressionType() instanceof ICompositeType ||
					(returnStatement.getReturnValue().getExpressionType() instanceof IQualifierType &&
					((IQualifierType)returnStatement.getReturnValue().getExpressionType()).getType() instanceof ICompositeType)))) 
					/* !(eval1Expr(returnStatement.getReturnValue()) instanceof ClassInstanceCreation))) */
				{
					// Call copy method on returned value...
					Expression create = eval1Expr(returnStatement.getReturnValue(), EnumSet.of(Flag.IS_RET_VAL));
					
					if (!(create instanceof ClassInstanceCreation))
					{
						create = jast.newMethod()
							.on(eval1Expr(returnStatement.getReturnValue()))
							.call("copy").toAST();
					}

					if (m_nextVariableId != 0)
						method.with(create);
					else
						ret2.setExpression(create);
				}
				else
				{
					if (m_nextVariableId != 0)
						method.with(eval1Expr(returnStatement.getReturnValue(), EnumSet.of(Flag.IS_RET_VAL)));
					else
						ret2.setExpression(eval1Expr(returnStatement.getReturnValue(), EnumSet.of(Flag.IS_RET_VAL)));
				}	

				if (m_nextVariableId != 0)
				{
					method.with("__stack").with(0);
					ret2.setExpression(method.toAST());
				}
				ret.add(ret2);
			}
			else
			{
				if (m_nextVariableId != 0)
				{
					Block blk = ast.newBlock();
					method.with(ast.newNullLiteral())
						.with("__stack")
						.with(0);
					blk.statements().add(ast.newExpressionStatement(method.toAST()));
					blk.statements().add(ret2);
					ret.add(blk);
				}
				else
					ret.add(ret2);
			}
		}
		else if (statement instanceof IASTSwitchStatement)
		{
			IASTSwitchStatement switchStatement = (IASTSwitchStatement)statement;
			print("Switch");

			SwitchStatement swt = ast.newSwitchStatement();
			
			if (switchStatement instanceof ICPPASTSwitchStatement &&
				((ICPPASTSwitchStatement) switchStatement).getControllerDeclaration() != null)
			{
				ICPPASTSwitchStatement cppSwitch = (ICPPASTSwitchStatement) switchStatement;

				List<VariableDeclarationFragment> frags = getDeclarationFragments(cppSwitch.getControllerDeclaration());
				frags.get(0).setInitializer(null);
				VariableDeclarationStatement decl = ast.newVariableDeclarationStatement(frags.get(0));

				Type jType = evaluateDeclarationReturnTypes(cppSwitch.getControllerDeclaration()).get(0);
				decl.setType(jType);
				ret.add(decl);

				List<Expression> exprs = evaluateDeclarationReturnInitializers(cppSwitch.getControllerDeclaration(), true);
				
				Assignment assign = jast.newAssign()
						.left(ast.newSimpleName(frags.get(0).getName().getIdentifier()))
						.right(exprs.get(0))
						.op(Assignment.Operator.ASSIGN).toAST();

				swt.setExpression(assign);
			}
			else
			{
				swt.setExpression(evalExpr(switchStatement.getControllerExpression()).get(0));
			}

			if (switchStatement.getBody() instanceof IASTCompoundStatement)
			{
				IASTCompoundStatement compound = (IASTCompoundStatement) switchStatement.getBody();

				startNewCompoundStmt(EnumSet.of(Flag.IS_SWITCH));
				
				for (IASTStatement stmt : compound.getStatements())
					swt.statements().addAll(evalStmt(stmt));
				
				endCompoundStmt();
			}
			else
			{
				swt.statements().addAll(evalStmt(switchStatement.getBody(), EnumSet.of(Flag.IS_SWITCH)));
			}
			
			ret.add(swt);
		}
		else if (statement instanceof IASTWhileStatement)
		{
			IASTWhileStatement whileStatement = (IASTWhileStatement)statement;
			print("while");

			WhileStatement whs = ast.newWhileStatement();

			if (whileStatement instanceof ICPPASTWhileStatement &&
				((ICPPASTWhileStatement) whileStatement).getConditionDeclaration() != null)
			{
				whs.setExpression(generateDeclarationForWhileIf(((ICPPASTWhileStatement)whileStatement).getConditionDeclaration(), ret));
			}
			else
			{
				whs.setExpression(eval1Expr(whileStatement.getCondition(), EnumSet.of(Flag.NEED_BOOLEAN)));
			}

			whs.setBody(surround(eval1Stmt(whileStatement.getBody(), EnumSet.of(Flag.IS_LOOP))));
			ret.add(whs);
		}

		else if (statement instanceof ICPPASTTryBlockStatement)
		{
			ICPPASTTryBlockStatement tryBlockStatement = (ICPPASTTryBlockStatement)statement;
			print("Try");

			TryStatement trys = ast.newTryStatement();

			trys.setBody(surround(eval1Stmt(tryBlockStatement.getTryBody())));

			for (ICPPASTCatchHandler catchHandler : tryBlockStatement.getCatchHandlers())
				trys.catchClauses().add(evaluateCatchClause(catchHandler));

			ret.add(trys);
		}
		else if (statement != null)
		{
			printerr(statement.getClass().getCanonicalName());
		}
		return ret;
	}

	
	/**
	 * Attempts to make a Java boolean expression. Eg. Adds != null, != 0, etc.
	 */
	private Expression makeExpressionBoolean(Expression exp, IASTExpression expcpp) throws DOMException
	{
		TypeEnum expType = expressionGetType(expcpp);

		if (expType != TypeEnum.BOOLEAN)
		{
			// We enclose the non-boolean expression in brackets to be safe...
			ParenthesizedExpression paren = jast.newParen(exp);

			InfixExpression infix = jast.newInfix()
					.left(paren)
					.op(InfixExpression.Operator.NOT_EQUALS).toAST();

			if (expType == TypeEnum.OTHER ||
				expType == TypeEnum.POINTER ||
				expType == TypeEnum.REFERENCE)
			{
				infix.setRightOperand(ast.newNullLiteral());
			}
			else if (expType == TypeEnum.CHAR)
			{
				CharacterLiteral c = ast.newCharacterLiteral();
				c.setCharValue('\0');
				infix.setRightOperand(c);
			}
			else if (expType == TypeEnum.NUMBER)
			{
				infix.setRightOperand(jast.newNumber(0));
			}
			return infix;
		}
		return exp;
	}

	/**
	 * Attempts to convert a C++ catch clause to Java.
	 */
	private CatchClause evaluateCatchClause(IASTStatement statement) throws DOMException
	{
		if (statement instanceof ICPPASTCatchHandler)
		{
			ICPPASTCatchHandler catchHandler = (ICPPASTCatchHandler)statement;
			print("catch");

			CatchClause catchcls = ast.newCatchClause();
			catchcls.setBody((Block) evalStmt(catchHandler.getCatchBody()));
			catchcls.setException(evaluateDeclarationReturnSingleVariable(catchHandler.getDeclaration()));
			return catchcls;
		}
		return null;
	}

	/**
	 * Given a declaration, returns a single variable. This is used for 
	 * function arguments.
	 */
	private SingleVariableDeclaration evaluateDeclarationReturnSingleVariable(IASTDeclaration declaration) throws DOMException
	{
		SingleVariableDeclaration decl = ast.newSingleVariableDeclaration();
		List<SimpleName> names = evaluateDeclarationReturnNames(declaration);
		decl.setName(names.get(0));
		decl.setType(evaluateDeclarationReturnTypes(declaration).get(0));
		return decl;
	}

	private enum TypeEnum
	{
		NUMBER, 
		BOOLEAN,
		CHAR,
		VOID,
		POINTER,
		OTHER,
		ARRAY,
		ANY,
		REFERENCE
	};

	/**
	 * Given a typeId returns a Java type. Used in sizeof, etc. 
	 */
	private Type evalTypeId(IASTTypeId typeId) throws DOMException
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

		return TypeEnum.OTHER;
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
	private List<Expression> getArraySizeExpressions(IType type) throws DOMException
	{
		List<Expression> ret = new ArrayList<Expression>();

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

	private Type cppToJavaType(IType type) throws DOMException
	{
		return cppToJavaType(type, false, false);
	}

	/**
	 * Determines if a type will turn into a Ptr or Ref. If so, we should access
	 * it with .val suffix.
	 */
	private boolean isEventualPtrOrRef(IType type)
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
		else if (type instanceof ICPPReferenceType)
		{
			ICPPReferenceType ref = (ICPPReferenceType) type;

			if ((ref.getType() instanceof IQualifierType) || ref.getType() instanceof ICPPClassType)
				return false;
			else
				return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Returns the template arguments as JDT Types.
	 * Eg. HashMap<int, Foo> would return Integer, Foo.
	 */
	private List<Type> getTypeParams(ICPPTemplateArgument[] typeParams) throws DOMException
	{
		List<Type> types = new ArrayList<Type>();
		for (ICPPTemplateArgument param : typeParams)
		{
			if (param.getTypeValue() != null)
				types.add(cppToJavaType(param.getTypeValue(), false, true));
		}
		return types;
	}
	
	
	/**
	 * Attempts to convert a CDT type to a JDT type.
	 */
	private Type cppToJavaType(IType type, boolean retValue, boolean needBoxed) throws DOMException
	{
		if (type instanceof IBasicType)
		{
			// Primitive type - int, bool, char, etc...
			IBasicType basic = (IBasicType) type;
			
			if (needBoxed)
				return jast.newType(evaluateSimpleTypeBoxed(basic.getType(), basic.isShort(), basic.isLongLong(), basic.isUnsigned()));
			return evaluateSimpleType(basic.getType(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
		}
		else if (type instanceof IArrayType)
		{
			IArrayType array = (IArrayType) type;
			ArrayType arr = ast.newArrayType(cppToJavaType(array.getType()));
			return arr;
		}
		else if (type instanceof ICompositeType)
		{
			ICompositeType comp = (ICompositeType) type;
			Type simple = jast.newType(getSimpleType(comp.getName()));

			printerr(comp.getClass().getCanonicalName());
			
			if (type instanceof ICPPTemplateInstance)
			{
				ICPPTemplateInstance template = (ICPPTemplateInstance) type;
				print("template instance");

				ParameterizedType param = ast.newParameterizedType(simple);
				List<Type> list = getTypeParams(template.getTemplateArguments());
				param.typeArguments().addAll(list);
				return param;
			}
			else
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
				ParameterizedType paramType = ast.newParameterizedType(jast.newType("Ptr"));
				paramType.typeArguments().add(cppToJavaType(((IPointerType)pointer.getType()).getType()));
				return paramType;
			}
			else if (pointer.getType() instanceof IBasicType || (pointer.getType() instanceof ITypedef && ((ITypedef)(pointer.getType())).getType() instanceof IBasicType))
			{
				IBasicType basic;
				if (pointer.getType() instanceof ITypedef)
					basic = ((IBasicType)((ITypedef) pointer.getType()).getType());
				else
					basic = (IBasicType) pointer.getType();

				String basicStr = evaluateSimpleTypeBoxed(basic.getType(), basic.isShort(), basic.isLongLong(), basic.isUnsigned());
				SimpleType simpleType = jast.newType("Ptr" + basicStr);
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
				SimpleType simpleType = jast.newType("Ref" + basicStr);
				return simpleType;
			}
			else
			{
				ParameterizedType paramType = ast.newParameterizedType(jast.newType("Ref"));    		  
				paramType.typeArguments().add(cppToJavaType(ref.getType(), false, true));  		  
				return paramType;
			}
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

			return jast.newType("PROBLEM__");
		}
		else if (type instanceof ITypedef)
		{
			ITypedef typedef = (ITypedef) type;
			return cppToJavaType(typedef.getType(), retValue, needBoxed);
		}
		else if (type instanceof IEnumeration)
		{
			IEnumeration enumeration = (IEnumeration) type;
			return jast.newType(getSimpleType(enumeration.getName()));
		}
		else if (type instanceof IFunctionType)
		{
			IFunctionType func = (IFunctionType) type;
			return jast.newType("FunctionPointer");
		}
		else if (type instanceof IProblemType)
		{
			IProblemType prob = (IProblemType)type; 
			printerr("Problem type: " + prob.getMessage());
			//exitOnError();
			return jast.newType("PROBLEM");
		}
		else if (type instanceof ICPPTemplateTypeParameter)
		{
			ICPPTemplateTypeParameter template = (ICPPTemplateTypeParameter) type;
			print("template type");
			return jast.newType(template.toString());
		}
		else if (type != null)
		{
			printerr("Unknown type: " + type.getClass().getCanonicalName() + type.toString());
			exitOnError();
		}
		return null;
	}

	private boolean isBitfield(IASTName name) throws DOMException
	{
		String complete = getCompleteName(name);
		return bitfields.contains(complete);
	}

	private int findLastLoopId()
	{
		int cnt = 0;
		for (ScopeVar sv : m_localVariableStack)
		{
			cnt += sv.cnt;
			if (sv.isLoop)
				return cnt == 0 ? -1 : sv.id;
		}
		
		return -1;
	}
	
	private int findLastSwitchOrLoopId()
	{
		int cnt = 0;
		for (ScopeVar sv : m_localVariableStack)
		{
			cnt += sv.cnt;
			if (sv.isSwitch || sv.isLoop)
				return cnt == 0 ? -1 : sv.id;
		}
		return -1;
	}
	
	private void incrementLocalVariableId()
	{
		m_nextVariableId++;

		if (m_localVariableStack.peek() != null)
			m_localVariableStack.peek().cnt++;
		
		if (m_nextVariableId > m_localVariableMaxId)
			m_localVariableMaxId = m_nextVariableId;
	}

	private enum Flag
	{
		NEED_BOOLEAN,
		IS_LOOP,
		IS_SWITCH,
		IS_RET_VAL,
		IN_METHOD_ARGS
	}
	
	// The Java AST. We need this to create AST nodes...
	private AST ast;
	
	private JASTHelper jast;
	
	// The Java CompilationUnit. We add type declarations (classes, enums) to this...
	private CompilationUnit unit; 

	// A list of qualified names containing the bitfields...
	private List<String> bitfields = new ArrayList<String>();
	
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

	private int m_localVariableMaxId;
	private int m_nextVariableId;
	private Deque<ScopeVar> m_localVariableStack = new ArrayDeque<ScopeVar>();
	
	private static class FieldInfo
	{
		FieldInfo(IASTDeclarator declaratorArg, Expression initArg, IField fieldArg)
		{
			declarator = declaratorArg;
			init = initArg;
			field = fieldArg;
		}
		
		final IASTDeclarator declarator;
		final IField field;
		Expression init;
		boolean isBitfield;
	}
	
	private static class CompositeInfo
	{
		CompositeInfo(TypeDeclaration tydArg)
		{
			tyd = tydArg;
		}
		
		TypeDeclaration tyd;
		IASTDeclSpecifier declSpecifier;
		boolean hasCtor;
		boolean hasDtor;
		boolean hasCopy;
	}
	
	private Map<String, CompositeInfo> compositeMap = new HashMap<String, CompositeInfo>();

	
	/**
	 * Traverses the AST of the given translation unit
	 * and tries to convert the C++ abstract syntax tree to
	 * a Java AST.
	 */
	String traverse(IASTTranslationUnit translationUnit)
	{
		// Use AST.JLS4 to use features introduced with Java 7...
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource("".toCharArray());
		unit = (CompilationUnit) parser.createAST(null); 
		unit.recordModifications();
		ast = unit.getAST(); 
		jast = new JASTHelper(ast);
		
		PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
		unit.setPackage(packageDeclaration);
		packageDeclaration.setName(ast.newSimpleName("yourpackage")); //TODO

		TypeDeclaration global = ast.newTypeDeclaration();
		global.setName(ast.newSimpleName("Globals"));
		unit.types().add(global);

		compositeMap.put("", new CompositeInfo(global));

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

		char[] contents = null;
		try {
			Document doc = new Document();
			TextEdit edits = unit.rewrite(doc,null);
			edits.apply(doc);
			String sourceCode = doc.get();
			if (sourceCode != null) 
				contents = sourceCode.toCharArray(); 
		}
		catch (BadLocationException e) {
			throw new RuntimeException(e);
		}

		printerr("Java source is " + contents.length + " bytes");
		return String.valueOf(contents);
	}
}
