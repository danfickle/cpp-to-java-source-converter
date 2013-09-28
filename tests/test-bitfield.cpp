class A
{
public:
	// Test bit fields...
	int test_with_bit_field : 5;

	A() : test_with_bit_field(8) { }

	int func2()
	{
		// Test setting of bit field...
		test_with_bit_field = 8;

		test_with_bit_field += 3;
		test_with_bit_field *= 4;

		test_with_bit_field++;
		test_with_bit_field--;

		--test_with_bit_field;
		int i = ++test_with_bit_field + 33;

		// Test getting of bit field...
		return test_with_bit_field;
	}
};

int func3()
{
	A a;
	a.test_with_bit_field++;
	a.test_with_bit_field += 3;

	A * b = new A;
	b->test_with_bit_field++;
	b->test_with_bit_field += 5;

	return a.test_with_bit_field + b->test_with_bit_field;
}

