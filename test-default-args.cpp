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

