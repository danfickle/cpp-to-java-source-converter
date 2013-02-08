declaration_tp(decl) ::= <%

 <if(decl.isEnum)>
   <enum_tp(decl.simpleName, decl.enumerators, "int")>

 <elseif(decl.isBitfield)>
   <bitfield_tp(decl)>

 <endif>

%>

