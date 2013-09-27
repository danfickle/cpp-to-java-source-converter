int func(int a);

int func(int a, int b)
{
  func(1, 2);
  func(0);
}

int func(int a)
{
  return a;
}

