class OpTest
{
public:
	int j;
	OpTest(int i) : j(i) { }

	// Binary method
 	OpTest operator -(const OpTest& b) const
	{
		return OpTest(j - b.j);
	}

	// Unary method
	OpTest operator -()
	{
		return OpTest(-j);
	}

	// Function call method
	void operator()
	{
		j++;
	}

};

// Binary function
OpTest operator +(const OpTest& a, const OpTest& b)
{
	return OpTest(a.j + b.j);
}

// Unary function
OpTest operator +(const OpTest& a)
{
	return OpTest(-a.j);
}


void OpTestTest()
{
	OpTest a(1);
	OpTest b(2);
	OpTest c = a + b;
	OpTest d = b - a;
	OpTest e = +a;
	OpTest f = -a;
	f();
}

