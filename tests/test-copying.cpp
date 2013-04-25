int * func1()
{
	int b = 5;
	return &b;
}

int * func2()
{
	int * b = 0;
	return b;
}

void func3(int * c)
{
	func3(c);
}

void func4(int d)
{
	int i = 0, j;
	func4(i);
	func3(&i);

	j = 5;
	j = i;

	func4(i + 5);
	func4(i++);
	func4(--i);
	func4(i *= 3);
}

