declaration_tp(decl) ::= <%

 <if(decl.isEnum)>
  <\n><enum_tp(decl.simpleName, decl.enumerators, "int")>

 <elseif(decl.isBitfield)>
  <\n><bitfield_tp(decl)>

 <elseif(decl.isGeneratedDtor)>
  <\n>@Override
  <\n>public void destruct()
  <\n><statement_tp(decl.body)>

 <elseif(decl.isGeneratedCtor)>
  <\n>public <decl.type>()
  <\n><statement_tp(decl.body)>
 
 <elseif(decl.isGeneratedAssign)>
  <\n>@Override
  <\n>public <decl.type> opAssign(<decl.type> right)
  <\n><statement_tp(decl.body)>

 <elseif(decl.isFunctionDeclaration)>
  <\n>public <decl.retType> <decl.name>(<decl.args:{a | <a.type> <a.name>}; separator=", ">)
  <\n><statement_tp(decl.body)>

 <elseif(decl.isClassDeclaration)>
  <\n><if(decl.isUnion)>@CppUnion<endif>
  <\n>public<if(decl.isNested)> static<endif> class <decl.name><if(decl.superclass)> extends <decl.superclass><endif> implements CppType\<<decl.name>\>/* <decl.additionalSupers:{s | <s>}> */
  <\n>{
  <\n>  <decl.declarations:{d | <declaration_tp(d)>}>
  <\n>}

 <elseif(decl.isSimpleDecl)>
  <\n><if(decl.isPublic)>public<endif><if(decl.isNested)> static<endif> <decl.type> <decl.name><if(decl.initExpr)> = <expression_tp(decl.initExpr)><endif><if(decl.isPublic)>;<endif>

 <else>
  <\n>// ERROR.

 <endif>

%>

