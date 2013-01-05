class A
{
	// Test bit fields...
	int test_with_bit_field : 5;

	int func2()
	{
		// Test setting of bit field...
		test_with_bit_field = 8;

		test_with_bit_field++;
		--test_with_bit_field;

		// Test getting of bit field...
		return test_with_bit_field;
	}
};


