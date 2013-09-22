package com.github.danfickle.cpptojavasourceconverter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.*;

import com.github.danfickle.cpptojavasourceconverter.DeclarationModels.CppBitfield;
import com.github.danfickle.cpptojavasourceconverter.SourceConverterStage2.GlobalContext;

public class BitfieldHelpers
{
	// A set of qualified names containing the bitfields...
	private Set<String> bitfields = new HashSet<String>();

	private GlobalContext ctx;
	
	BitfieldHelpers(GlobalContext con) {
		ctx = con;
	}

	boolean isBitfield(IASTName name) throws DOMException
	{
		String complete = TypeHelpers.getCompleteName(name);
		return bitfields.contains(complete);
	}
	
	static String getBitfieldSimpleName(IASTExpression expr) throws DOMException
	{
		if (expr instanceof IASTIdExpression)
			return TypeHelpers.getSimpleName(((IASTIdExpression) expr).getName());
		else
			return TypeHelpers.getSimpleName(((IASTFieldReference) expr).getFieldName());
	}
	
	void addBitfield(IASTName name) throws DOMException
	{
		String complete = TypeHelpers.getCompleteName(name);
		bitfields.add(complete);
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
		bitfield.name = field.getName();
		bitfield.bits = ctx.exprEvaluator.eval1Expr(((IASTFieldDeclarator) declarator).getBitFieldSize());
		bitfield.type = TypeHelpers.cppToJavaType(field.getType());
		ctx.converter.addDeclaration(bitfield);
		ctx.converter.popDeclaration();
	}

	void add(String nm)
	{
		bitfields.add(nm);
	}
}
