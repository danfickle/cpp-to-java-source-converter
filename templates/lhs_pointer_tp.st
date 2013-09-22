lhs_pointer_tp(expr_obj) ::= <% 

  <if(expr_obj.isPtrArray)>
   <plain_variable_tp(expr_obj.operand)>.ptrOffset(<expr_obj.subscript:{s | <expression_tp(s)>}; separator=", ">)

  <elseif(expr_obj.isPrefixPointerInc)>
   <plain_variable_tp(expr_obj.operand)>.ptrAdjust(1)

  <elseif(expr_obj.isPrefixPointerDec)>
   <plain_variable_tp(expr_obj.operand)>.ptrAdjust(-1)

  <elseif(expr_obj.isArrayAccessWithAddressOf)>
   <lhs_pointer_tp(expr_obj.operand)>

  <elseif(expr_obj.isPostfixPointerInc)>
    <plain_variable_tp(expr_obj.operand)>.ptrPostInc()

  <elseif(expr_obj.isPostfixPointerDec)>
    <plain_variable_tp(expr_obj.operand)>.ptrPostDec()

  <elseif(expr_obj.isPrefixPointerStar)>
    <lhs_pointer_tp(expr_obj.operand)>

  <elseif(expr_obj.isBrackets)>
   (<lhs_pointer_tp(expr_obj.operand)>)

  <elseif(expr_obj.isInfixWithPtrOnLeft)>
    <lhs_pointer_tp(expr_obj.left)>.ptrOffset(<expr_obj.operator><expression_tp(expr_obj.right)>)

  <elseif(expr_obj.isInfixWithPtrOnRight)>
    <lhs_pointer_tp(expr_obj.right)>.ptrOffset(<expr_obj.operator><expression_tp(expr_obj.left)>)

  <elseif(expr_obj.isIdentityPtr)>
    <expr_obj.ident>

  <elseif(expr_obj.isFieldReferencePtr)>
    <expression_tp(expr_obj.object)>.<expr_obj.field>

  <else>
    <expression_tp(expr_obj)>
  <endif>
%>

