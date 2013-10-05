#include "test-include2.hpp"

int global_var;
int global_func()
{
  return 32;
}

global_cls::global_cls() 
{
  int i = 42;
}

int global_cls::meth(int i) {
  return 54;
}

