initializer_tp(init_obj) ::= <%
<if(init_obj.hasDeclaration)>
  <variable_declaration_tp(init_obj.declaration)> 
<endif>
<if(init_obj.expression && init_obj.hasDeclaration)>
 = 
<endif>
<if(init_obj.expression)>
  <expression_tp(init_obj.expression)>
<endif>
%>
