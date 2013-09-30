enum foo_baz
{
  one, two, three
};

class baz_far
{

};

class foo_bar
{
  int i;     // Test basic type.
  long m[4]; // Test basic array
  int * p;   // Test basic ptr
  foo_baz j; // Test enum
  short k:5; // Test bitfield
  baz_far o; // Test object
  baz_far h[8]; // Test object array
  baz_far * r;  // Test object ptr

  // TODO: ptr to ptr, function ptr.
};

