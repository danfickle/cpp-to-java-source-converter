lhs_object_tp(expr_obj) ::= <% 

  <! Returns a raw pointer object or number object ready for calling methods on. !>

  <if(expr_obj.isPtrArray)>
   <lhs_object_tp(expr_obj.operand)>.ptrOffset(<expr_obj.subscript:{s | <expression_tp(s)>}; separator=", ">)

  <elseif(expr_obj.isPrefixPointerInc)>
   <lhs_object_tp(expr_obj.operand)>.ptrAdjust(1)

  <elseif(expr_obj.isPrefixPointerDec)>
   <lhs_object_tp(expr_obj.operand)>.ptrAdjust(-1)

  <elseif(expr_obj.isArrayAccessWithAddressOf)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isPostfixPointerInc)>
   <lhs_object_tp(expr_obj.operand)>.ptrPostInc()

  <elseif(expr_obj.isPostfixPointerDec)>
   <lhs_object_tp(expr_obj.operand)>.ptrPostDec()

  <elseif(expr_obj.isPrefixPointerStar)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isBrackets)>
   (<lhs_object_tp(expr_obj.operand)>)

  <elseif(expr_obj.isInfixWithPtrOnLeft)>
   <lhs_object_tp(expr_obj.left)>.ptrOffset(<expr_obj.operator><expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isInfixWithPtrOnRight)>
   <lhs_object_tp(expr_obj.right)>.ptrOffset(<expr_obj.operator><expression_tp(expr_obj.left)>)

  <elseif(expr_obj.isIdentityPtr)>
   <expr_obj.ident>

  <elseif(expr_obj.isFieldReferencePtr)>
   <lhs_object_tp(expr_obj.object)>.<expr_obj.field>

  <elseif(expr_obj.isIdentity)>
   <expr_obj.ident>

  <elseif(expr_obj.isIdentityNumber)>
   <expr_obj.ident>

  <elseif(expr_obj.isFieldReferenceNumber)>
   <lhs_object_tp(expr_obj.object)>.<expr_obj.field>

  <elseif(expr_obj.isPostfixNumberInc)>
   <lhs_object_tp(expr_obj.operand)>.postInc()

  <elseif(expr_obj.isPostfixNumberDec)>
   <lhs_object_tp(expr_obj.operand)>.postDec()

  <elseif(expr_obj.isPrefixNumberInc)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isPrefixNumberDec)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isAssignmentWithNumberOnLeft)>
   <lhs_object_tp(expr_obj.left)>

  <elseif(expr_obj.isCompoundWithNumberOnLeft)>
   <lhs_object_tp(expr_obj.left)>

  <elseif(expr_obj.isIdentityBitfield)>
   <! NOTHING !>

  <elseif(expr_obj.isIdentityEnumerator)>
   <expr_obj.enumName>.<expr_obj.ident>.val

  <elseif(expr_obj.isValueOfNumber)>
   BAD

  <elseif(expr_obj.isLiteral)>
   <expr_obj.literal>

  <elseif(expr_obj.isPrefix)>
   BAD

  <elseif(expr_obj.isPrefixPointerInc)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isPrefixPointerDec)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isPrefixPointerStar)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isPrefixBitfieldInc)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isPrefixBitfieldDec)>
   <lhs_object_tp(expr_obj.operand)>

  <elseif(expr_obj.isPrefixBitfield)>
   BAD

  <elseif(expr_obj.isFieldReferenceBitfield)>
   <lhs_object_tp(expr_obj.object)>

  <elseif(expr_obj.isFieldReference)>
   <lhs_object_tp(expr_obj.object)>.<expr_obj.field>

  <else>
    MISSING

  <endif>
%>

