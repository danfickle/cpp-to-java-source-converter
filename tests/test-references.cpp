int& ref_test_method(short& b)
{
	int a;
	short c = 20;
	int & d = a;

	a = 30;
	d = 40;

	ref_test_method(c);

	return a;
}


