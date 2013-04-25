declaration_tp(decl) ::= <<

 <if(decl.isEnum)>
   <enum_tp(decl.simpleName, decl.enumerators, "int")>

 <elseif(decl.isBitfield)>
   <bitfield_tp(decl)>

 <elseif(decl.isGeneratedDtor)>
  @Override
  public void destruct()
  <statement_tp(decl.body)>

 <elseif(decl.isGeneratedCtor)>
  public <decl.type>()
  <statement_tp(decl.body)>
 
 <elseif(decl.isGeneratedAssign)>
  @Override
  public <decl.type> opAssign(<decl.type> right)
  <statement_tp(decl.body)>

 <elseif(decl.isFunctionDeclaration)>
  public <decl.retType> <decl.name>(<decl.args:{a | <a.type> <a.name>}; separator=", ">)
  <statement_tp(decl.body)>

 <elseif(decl.isClassDeclaration)>
  <if(decl.isUnion)>@CppUnion<endif>
  public<if(decl.isNested)> static<endif> class <decl.name><if(decl.superclass)> extends <decl.superclass><endif> implements CppType\<<decl.name>\>/* <decl.additionalSupers:{s | <s>}> */
  {
    <decl.declarations:{d | <declaration_tp(d)>}>
  }

 <elseif(decl.isSimpleDecl)>
  public<if(decl.isNested)> static<endif> <decl.type> <decl.name><if(decl.initExpr)> = <expression_tp(decl.initExpr, false, false)><endif>

 <else>
  // ERROR.

 <endif>

>>

