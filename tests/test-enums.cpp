// Test anonymous global enums...
enum { value1, value2 };
enum { value3, value4 };

int func()
{
	int i;

	// Test use of anonymous enums values
	// and assigning enum values to int...
	i = value1;
	i = value4;
}

class test
{
	// Test an enclosed enumeration...
	enum test_enum
	{
		val1,
		// Test giving enumerators values...
		val2 = 1020,
		val3,
		val4 = 1045
	};

	// Test an enclosed anonymous enumeration...
	enum { val5, val6 };

	int meth()
	{
		// Test qualified enum access...
		int i = test::val1 + test::val2;
		int j = val5 + val6;
	}
};

