declaration_tp(decl) ::= <%

 <if(decl.isEnum)>
  <\n><enum_tp(decl.name, decl.enumerators, "int")>

 <elseif(decl.isBitfield)>
  <\n><bitfield_tp(decl)>

 <else>
  <decl>

 <endif>

%>

