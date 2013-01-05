int func()
{
	int d = 2;

	// Test switch...
	switch (d)
	{
		// Test fall through...
		case 1:
		case 2:
			break;
		default:
			return 6;
	}


	// Test switch with declaration statement...
	switch (int decl = 1)
	{
		case 1:
			decl += 5;
			break;
	}


	// Switch in loop...
	while (true)
	{
		switch(5)
		{
			case 7:
				break;
			case 9:
				continue;
		}
	}
}

