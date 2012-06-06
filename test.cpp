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
}


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
	foo() { }
	foo(int i) {
		// Test use of anonymous enums values
		// and assigning enum values to int...
		i = value1;
		i = value4;
	}
	foo(int i, short t) {}
	
	// Test a function declaration with default values...
	int func_with_defaults(int one, int two = 5);

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

	// Test an enclosed class...
	class subby
	{
		int k;
	};

	// Test value in class with implicit constructor...
	foo test_foo_with_init;

	// Test value in class with initializer value...
	foo test_foo_with_int_constructor(55);

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
		int i = test_enum::val1 + test_enum::val2;

		// Test various pointer to basic types...
		char * ptr1;
		int ** ptr2;
		short * ptr3;
		unsigned * ptr4;
		bool * ptr5;
		// Test multiple declarators on with one declaration...
		int * ptr7, * ptr6;

		// Test address of operator...
		ptr7 = &a;

		// Test basic pointer assignment...
		ptr7 = ptr6;

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

		// Test -> operator...
		foo6->func_with_defaults_and_definition(100);

		// Test dereference and call...
		(*foo6).func_with_defaults_and_definition(100, 2);

		// Test copy constructor on return...
		return sd[2];
	}

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


};

