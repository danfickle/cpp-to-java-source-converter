package com.github.danfickle.cpptojavasourceconverter;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.*;
import com.github.danfickle.cpptojavasourceconverter.ExpressionModels.*;
import com.github.danfickle.cpptojavasourceconverter.SourceConverter.CompositeInfo;
import com.github.danfickle.cpptojavasourceconverter.SourceConverter.FieldInfo;
import com.github.danfickle.cpptojavasourceconverter.StmtModels.*;
import com.github.danfickle.cpptojavasourceconverter.TypeHelpers.TypeEnum;
import com.github.danfickle.cpptojavasourceconverter.VarDeclarations.MSimpleDecl;

class SpecialGenerator
{
	private final GlobalContext ctx;
	
	SpecialGenerator(GlobalContext con)
	{
		ctx = con;
	}
	
	/**
	 * Given a list of fields for a class, adds initialization statements
	 * to the constructor for each field as required.
	 * Initializers provided to this function are generated from C++ initializer
	 * lists, and implicit initializers for objects.
	 * Note: We must initialize in order that fields were declared.
	 */
	void generateCtorStatements(List<FieldInfo> fields, MCompoundStmt method) throws DOMException
	{
		int start = 0;
		for (FieldInfo fieldInfo : fields)
		{
			MyLogger.log(fieldInfo.field.getName());

			// Static fields can't be initialized in the constructor.
			if (fieldInfo.init != null && !fieldInfo.isStatic)
			{
				if (ctx.bitfieldMngr.isBitfield(fieldInfo.declarator.getName()))
				{
					MInfixAssignmentWithBitfieldOnLeft infix = new MInfixAssignmentWithBitfieldOnLeft();
					MFieldReferenceExpressionBitfield lbf = new MFieldReferenceExpressionBitfield();
					
					lbf.object = ModelCreation.createLiteral("this");
					lbf.field = fieldInfo.field.getName();
					
					infix.left = lbf;
					infix.right = fieldInfo.init;
					
					MStmt stmt = ModelCreation.createExprStmt(infix);
					method.statements.add(start, stmt);
					start++;
				}
				else
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
	
	CppFunction generateCopyCtor(CompositeInfo info, CppClass tyd, IASTDeclSpecifier declSpecifier) throws DOMException
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
		
		List<FieldInfo> fields = ctx.converter.collectFieldsForClass(declSpecifier);

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
					TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.OBJECT)
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
//				CastExpression cast = ast.newCastExpression();
//				//cast.setType(cppToJavaType(fieldInfo.field.getType()));
//				cast.setExpression(meth3);
			}
			else if (ctx.bitfieldMngr.isBitfield(fieldInfo.declarator.getName()))
			{
				MInfixAssignmentWithBitfieldOnLeft infix = new MInfixAssignmentWithBitfieldOnLeft();
				MFieldReferenceExpressionBitfield lbf = new MFieldReferenceExpressionBitfield();
				MFieldReferenceExpressionBitfield rbf = new MFieldReferenceExpressionBitfield();
				
				lbf.object = ModelCreation.createLiteral("this");
				lbf.field = fieldInfo.field.getName();
				
				rbf.object = ModelCreation.createLiteral("right");
				rbf.field = fieldInfo.field.getName();
				
				infix.left = lbf;
				infix.right = rbf;
				
				MStmt stmt = ModelCreation.createExprStmt(infix);
				meth.body.statements.add(stmt);
			}
			// TODO: basic objects, enums, etc.
			else
			{
				// TODO: Is this needed?
				// this.field = right.field;
				MFieldReferenceExpression fr1 = ModelCreation.createFieldReference("this", fieldInfo.field.getName());
				MFieldReferenceExpression fr2 = ModelCreation.createFieldReference("right", fieldInfo.field.getName());

				MExpression infix = ModelCreation.createInfixExpr(fr1, fr2, "=");						
				meth.body.statements.add(ModelCreation.createExprStmt(infix));
			}
		}
		return meth;
	}
	
	CppAssign generateAssignMethod(CompositeInfo info, CppClass tyd, IASTDeclSpecifier declSpecifier) throws DOMException
	{
		CppAssign ass = new CppAssign();
		
		ass.type = tyd.name;
		ass.body = new MCompoundStmt();
		
		List<FieldInfo> fields = ctx.converter.collectFieldsForClass(declSpecifier);

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
					TypeHelpers.getTypeEnum(fieldInfo.field.getType()) == TypeEnum.OBJECT)
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
			else if (ctx.bitfieldMngr.isBitfield(fieldInfo.declarator.getName()))
			{
				MInfixAssignmentWithBitfieldOnLeft infix = new MInfixAssignmentWithBitfieldOnLeft();
				MFieldReferenceExpressionBitfield lbf = new MFieldReferenceExpressionBitfield();
				MFieldReferenceExpressionBitfield rbf = new MFieldReferenceExpressionBitfield();
				
				lbf.object = ModelCreation.createLiteral("this");
				lbf.field = fieldInfo.field.getName();
				
				rbf.object = ModelCreation.createLiteral("right");
				rbf.field = fieldInfo.field.getName();
				
				infix.left = lbf;
				infix.right = rbf;
				
				MStmt stmt = ModelCreation.createExprStmt(infix);
				ifBlock.statements.add(stmt);
			}
			// TODO: Basic objects, enums, etc.
			else
			{
				// TODO: IS this needed?
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
		
		return ass;
	}
}
