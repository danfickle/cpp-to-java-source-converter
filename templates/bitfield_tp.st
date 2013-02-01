bitfield_tp(bitfield_obj) ::= <<
<!
//
// Generates getters and setters for bit fields.
// For example:
//   int test_with_bit_field : 1;
// would generate:
//   int get__test_with_bit_field()
//   {
//     return __bitfields & 1;
//   }
//   int set__test_with_bit_field(int val)
//   {
//     __bitfields &= ~1;
//     __bitfields |= (val << 0) & 1;
//     return val;
//   }
//   int postInc__test_with_bit_field()
//   {
//     int ret = get__test_with_bit_field();
//     set__test_with_bit_field(ret + 1);
//     return ret;
//   }
//   int postDec__test_with_bit_field()
//   {
//     int ret = get__test_with_bit_field();
//     set__test_with_bit_field(ret - 1);
//     return ret;
//   }
//
!>
<bitfield_obj.type> get__<bitfield_obj.name>()
{
  return __bitfields & 1;
}

<bitfield_obj.type> set__<bitfield_obj.name>(final <bitfield_obj.type> val)
{
  __bitfields &= ~1;
  __bitfields |= (val \<\< 0) & 1;
  return val;
}

<bitfield_obj.type> postInc__<bitfield_obj.name>()
{
  int ret = get__<bitfield_obj.name>();
  set__<bitfield_obj.name>(ret + 1);
  return ret;
}

<bitfield_obj.type> postDec__<bitfield_obj.name>()
{
  int ret = get__<bitfield_obj.name>();
  set__<bitfield_obj.name>(ret - 1);
  return ret;
}
>>
