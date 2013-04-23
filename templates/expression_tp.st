expression_tp(expr_obj, want_lvalue_obj, want_lvalue_field) ::= <% 

<if(want_lvalue_obj && want_lvalue_field)>

  <! Return the plain object name. !>

  <if(expr_obj.isLiteral)>
    <expr_obj.literal>

  <elseif(expr_obj.isIdentity)>
    <expr_obj.ident>

  <elseif(expr_obj.isIdentityPtr)>
    <expr_obj.ident>

  <endif>

<elseif(want_lvalue_obj)>

  <! Return the plain object name. !>

  <if(expr_obj.isFieldReference)>
    <expression_tp(expr_obj.object, true, true)>.

  <elseif(expr_obj.isFieldReferenceBitfield)>
    <expression_tp(expr_obj.object, true, true)>.

  <elseif(expr_obj.isPtrArray)>
   <expression_tp(expr_obj.operand, false, false)>.add(<expr_obj.subscript:{s | <expression_tp(s, false, false)>}; separator=", ">).

  <elseif(expr_obj.isPrefixPointerStar)>
    <expression_tp(expr_obj.operand, false, false)>.

  <elseif(expr_obj.isFieldReferencePtr)>
    <expression_tp(expr_obj.object, false, false)>.<expr_obj.field>.

  <else>
    <! nothing !>

  <endif>

<elseif(want_lvalue_field)>

  <! Return the plain identity. !>

  <if(expr_obj.isFieldReference)>
    <expr_obj.field>

  <elseif(expr_obj.isFieldReferenceBitfield)>
    <expr_obj.field>

  <elseif(expr_obj.isFieldReferencePtr)>
    <expr_obj.field>

  <elseif(expr_obj.isIdentity)>
    <expr_obj.ident>

  <elseif(expr_obj.isIdentityBitfield)>
    <expr_obj.ident>

  <elseif(expr_obj.isIdentityPtr)>
    <expr_obj.ident>

  <else>
    PROBLEM

  <endif>

