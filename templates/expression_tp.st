expression_tp(expr_obj) ::= <% 
<if(expr_obj.isIdentity)>

   <expr_obj.ident>

<elseif(expr_obj.isIdentityBitfield)>

  get__<expr_obj.ident>()

<elseif(expr_obj.isIdentityEnumerator)>

  <expr_obj.enumName>.<expr_obj.ident>

<elseif(expr_obj.isIdentityPtr)>

  <expr_obj.ident>.get()

<elseif(expr_obj.isLiteral)>

  <expr_obj.literal>

<elseif(expr_obj.isFieldReference)>

   <expression_tp(expr_obj.object)>.<expr_obj.field>

<elseif(expr_obj.isInfix)>

   <expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>

<elseif(expr_obj.isInfixWithBitfieldOnLeft)>

   set_<expr_obj.left.ident>(<expression_tp(expr_obj.left)> <expr_obj.operator> <expression_tp(expr_obj.right)>)

<elseif(expr_obj.isPrefix)>

   <expr_obj.operator><expression_tp(expr_obj.operand)>

<elseif(expr_obj.isPostfix)>

   <expression_tp(expr_obj.operand)><expr_obj.operator>

<elseif(expr_obj.isTernary)>

   <expression_tp(expr_obj.condition)> ? <expression_tp(expr_obj.positive)> : <expression_tp(expr_obj.negative)>



<elseif(expr_obj.isFunctionCall)>

   <expression_tp(expr_obj.name)>(<expr_obj.args:{a | expression_tp(a) }; separator=", ">)

<elseif(expr_obj.isCast)>

   (<expr_obj.type>) <expression_tp(expr_obj.operand)>

<endif>
%>
