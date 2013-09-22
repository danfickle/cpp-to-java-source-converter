plain_object_tp(expr_obj) ::= <%

  <if(expr_obj.isFieldReference)>
    <really_plain_object_tp(expr_obj.object)>.

  <elseif(expr_obj.isFieldReferenceBitfield)>
    <really_plain_object_tp(expr_obj.object)>.

  <elseif(expr_obj.isPtrArray)>
   <expression_tp(expr_obj.operand)>.ptrOffset(<expr_obj.subscript:{s | <expression_tp(s)>}; separator=", ">).

  <elseif(expr_obj.isPrefixPointerStar)>
    <expression_tp(expr_obj.operand)>.

  <elseif(expr_obj.isFieldReferencePtr)>
    <expression_tp(expr_obj.object)>.<expr_obj.field>.

  <else>
    /* 2 */

  <endif>
%>

