expression_tp(expr_obj) ::= <% 
<if(expr_obj.isIdentity)>

   <expr_obj.ident>

<elseif(expr_obj.isIdentityBitfield)>

  get__<expr_obj.ident>()

<elseif(expr_obj.isIdentityEnumerator)>

  <expr_obj.enumName>.<expr_obj.ident>.val

<elseif(expr_obj.isIdentityPtr)>

  <expr_obj.ident>

<elseif(expr_obj.isLiteral)>

  <expr_obj.literal>

<elseif(expr_obj.isPrefix)>

  <expr_obj.operator><expression_tp(expr_obj.operand)>

<elseif(expr_obj.isPrefixPointerInc)>

  <expression_tp(expr_obj.operand)>.adjust(1)

<elseif(expr_obj.isPrefixPointerDec)>

  <expression_tp(expr_obj.operand)>.adjust(-1)

<elseif(expr_obj.isPrefixPointerStar)>

  <expression_tp(expr_obj.operand)>.get()

<elseif(expr_obj.isPrefixBitfieldInc)>

  <if(expr_obj.operand.isFieldReferenceBitfield)>
    <expression_tp(expr_obj.operand.object)>.set__<expr_obj.operand.field>(<expression_tp(expr_obj.operand.object)>.get__<expr_obj.operand.field>() + 1)
  <else>
    set__<expression_tp(expr_obj.set)>(<expression_tp(expr_obj.operand)> + 1)
  <endif>

<elseif(expr_obj.isPrefixBitfieldDec)>

  <if(expr_obj.operand.isFieldReferenceBitfield)>
    <expression_tp(expr_obj.operand.object)>.set__<expr_obj.operand.field>(<expression_tp(expr_obj.operand.object)>.get__<expr_obj.operand.field>() - 1)
  <else>
    set__<expression_tp(expr_obj.set)>(<expression_tp(expr_obj.operand)> - 1)
  <endif>

<elseif(expr_obj.isPrefixBitfield)>
 
  <if(expr_obj.operand.isFieldReferenceBitfield)>
    <expression_tp(expr_obj.operand.object)>.set__<expr_obj.operand.field>(<expr_obj.operator><expression_tp(expr_obj.operand.object)>.get__<expr_obj.operand.field>())
  <else>
    set__<expression_tp(expr_obj.set)>(<expr_obj.operator><expression_tp(expr_obj.operand)>)
  <endif>

<elseif(expr_obj.isPostfix)>

  <expression_tp(expr_obj.operand)><expr_obj.operator>

<elseif(expr_obj.isPostfixPointer)>

 TODO  <expression_tp(expr_obj.operand)>

<elseif(expr_obj.isPostfixPointerInc)>

  <expression_tp(expr_obj.operand)>.postinc()

<elseif(expr_obj.isPostfixPointerDec)>

  <expression_tp(expr_obj.operand)>.postdec()

<elseif(expr_obj.isPostfixBitfieldInc)>

  <if(expr_obj.operand.isFieldReference)>
    <expression_tp(expr_obj.operand.object)>.postInc__<expr_obj.operand.field>()
  <else>
    postInc__<expression_tp(expr_obj.operand)>()
  <endif>

<elseif(expr_obj.isPostfixBitfieldDec)>

  <if(expr_obj.operand.isFieldReference)>
    <expression_tp(expr_obj.operand.object)>.postDec__<expr_obj.operand.field>()
  <else>
    postDec__<expression_tp(expr_obj.operand)>()
  <endif>

<elseif(expr_obj.isFieldReference)>

   <expression_tp(expr_obj.object)>.<expr_obj.field>

<elseif(expr_obj.isFieldReferenceBitfield)>

   <expression_tp(expr_obj.object)>.get__<expr_obj.field>()

<elseif(expr_obj.isFieldReferencePtr)>

   <expression_tp(expr_obj.object)>.<expr_obj.field>.get()

<elseif(expr_obj.isFieldReferenceEnumerator)>

   <expression_tp(expr_obj.object)>.<expr_obj.field>.val

<elseif(expr_obj.isPrefix)>

   <expr_obj.operator><expression_tp(expr_obj.operand)>

<elseif(expr_obj.isPostfix)>

   <expression_tp(expr_obj.operand)><expr_obj.operator>

<elseif(expr_obj.isTernary)>

   <expression_tp(expr_obj.condition)> ? <expression_tp(expr_obj.positive)> : <expression_tp(expr_obj.negative)>

<elseif(expr_obj.isArray)> <! 1 !>

   <expression_tp(expr_obj.operand)><expr_obj.subscript:{s | [<expression_tp(s)>]}>

<elseif(expr_obj.isPtrArray)> <! 2 !>

   <expression_tp(expr_obj.operand)>.add(<expr_obj.subscript:{s | <expression_tp(s)>}; separator=", ">).get()

<elseif(expr_obj.isFunctionCall)>

   <expression_tp(expr_obj.name)>(<expr_obj.args:{a | expression_tp(a) }; separator=", ">)

<elseif(expr_obj.isCast)>

   (<expr_obj.type>) <expression_tp(expr_obj.operand)>

<elseif(expr_obj.isInfix)>

   <expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>

<elseif(expr_obj.isInfixWithBitfieldOnLeft)>

   <expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>

<elseif(expr_obj.isAssignmentWithBitfieldOnLeft)>

   set__<expr_obj.left.ident>(<expression_tp(expr_obj.right)>)

<elseif(expr_obj.isCompoundWithBitfieldOnLeft)>

   set__<expr_obj.left.ident>(<expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>)

<endif>
%>
