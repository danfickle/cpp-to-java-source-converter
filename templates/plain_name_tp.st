plain_name_tp(expr_obj) ::= <%

 <! Return the plain field or identity name.
    For use in function call names. !>

  <if(expr_obj.isIdentityBitfield)>
    <expr_obj.ident>

  <elseif(expr_obj.isFieldReferenceBitfield)>
   <expr_obj.field>

  <endif>
%>

