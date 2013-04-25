bool func()
{
	// Test declaration in for...
	for (int a5 = 1; int b7 = 5; a5 = 2) { }

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

	int i;

	// Test loops without braces...
	for (i = 0; i < 20; i++)
		i--;

	while (false)
		i -= 10;


	// Test empty for-ever loop...
	for ( ; ; )
	{
	}

	// Test declaration in while...
	while (int* j = 0) { }

	int b = 1;

	// Test while with non java boolean expression...
	while (b) { }

	// Test do with non java boolean expression...
	do {   } while(b);

	// Test if with non java boolean expression...
	if (b) { }

	// Test ternary with non java boolean expression...
	b ? 1 : 0;

	// Test logical and with non java boolean expression...
	b && true;

	// Test assignment with boolean on left and non boolean on the right...
	bool bl;
	bl = b;

	// Test declaration with boolean on the left and non boolean on the right.
	bool bl2 = b;

	// Test return with non boolean return expression...
	return b;

	// Test nested without braces...
	for ( ; ; )
		while (true) ;


	// Test do ... while...
	do
	{
		if (true)
			break;
		else
			continue;
	} while (false);

}



