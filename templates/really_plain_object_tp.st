really_plain_object_tp(expr_obj) ::= <%

 <! Return the plain object name without trailing dot. !>

  <if(expr_obj.isLiteral)>
    <expr_obj.literal>

  <elseif(expr_obj.isIdentity)>
    <expr_obj.ident>

  <elseif(expr_obj.isIdentityPtr)>
    <expr_obj.ident>

  <else>
    /* 1 */

  <endif>
%>

