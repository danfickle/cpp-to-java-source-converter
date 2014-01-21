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

	// Binary method
 	OpTest operator /(int b) const
	{
		return OpTest(j - b);
	}

	// Unary method
	OpTest operator -()
	{
		return OpTest(-j);
	}

	// Function call method
	void operator ()()
	{
		j++;
	}

	int operator ()(int kk, int ll)
	{
		kk++;
		ll++;
	}

	// Prefix increment
	int operator++()
	{
		j++;
	}
	
	// Postfix increment (with dummy int to mark it as postfix)
	int operator++(int)
	{
		j++;
	}

	OpTest& operator [](int a)
	{
		return *this;
	}

	void operator delete(void* ptr)
	{
	}
};

/* TODO: Uncomment when we have figured out function/method conflict.
void operator delete(void * ptr)
{
}
*/

void operator delete[](void * ptr)
{

}

// Binary function
OpTest operator +(const OpTest& a, const OpTest& b)
{
	return OpTest(a.j + b.j);
}

// Binary function (with built-in type on left)
OpTest operator +(int a, const OpTest& b)
{
	return OpTest(a + b.j);
}

// Unary function
OpTest operator +(const OpTest& a)
{
	return OpTest(-a.j);
}

// Pre decrement
int operator--(OpTest& b)
{
	b.j--;
}

// Post decrement
int operator--(OpTest& b, int)
{
	b.j--;
}


void OpTestTest()
{
	OpTest a(1);
	OpTest b(2);
	OpTest c = a + b;
	OpTest d = b - a;
	OpTest e = +a;
	OpTest f = -a;
	OpTest g = 5 + b;
	OpTest h = b / 2;

	++f;
	f++;

	--g;
	g--;

	g[12];

	f();
	f(1, 2);

	OpTest * ptr = new OpTest(1);
	delete ptr;

	int * ptr2 = new int(3);
	delete ptr2;

	int * ptr3 = new int[24];
	delete[] ptr3;
}

