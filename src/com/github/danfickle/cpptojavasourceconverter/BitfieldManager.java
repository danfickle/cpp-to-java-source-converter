package com.github.danfickle.cpptojavasourceconverter;

import org.eclipse.cdt.core.dom.ast.*;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppBitfield;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.NameType;
import com.github.danfickle.cpptojavasourceconverter.TypeManager.TypeType;

public class BitfieldManager
{
	private TranslationUnitContext ctx;
	
	BitfieldManager(TranslationUnitContext con) {
		ctx = con;
	}

	boolean isBitfield(IASTName name) throws DOMException
	{
		String complete = TypeManager.getCompleteName(name);
		return ctx.global.bitfields.contains(complete);
	}
	
	void addBitfield(IASTName name) throws DOMException
	{
		String complete = TypeManager.getCompleteName(name);
		ctx.global.bitfields.add(complete);
	}
	
	boolean isBitfield(IASTExpression expr) throws DOMException
	{
		expr = ExpressionHelpers.unwrap(expr);
		
		if (expr instanceof IASTIdExpression &&
			isBitfield(((IASTIdExpression) expr).getName()))
			return true;
		
		if (expr instanceof IASTFieldReference &&
			isBitfield(((IASTFieldReference) expr).getFieldName()))
			return true;
		
		return false;
	}
	
	void evalDeclBitfield(IField field, IASTDeclarator declarator) throws DOMException
	{
		CppBitfield bitfield = new CppBitfield();
		addBitfield(declarator.getName());
		bitfield.name = TypeManager.cppNameToJavaName(field.getName(), NameType.CAMEL_CASE);
		bitfield.bits = ctx.exprEvaluator.eval1Expr(((IASTFieldDeclarator) declarator).getBitFieldSize());
		bitfield.type = ctx.typeMngr.cppToJavaType(field.getType(), TypeType.RAW);
		ctx.converter.currentInfoStack.peekFirst().tyd.declarations.add(bitfield);
	}

	void addBitfield(String nm)
	{
		ctx.global.bitfields.add(nm);
	}
}
