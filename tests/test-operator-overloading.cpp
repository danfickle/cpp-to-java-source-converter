// Test global operator overloading...
class OpTest
{
public:
	int j;
	OpTest(int i) : j(i) { }
 	OpTest operator -(const OpTest& b) const
	{
		return OpTest(j - b.j);
	}

	OpTest operator -()
	{
		return OpTest(-j);
	}

};

OpTest operator +(const OpTest& a, const OpTest& b)
{
	return OpTest(a.j + b.j);
}

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
}

