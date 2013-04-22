int func(int b)
{
	for (b; b < 10; b++)
	{
		b++;
		--b;
	}

	for ( ; ; )
	{
		break;
	}

	for ( ; ; )
		continue;

test:
	switch(b)
	{
		case 1:
		case 1 + 2:
		{
			b++;
		}
		default:
			break;
	}
goto test;

	do {
		b++;
	} while (b < 10);

	b += 10;

	while (b > 0)
	{
		b--;
	}


	for( ; ; )
		;

	while (true)
		;

	do 
		;
	while (true);


	return 5 + 2;
}

