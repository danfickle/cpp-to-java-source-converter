// Test function outside namespace...
int func_outside_namespace();

// Test class outside namespace...
class class_outside_namespace
{
	// Test basic types...
	char a;
	wchar_t b;
	short c;
	int d;
	long e;
	long long f;
	unsigned g;
	unsigned short h;
	unsigned long long i;
};

// Test variables outside namespace...
class_outside_namespace cls1;
int variable;

// Test a namespace...
namespace mynamespace
{
// Test class with a reserved Java name...
class String
{
	int m_count;

	// Test constructor initializer lists...
	String(int i) : m_count(i) { }
};


// Test basic template class...
template <class T>
class mypair {
	// Test array of template parameter types...
	T values [2]; 
	// Test access modifier...
	public:
	// Test constructor using template parameter types...
	mypair (T first, T second)
	{
		// Test array access...
		values[0] = first;
		values[1] = second;
	}
};

// Test template class with two parameters...
template <class T, class V>
class HashMap
{
	T one;
	V two;
};

template<class V, class T> class CT
{
	// Test using template types in as template parameters...
	HashMap<V, int> three;
	T one;
	V two;

};

// Test anonymous enums...
enum { value1, value2 };
enum { value3, value4 };

class foo
{
public:
	foo() { }
	~foo() { }

	foo(int i) {
		// Test use of anonymous enums values
		// and assigning enum values to int...
		i = value1;
		i = value4;
	}
	foo(int i, short t) {}
	
	// Test a function declaration with default values...
	int func_with_defaults1(int one, int two = 5);

	// Test a function declaration with one default value and void return...
	void func_with_defaults2(int x = 7);

	// Test a function definition with default values...
	int func_with_defaults_and_definition(int one, int two = 3)
	{
		two = 4;
		// Test a return statement...
		return two;
	}
};

// Test a class with a parent class...
class test : public foo {

	// Test an enclosed enumeration...
	enum test_enum
	{
		val1,
		// Test giving enumerators values...
		val2 = 1020,
		val3,
		val4 = 1045
	};

	// Test an anonymous enclosed class...
	class { int a, b; } anon_class1, anon_class2;
	class { int c, d; } anon_class3, anon_class4;

	// Test an enclosed class...
	class subby
	{
		int k;
	};

	// Test value in class with implicit constructor...
	foo test_foo_with_init;

	// Test bit fields...
	int test_with_bit_field : 5;

	int func2()
	{
		// Test setting of bit field...
		test_with_bit_field = 8;
		// Test getting of bit field...
		return test_with_bit_field;
	}

	// Test function for pointers and references...
	int func3(foo a, foo * b, foo ** c, foo& d, const foo& e, int& f)
	{
		// Test declaration in while...
		while (foo* j = b) { }

		// Test declaration in if...
		if (foo * l = b) { }

		// Test declaration in for...
		for (foo * a5 = b;a5;a5 = b) { }

		// Test while...
		while (b) { }

		// Test if...
		if (b) { }

		// Test standard looking for...
		for (int i = 0; i < 10; i++) 
		{
			// Test nested for...
			for (i = 1; i < 5; i++)
			{
				// Test break statement...
				break;
			}
			// Test continue statement...
			continue;
		}

		// Test qualified enum access...
		int i = test::val1 + test::val2;

		// Test various pointer to basic types...
		char * ptr1;
		int ** ptr2;
		short * ptr3;
		unsigned * ptr4;
		bool * ptr5;
		int * ptr6;
		// Test multiple declarators with one declaration...
		foo * ptr7, * ptr8;

		// Test address of operator...
		ptr7 = &a;

		// Test basic pointer assignment...
		ptr8 = ptr7;

		// Test number literals...
		return 1072;
	}

