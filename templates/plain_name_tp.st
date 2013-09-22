plain_name_tp(expr_obj) ::= <%

 <! Return the plain field or identity name.
    For use in function call names. !>

  <if(expr_obj.isIdentity)>
    <expr_obj.ident>

  <elseif(expr_obj.isIdentityPtr)>
    <expr_obj.ident>

  <elseif(expr_obj.isIdentityEnumerator)>
    <expr_obj.enumName>

  <elseif(expr_obj.isIdentityNumber)>
    <expr_obj.ident>

  <elseif(expr_obj.isIdentityBitfield)>
    <expr_obj.ident>

  <elseif(expr_obj.isFieldReference)>
   <expr_obj.field>

  <elseif(expr_obj.isFieldReferenceBitfield)>
   <expr_obj.field>

  <elseif(expr_obj.isFieldReferencePtr)>
   <expr_obj.field>

  <elseif(expr_obj.isFieldReferenceEnumerator)>
   <expr_obj.field>

  <elseif(expr_obj.isFieldReferenceNumber)>
   <expr_obj.field>

  <else>
   <! <expression_tp(expr_obj)> !>

  <endif>
%>

