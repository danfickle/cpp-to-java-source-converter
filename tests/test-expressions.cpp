enum bb { aa, ii };

class cls
{
  int _a;
  int _b : 8;
  int * _c;
  //bb _d;

int func(int a)
{
  int arr[2];
  int * ptr;
  int myint;

  arr[1];   // 1  -> Plain array access
  ptr[2];   // 2  -> Pointer array access

  _a;       // 7  -> Id
  _b;       // 8  -> Bitfield id
  ptr;      // 9  -> Pointer id
  aa;       // 10 -> Enumerator id

  _a ? _b : 10; // 11 -> Ternary

  this._a;  // 12 -> Field reference
  this._b;  // 13 -> Field reference bitfield
  this->_c; // 14 -> Field reference dereference pointer
  //this._d;  // 15 -> Field reference enum

  10;     // 17 -> Literal
  ++_a;   // 18 -> Prefix plain
  _a++;   // 21 -> Postfix plain
  ++ptr;  // 25 -> Ptr prefix increment
  --ptr;  // 26 -> Ptr prefix decrement
  ptr++;  // 23 -> Ptr postfix increment
  ptr--;  // 24 -> Ptr postfix decrement
  *ptr;   // 27 -> Ptr prefix dereference
  _b++;   // 28 -> Bitfield postfix increment
  _b--;   // 29 -> Bitfield postfix decrement
  ~_b;    // 31 -> Bitfield prefix
  ++_b;   // 33 -> Bitfield prefix increment
  --_b;   // 34 -> Bitfield prefix decrement

  this._b++; // -> Bitfield field reference postfix increment
  this._b--; // -> Bitfield field reference postfix decrement

  ++this._b; // -> Bitfield field reference prefix increment
  --this._b; // -> Bitfield field reference prefix decrement
  ~this._b;  // -> Bitfield field reference prefix

  myint = 18;  // -> Plain assignment
  myint | 13;  // -> Plain infix
  myint += 14; // -> Plain compound

  arr[1] = 17;  // -> Plain array assignment
  arr[1] | 15;  // -> Plain array infix
  arr[2] -= 16; // -> Plain array compound

  _b = 10;    // -> Bitfield assignment
  _b | 11;    // -> Bitfield infix
  _b += 12;   // -> Bitfield compound assignment

  this._a = 19;  // -> Field reference assignment
  this._a ^ 20;  // -> Field reference infix
  this._a *= 21; // -> Field reference compound

  this._b = 12;  // -> Bitfield field reference assignment
  this._b ^ 23;  // -> Bitfield field reference infix
  this._b *= 24; // -> Bitfield field reference compound








  ptr[2] = 2; // -> Pointer array assignment


  //int zz = (int) _b;


/*
  a + a - a;
  a++;
  --a;
  a + ii;

  a ? a : ii;

  int * b = &a;
  b;
  _a;
  this._a;
  *b;
  _b;
  _b++;
  _b--;
  
  this._b++;
  this._b--;
*/

}

};
