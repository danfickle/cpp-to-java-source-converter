for_tp(for_obj) ::= <<
for ( <initializer_tp(for_obj.initializer)>; <expression_tp(for_obj.condition)>; <expression_tp(for_obj.updater)> )
{
  <for_obj.body.statements:{s | <s.text><\n>}>
}
>>
