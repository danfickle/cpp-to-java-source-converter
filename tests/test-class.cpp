class foo
{
public:
	// Test explicity defining special methods...
	foo() { }
	~foo() { }

	// Test copy ctor...
	foo(const foo& right)
	{
		int i = 10;
	}

	foo(int i, short t) {}

	// Test static fields...
	static int static_int;
};

// Test a class with a parent class...
class test : public foo
{
	// Test an anonymous enclosed class...
	class { int a, b; } anon_class1, anon_class2;
	class { int c, d; } anon_class3, anon_class4;

	// Test an enclosed class...
	class subby
	{
		int k;
	};

	// Test constructor...
	test()
	{
		int i = 10;
	}

	test(int i)
	{
		int j = i;
	}

	// Test destructor...
	~test()
	{
		int i = 55;
	}
};

// Test a class with a base class and no constructor...
class test2 : private foo
{
	int i;
};

class test3 : public foo
{
	// Test a constructor where the base class ctor is called with arguments...
	test3() : foo(1, 2) { }
};


