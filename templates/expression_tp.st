expression_tp(expr_obj) ::= <% 

  <! Return a complete RHS capable expression. !>

  <if(expr_obj.isIdentity)>
   <expr_obj.ident>

  <elseif(expr_obj.isIdentityBitfield)>
    get__<expr_obj.ident>()

  <elseif(expr_obj.isIdentityEnumerator)>
    <expr_obj.enumName>.<expr_obj.ident>.val

  <elseif(expr_obj.isIdentityPtr)>
    <expr_obj.ident>.ptrCopy()

  <elseif(expr_obj.isIdentityNumber)>
    <expr_obj.ident>.get()

  <elseif(expr_obj.isFieldReferenceNumber)>
    <lhs_object_tp(expr_obj.object)>.<expr_obj.field>.get()

  <elseif(expr_obj.isPostfixNumberInc)>
    <lhs_object_tp(expr_obj)>.get()

  <elseif(expr_obj.isPostfixNumberDec)>
    <lhs_object_tp(expr_obj)>.get()

  <elseif(expr_obj.isPrefixNumberInc)>
    <lhs_object_tp(expr_obj)>.set(<expression_tp(expr_obj.operand)> + 1)

  <elseif(expr_obj.isPrefixNumberDec)>
    <lhs_object_tp(expr_obj)>.set(<expression_tp(expr_obj.operand)> - 1)

  <elseif(expr_obj.isAssignmentWithNumberOnLeft)>
    <lhs_object_tp(expr_obj.left)>.set(<expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isCompoundWithNumberOnLeft)>
    <lhs_object_tp(expr_obj.left)>.set(<expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isValueOfNumber)>
    <expr_obj.type>.valueOf(<expression_tp(expr_obj.operand)>)

  <elseif(expr_obj.isBrackets)>
   (<expression_tp(expr_obj.operand)>)

  <elseif(expr_obj.isLiteral)>
    <expr_obj.literal>

  <elseif(expr_obj.isPrefix)>
    <expr_obj.operator><expression_tp(expr_obj.operand)>

  <elseif(expr_obj.isPrefixPointerInc)>
    <lhs_object_tp(expr_obj)>

  <elseif(expr_obj.isPrefixPointerDec)>
    <lhs_object_tp(expr_obj)>

  <elseif(expr_obj.isPrefixPointerStar)>
    <lhs_object_tp(expr_obj.operand)>.get()

  <elseif(expr_obj.isPrefixBitfieldInc)>
    <lhs_object_tp(expr_obj.operand)>.set__<plain_name_tp(expr_obj.operand)>(<expression_tp(expr_obj.operand)> + 1)

  <elseif(expr_obj.isPrefixBitfieldDec)>
    <lhs_object_tp(expr_obj.operand)>.set__<plain_name_tp(expr_obj.operand)>(<expression_tp(expr_obj.operand)> - 1)

  <elseif(expr_obj.isPrefixBitfield)>
    <expr_obj.operator><expression_tp(expr_obj.operand)>

  <elseif(expr_obj.isPostfixPointerInc)>
    <lhs_object_tp(expr_obj)>

  <elseif(expr_obj.isPostfixPointerDec)>
    <lhs_object_tp(expr_obj)>

  <elseif(expr_obj.isPostfixBitfieldInc)>
    <lhs_object_tp(expr_obj.operand)>.postInc__<plain_name_tp(expr_obj.operand)>()

  <elseif(expr_obj.isPostfixBitfieldDec)>
    <lhs_object_tp(expr_obj.operand)>.postDec__<plain_name_tp(expr_obj.operand)>()

  <elseif(expr_obj.isFieldReference)>
    <lhs_object_tp(expr_obj.object)>.<expr_obj.field>

  <elseif(expr_obj.isFieldReferenceBitfield)>
    <lhs_object_tp(expr_obj.object)>.get__<expr_obj.field>()

  <elseif(expr_obj.isFieldReferencePtr)>
    <lhs_object_tp(expr_obj.object)>.<expr_obj.field>.ptrCopy()

  <elseif(expr_obj.isFieldReferenceEnumerator)>
    <lhs_object_tp(expr_obj.object)>.<expr_obj.field>.val

  <elseif(expr_obj.isPrefix)>
    <expr_obj.operator><expression_tp(expr_obj.operand)>

  <elseif(expr_obj.isPostfix)>
    <expression_tp(expr_obj.operand)><expr_obj.operator>

  <elseif(expr_obj.isTernary)>
    <expression_tp(expr_obj.condition)> ? <expression_tp(expr_obj.positive)> : <expression_tp(expr_obj.negative)>

  <elseif(expr_obj.isPtrArray)>
   <lhs_object_tp(expr_obj.operand)>.ptrOffset(<expr_obj.subscript:{s | <expression_tp(s)>}; separator=", ">).get()

  <elseif(expr_obj.isArrayAccessWithAddressOf)>
   <lhs_object_tp(expr_obj)>.addressOf()

  <elseif(expr_obj.isFunctionCall)>
    <expression_tp(expr_obj.name)>(<expr_obj.args:{a | <expression_tp(a)>}; separator=", ">)

  <elseif(expr_obj.isClassInstanceCreation)>
    new <expression_tp(expr_obj.name)>(<expr_obj.args:{a | <expression_tp(a)>}; separator=", ">)

  <elseif(expr_obj.isAddItemCall)>
    StackHelper.addItem(<expression_tp(expr_obj.operand)>, <expr_obj.nextFreeStackId>, __stack)

  <elseif(expr_obj.isCast)>
    (<expr_obj.type>) <expression_tp(expr_obj.operand)>

  <elseif(expr_obj.isInfixWithPtrOnLeft)>
    <lhs_object_tp(expr_obj.left)>.ptrOffset(<expr_obj.operator><expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isInfixWithPtrOnRight)>
    <lhs_object_tp(expr_obj.right)>.ptrOffset(<expr_obj.operator><expression_tp(expr_obj.left)>)

  <elseif(expr_obj.isAssignmentWithPtrOnLeft)>
    <lhs_object_tp(expr_obj.left)> = <expression_tp(expr_obj.right)>

  <elseif(expr_obj.isCompoundWithPtrOnLeft)>
    <lhs_object_tp(expr_obj.left)>.ptrAdjust(<expr_obj.operator><expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isAddressOfPtr)>
    <lhs_object_tp(expr_obj.operand)>.ptrAddressOf()

  <elseif(expr_obj.isPtrCopy)>
    <expression_tp(expr_obj.operand)>

  <elseif(expr_obj.isInfix)>
    <expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>

  <elseif(expr_obj.isInfixWithBitfieldOnLeft)>
    <expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>

  <elseif(expr_obj.isAssignmentWithBitfieldOnLeft)>
    <lhs_object_tp(expr_obj.left)>.set__<plain_name_tp(expr_obj.left)>(<expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isCompoundWithBitfieldOnLeft)>
    <lhs_object_tp(expr_obj.left)>.set__<plain_name_tp(expr_obj.left)>(<expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isInfixWithDerefOnLeft)>
    <expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>

  <elseif(expr_obj.isAssignmentWithDerefOnLeft)>
    <lhs_object_tp(expr_obj.left)>.set(<expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isCompoundWithDerefOnLeft)>
    <lhs_object_tp(expr_obj.left)>.set(<expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isDeleteObjectSingle)>
    <expression_tp(expr_obj.operand)>.destruct()

  <elseif(expr_obj.isDeleteObjectMultiple)>
    DestructHelper.destructArray(<expression_tp(expr_obj.operand)>)

  <elseif(expr_obj.isBasicNewArray)>
    <expr_obj.type>Multi.create(<expr_obj.sizes:{s | <expression_tp(s)>}; separator=", ">)

  <elseif(expr_obj.isObjectNewArray)>
    PTR.newObjPtr(CreateHelper.allocateArray(<expr_obj.type>.class, <expr_obj.sizes:{s | <expression_tp(s)>}; separator=", ">)

  <elseif(expr_obj.isNewSingle)>
    PTR.new<expr_obj.type>Ptr(<expression_tp(expr_obj.argument)>)

  <elseif(expr_obj.isNewObject)>
    new <expr_obj.type>(<expr_obj.arguments:{a | <expression_tp(a)>}; separator=", ">)

  <elseif(expr_obj.isAddressOf)>
    <lhs_object_tp(expr_obj.operand)>.addressOf()

  <elseif(expr_obj.isEmpty)>
    /* Empty expression */

  <elseif(!expr_obj)>
    /* Null expression */

  <else>
    /* 4 */

  <endif>

%>

