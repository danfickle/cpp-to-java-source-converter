int func(int a1)
{
  int a;
  short b = 10;

  a = b + b;
  a++;
  a--;
  --a;
  ++a;
  b += 10;
  &a;
  a = -b;
  func(b);
  return a;
}

