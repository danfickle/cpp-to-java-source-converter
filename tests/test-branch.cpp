int func()
{
	int b = 0;
	int i = 0;

	// Test if...
	if (b) { }

	// Test declaration in if and empty if...
	if (int c = 3) ;

	// Test if statement without braces...
	if (true)
		i += 5;

	// Test else statement
	if (true) { i -= 5; }
	else { i += 5; }

	// Test else if statement
	if (true) { i++; }
	else if (false) { i--; }
	else if (1 + 2 == 3) { i++; }
	else { i--; }
}

