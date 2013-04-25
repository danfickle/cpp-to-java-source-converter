statement_tp(stmt_obj) ::= <<

 <if(stmt_obj.isFor)>
  <if(stmt_obj.decl)>
   <declaration_tp(stmt_obj.decl)>;
  <endif>
   for (<statement_tp(stmt_obj.initializer)> <expression_tp(stmt_obj.condition, false, false)>; <expression_tp(stmt_obj.updater, false, false)>)<\n>
    <statement_tp(stmt_obj.body)>

 <elseif(stmt_obj.isBreak)>
   <statement_tp(stmt_obj.cleanup)><\n>
   break;

 <elseif(stmt_obj.isContinue)>
   <statement_tp(stmt_obj.cleanup)><\n>
   continue;

 <elseif(stmt_obj.isCase)>
  case (<expression_tp(stmt_obj.expr, false, false)>):

 <elseif(stmt_obj.isDefault)>
  default:

 <elseif(stmt_obj.isEmpty)>
  /* Empty statement */;

 <elseif(stmt_obj.isCompound)>
{
    <stmt_obj.statements:{s | <statement_tp(s)><\n>}>
    <statement_tp(stmt_obj.cleanup)>
}

 <elseif(stmt_obj.isGoto)>
  TODO: goto <stmt_obj.lbl>

 <elseif(stmt_obj.isLabel)>
  TODO: label <stmt_obj.lbl><\n>
  <statement_tp(stmt_obj.body)>

 <elseif(stmt_obj.isProblemStmt)>
  TODO: problem <stmt_obj.problem>

 <elseif(stmt_obj.isDeclStmt)>
  <declaration_tp(stmt_obj.simple)>

 <elseif(stmt_obj.isDo)>
  do <\n>
   <statement_tp(stmt_obj.body)>
  while (<expression_tp(stmt_obj.expr, false, false)>);

 <elseif(stmt_obj.isExprStmt)>
  <expression_tp(stmt_obj.expr, false, false)>;

 <elseif(stmt_obj.isIf)>
  if (<expression_tp(stmt_obj.condition, false, false)>)<\n>
   <statement_tp(stmt_obj.body)>
  <if(stmt_obj.elseBody)>
   else <statement_tp(stmt_obj.elseBody)>
  <endif>

 <elseif(stmt_obj.isReturn)>
  <statement_tp(stmt_obj.cleanup)><\n>
  return (<expression_tp(stmt_obj.expr, false, false)>);

 <elseif(stmt_obj.isWhile)>
  while (<expression_tp(stmt_obj.expr, false, false)>)<\n>
   <statement_tp(stmt_obj.body)>

 <elseif(stmt_obj.isSwitch)>
  switch (<expression_tp(stmt_obj.expr, false, false)>)<\n>
   <statement_tp(stmt_obj.body)>

 <elseif(stmt_obj.isSuperStmt)>
  super();

 <elseif(stmt_obj.isSuperDtorStmt)>
  super.destruct();

 <elseif(stmt_obj.isSuperAssignStmt)>
  super.opAssign(right);

 <elseif(!stmt_obj)>

 <else>
  ERROR: <stmt_obj.toString>

<endif>
>>