<else>

  <! Return a complete expression. !>

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
    <expr_obj.operator><expression_tp(expr_obj.operand, false, false)>

  <elseif(expr_obj.isPrefixPointerInc)>
    <expression_tp(expr_obj.operand, false, false)>.adjust(1)

  <elseif(expr_obj.isPrefixPointerDec)>
    <expression_tp(expr_obj.operand, false, false)>.adjust(-1)

  <elseif(expr_obj.isPrefixPointerStar)>
    <expression_tp(expr_obj.operand, false, false)>.get()

  <elseif(expr_obj.isPrefixBitfieldInc)>
    <expression_tp(expr_obj.operand, true, false)>set__<expression_tp(expr_obj.operand, false, true)>(<expression_tp(expr_obj.operand, false, false)> + 1)

  <elseif(expr_obj.isPrefixBitfieldDec)>
    <expression_tp(expr_obj.operand, true, false)>set__<expression_tp(expr_obj.operand, false, true)>(<expression_tp(expr_obj.operand, false, false)> - 1)

  <elseif(expr_obj.isPrefixBitfield)>
    <expr_obj.operator><expression_tp(expr_obj.operand, false, false)>

 <! <elseif(expr_obj.isPostfix)>
    <expression_tp(expr_obj.operand, true, false)><expr_obj.operator> !>

  <elseif(expr_obj.isPostfixPointerInc)>
    <expression_tp(expr_obj.operand, false, false)>.postinc()

  <elseif(expr_obj.isPostfixPointerDec)>
    <expression_tp(expr_obj.operand, false, false)>.postdec()

  <elseif(expr_obj.isPostfixBitfieldInc)>
    <expression_tp(expr_obj.operand, true, false)>postInc__<expression_tp(expr_obj.operand, false, true)>()

  <elseif(expr_obj.isPostfixBitfieldDec)>
    <expression_tp(expr_obj.operand, true, false)>postDec__<expression_tp(expr_obj.operand, false, true)>()

  <elseif(expr_obj.isFieldReference)>
    <expression_tp(expr_obj.object, false, false)>.<expr_obj.field>

  <elseif(expr_obj.isFieldReferenceBitfield)>
    <expression_tp(expr_obj.object, false, false)>.get__<expr_obj.field>()

  <elseif(expr_obj.isFieldReferencePtr)>
    <expression_tp(expr_obj.object, false, false)>.<expr_obj.field>.get()

  <elseif(expr_obj.isFieldReferenceEnumerator)>
    <expression_tp(expr_obj.object, false, false)>.<expr_obj.field>.val

  <elseif(expr_obj.isPrefix)>
    <expr_obj.operator><expression_tp(expr_obj.operand, false, false)>

  <elseif(expr_obj.isPostfix)>
    <expression_tp(expr_obj.operand, false, false)><expr_obj.operator>

  <elseif(expr_obj.isTernary)>
    <expression_tp(expr_obj.condition, false, false)> ? <expression_tp(expr_obj.positive, false, false)> : <expression_tp(expr_obj.negative, false, false)>

  <elseif(expr_obj.isArray)>
    <expression_tp(expr_obj.operand, false, false)><expr_obj.subscript:{s | [<expression_tp(s, false, false)>]}>

  <elseif(expr_obj.isPtrArray)>
   <expression_tp(expr_obj.operand, false, false)>.add(<expr_obj.subscript:{s | <expression_tp(s, false, false)>}; separator=", ">).get()

  <elseif(expr_obj.isFunctionCall)>
    <expression_tp(expr_obj.name, false, false)>(<expr_obj.args:{a | <expression_tp(a, false, false)>}; separator=", ">)

  <elseif(expr_obj.isClassInstanceCreation)>
    new <expression_tp(expr_obj.name, false, false)>(<expr_obj.args:{a | <expression_tp(a, false, false)>}; separator=", ">)

  <elseif(expr_obj.isAddItemCall)>
    StackHelper.addItem(<expression_tp(expr_obj.operand, false, false)>, <expr_obj.nextFreeStackId>, __stack)

  <elseif(expr_obj.isCast)>
    (<expr_obj.type>) <expression_tp(expr_obj.operand, false, false)>

  <elseif(expr_obj.isInfix)>
    <expression_tp(expr_obj.left, false, false)> <expr_obj.operator> <expression_tp(expr_obj.right, false, false)>

  <elseif(expr_obj.isInfixWithBitfieldOnLeft)>
    <expression_tp(expr_obj.left, false, false)> <expr_obj.operator> <expression_tp(expr_obj.right, false, false)>

  <elseif(expr_obj.isAssignmentWithBitfieldOnLeft)>
    <expression_tp(expr_obj.left, true, false)>set__<expression_tp(expr_obj.left, false, true)>(<expression_tp(expr_obj.right, false, false)>)

  <elseif(expr_obj.isCompoundWithBitfieldOnLeft)>
    <expression_tp(expr_obj.left, true, false)>set__<expression_tp(expr_obj.left, false, true)>(<expression_tp(expr_obj.left, false, false)> <expr_obj.operator> <expression_tp(expr_obj.right, false, false)>)

  <elseif(expr_obj.isInfixWithDerefOnLeft)>
    <expression_tp(expr_obj.left, false, false)> <expr_obj.operator> <expression_tp(expr_obj.right, false, false)>

  <elseif(expr_obj.isAssignmentWithDerefOnLeft)>
    <expression_tp(expr_obj.left, true, false)>set(<expression_tp(expr_obj.right, false, false)>)

  <elseif(expr_obj.isCompoundWithDerefOnLeft)>
    <expression_tp(expr_obj.left, true, false)>set(<expression_tp(expr_obj.left, false, false)> <expr_obj.operator> <expression_tp(expr_obj.right, false, false)>)

  <elseif(expr_obj.isDeleteObjectSingle)>
    <expression_tp(expr_obj.operand, false, false)>.destruct()

  <elseif(expr_obj.isDeleteObjectMultiple)>
    DestructHelper.destructArray(<expression_tp(expr_obj.operand, false, false)>)

  <elseif(expr_obj.isBasicNewArray)>
    PTR.new<expr_obj.type>Ptr(new <expr_obj.type><expr_obj.sizes:{s | [<expression_tp(s, false, false)>]}; separator=", ">)

  <elseif(expr_obj.isObjectNewArray)>
    PTR.newObjPtr(CreateHelper.allocateArray(<expr_obj.type>.class, <expr_obj.sizes:{s | <expression_tp(s, false, false)>}; separator=", ">)

  <elseif(expr_obj.isNewSingle)>
    PTR.new<expr_obj.type>Ptr(<expression_tp(expr_obj.argument, false, false)>)

  <elseif(expr_obj.isNewObject)>
    new <expr_obj.type>(<expr_obj.arguments:{a | <expression_tp(a, false, false)>}; separator=", ">)

  <elseif(expr_obj.isAddressOf)>
    <expression_tp(expr_obj.operand, false, false)>.addressOf()

  <endif>

<endif>

%>