	foo func4(int a, int b, short c = 55)
	{
		// Test implicity constructors with array...
		foo sd[15];

		// Test pointers to composite type...
		foo * foo1;
		foo ** foo2;

		// Test references to composite type...
		foo& foo3 = sd[1];

		// Test constructor call...
		foo foo4(1, 2);

		// Test implicity copy constructor call...
		foo foo5 = foo4;

		// Test pointer assignment from this...
		foo * foo6 = this;

		// Test copy from this...
		foo foo7 = *this;

		// Test bracketed expression...
		int d = (1 + 2) * 4;

		// Test && with non boolean expression...
		bool mybool1 = foo1 && foo6 && true;

		// Test || with non boolean expression...
		bool mybool2 = foo1 || foo6 || false;

		// Test function call...
		func2();

		// Test +=...
		d += 10;

		// Test switch...
		switch (d)
		{
			// Test fall through...
			case 1:
			case 2:
				break;
			default:
				return foo7;
		}

		// Test switch with declaration statement...
		switch (int decl = 1)
		{
			case 1:
				decl += 5;
				break;
		}

		// Test -> operator...
		foo6->func_with_defaults_and_definition(100);

		// Test dereference and call...
		(*foo6).func_with_defaults_and_definition(100, 2);

		// Test copy constructor on return...
		return sd[2];
	}

	void func5()
	{
		int i = 0;

		// Test condition statements without braces...
		if (true)
			i += 5;

		while (false)
			i -= 10;

		for (i = 0; i < 20; i++)
			i--;

		switch (i)
		{
			case 1:
			return;
			case 2:
			i++;
		}

		if (true)
			for ( ; ; )
				while (true) ;

		// Test for statement with comma operator...
		for (int k = 0; k < 10;k++)
			k *= 10;

		// Test new statement...
		foo * ptr1 = new foo;
		foo * ptr2 = new foo(100);
		foo * ptr3 = new foo[100];

		// Test delete statement...
		delete ptr1;
		delete ptr2;
		delete [] ptr3;

		// Test arrays of basic types on the stack...
		int basic[100];
		short basic2[5][10];

		if (false)
			// Test return statement for void function...
			return;

		// Test arrays of composite types on the stack...
		foo foos_array[45 + 2];
		foo foos_array2[50][20];

		// Test a new statement with variable declaration...
		int * p = new int[2], r;
	}

	void func6(foo a)
	{
		// Test stack object creation in method argument...
		func6(foo(1));

		// Test destructor calls at end brace...
		{
			foo foo_bar;
		}
		
		if (true)
		{
			foo foo_bar;
		}

		while (true)
		{
			foo foo_bar;
		}

		switch (1)
		{
			case 1:
			{
				foo foo_bar;
			}
		}

		do
		{
			foo foo_bar;
		} while (false);

		// Nested...
		if (true)
		{
			foo foo_bar;

			while (true)
			{
				foo foo_baz;

				for ( ; ; )
				{
					foo foo_bug;
				}
			}
		}
	}


	void func7()
	{
		// Test destructor calls on break and continue...
		for (int i = 0; i < 10; i++)
		{
			foo bar;

			if (false)
				break;

			if (true)
			{
				continue;
			}
		}

		// Nothing to cleanup...
		for (int i = 0; i < 20; i++)
		{
			if (true)
				break;
			else
				continue;
		}

		// Pre-existing items on stack...
		foo baz, bug, dog;
		for (int i = 0; i < 5; i++)
		{
			foo house;
			if (false)
			{
				foo car;
				break;
			}
			else
				continue;
		}

		// Nested loops...
		while (true)
		{
			foo honda;
			do
			{
				foo ferrari;

				if (true)
					break;
				else
					continue;
			} while (false);
		}

		// Test creating items with no variable and in function arguments...
		foo(1);
		func6(foo());

		// Test creating items as a return value from a function...
		func8();
	}

	foo func8()
	{
		return foo();
	}

	// Test arrays as class fields...
	foo foo_bar_array[10];
	foo foo_baz_array[10][25];
	int basic_array[1];
	int not_so_basic_array[5][7];

	// Test object at class level...
	foo foo_bar;

	// Test a union...
	union test_union
	{
		int a;
		float b;
	};

	// Test a struct...
	struct test_struct
	{
		int a;
		int b;
	};
};

// Test a namepace level union...
union test_union2
{
	short a, b;
};

// Test an anonymous class containing an anonymous class...
class
{
	class { int a; } anon_class1, anon_class2;
} anon_class3;

};

using namespace mynamespace;
// Test top level objects and arrays...
foo foo_bar_top_level;
foo foo_bar_array_top_level1[10];
foo foo_bar_array_top_level2[10][40];
int array_top_level1[10];
char array_top_level2[20][5];

// Test initializing variables...
int top_level_int = 5;
foo top_level_foo(3);
int * top_level_ptr = new int;
int * top_level_ptr2 = new int[25];

// Tests TODO
// comma operator.
//



