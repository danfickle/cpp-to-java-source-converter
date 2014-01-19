enum bb { aa, ii };

int global_func(int a, int b) {}
int global_func(int a) {}

class cls
{
  int _a;
  int _b : 8;
  int * _c;
  bb _d;

  cls() { }
  cls(int a, int b) {}

  int mymethod(int a, cls b) {}

int func(int a)
{
  int arr[2];
  int * ptr;
  int myint;
  cls myclass;
  cls * ptr_cls;

  arr[1];   // 1  -> Plain array access
  ptr[2];   // 2  -> Pointer array access

  _a;       // 7  -> Id
  _b;       // 8  -> Bitfield id
  ptr;      // 9  -> Pointer id
  aa;       // 10 -> Enumerator id

  _a ? _b : 10; // 11 -> Ternary

  this->_a;  // 12 -> Field reference
  this->_b;  // 13 -> Field reference bitfield
  this->_c; // 14 -> Field reference dereference pointer
  this->_d; // 15 -> Field reference enum

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

  this->_b++; // -> Bitfield field reference postfix increment
  this->_b--; // -> Bitfield field reference postfix decrement

  ++this->_b; // -> Bitfield field reference prefix increment
  --this->_b; // -> Bitfield field reference prefix decrement
  ~this->_b;  // -> Bitfield field reference prefix

  myint = 18;  // -> Plain assignment
  myint | 13;  // -> Plain infix
  myint += 14; // -> Plain compound

  arr[1] = 17;  // -> Plain array assignment
  arr[1] | 15;  // -> Plain array infix
  arr[2] -= 16; // -> Plain array compound

  _b = 10;    // -> Bitfield assignment
  _b | 11;    // -> Bitfield infix
  _b += 12;   // -> Bitfield compound assignment

  this->_a = 19;  // -> Field reference assignment
  this->_a ^ 20;  // -> Field reference infix
  this->_a *= 21; // -> Field reference compound

  this->_b = 12;  // -> Bitfield field reference assignment
  this->_b ^ 23;  // -> Bitfield field reference infix
  this->_b *= 24; // -> Bitfield field reference compound

  myclass._b = 25; // -> Field reference bitfield

  ptr[2] = 2;  // -> Pointer array assignment
  ptr[2] | 3;  // -> Pointer array infix
  ptr[2] += 4; // -> Pointer array compound

  *ptr = 2;  // -> Pointer deref assignment
  *ptr | 3;  // -> Pointer deref infix
  *ptr += 4; // -> Pointer deref compound

  (*this)._a = 2;  // -> Field deref assignment
  (*this)._a | 3;  // -> Field deref infix
  (*this)._a += 4; // -> Field deref compound

  this->_a = 2;  // -> Field deref assignment
  this->_a | 3;  // -> Field deref infix
  this->_a += 4; // -> Field deref compound

  delete ptr;           // -> Basic type delete
  delete ptr_cls;       // -> Object type delete
  delete [] ptr_cls;    // -> Object array delete

  ptr = new int[10];        // -> New expression basic array
  ptr_cls = new cls[11];    // -> New expression object
  ptr = new int;            // -> New expression single basic
  ptr = new int(23);        // -> New expression single basic with arg
  ptr_cls = new cls;        // -> New expression single object
  ptr_cls = new cls(1, 2);  // -> New expression single object with args

  global_func(100);                // -> Function call expression
  global_func(101, 102);           // -> Function call expression with multiple args
  mymethod(103, myclass);          // -> Id method call
  this->mymethod(105, cls(100, 101));   // -> Field reference pointer function call
  myclass.mymethod(107, cls(200, 201)); // -> Field reference object function call

  _d = (bb) 1;       // Cast to enum
  _b = (short) 5;    // Cast to bitfield
  short shrt = (short) _a;  // Cast to number


  // TODO: Overloaded operators, comma expression, bracketed expressions.
}

};

