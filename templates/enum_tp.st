enum_tp(name, enumerators, type) ::= <<
<!
//
// Sample Output:
// enum my_enum
// {
//  one(1), 
//  two(2), 
//  three(3);
//
//  final int val;
//
//  my_enum(final int enumVal)
//  {
//    val = enumVal;
//  }
//
//  static my_enum fromValue(final int enumVal)
//  {
//     switch (enumVal)
//     {
//       case (1): return one;
//       case (2): return two;
//       case (3): return three;
//
//       default: throw new ClassCastException();
//     }
//  }
//}
//
!>
enum <name>
{
  <enumerators:{e | <e.name>(<e.value>)}; separator=", \n">;

  final <type> val;

  <name>(final <type> enumVal)
  {
    val = enumVal;
  }
 
  static <name> fromValue(final <type> enumVal)
  {
     switch (enumVal)
     {
       <enumerators:{e | case (<e.value>): return <e.name>;<\n>}>
       default: throw new ClassCastException();
     }
  }
}
>>
