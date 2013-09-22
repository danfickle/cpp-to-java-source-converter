plain_object_tp(expr_obj) ::= <%

  <if(expr_obj.isFieldReference)>
    <really_plain_object_tp(expr_obj.object)>.

  <elseif(expr_obj.isFieldReferenceBitfield)>
    <really_plain_object_tp(expr_obj.object)>.

  <elseif(expr_obj.isPtrArray)>
    <really_plain_object_tp(expr_obj.operand)>.

  <elseif(expr_obj.isPrefixPointerStar)>
    <really_plain_object_tp(expr_obj.operand)>.

  <elseif(expr_obj.isFieldReferencePtr)>
    <really_plain_object_tp(expr_obj.object)>.

  <else>
    /* 2 */

  <endif>
%>

