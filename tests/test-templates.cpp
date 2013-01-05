// Test basic template class...
template <class T>
class mypair {
	// Test array of template parameter types...
	T values [2]; 
	// Test access modifier...
	public:
	// Test constructor using template parameter types...
	mypair (T first, T second)
	{
		// Test array access...
		values[0] = first;
		values[1] = second;
	}
};

// Test template class with two parameters...
template <class T, class V>
class HashMap
{
	T one;
	V two;
};

template<class V, class T> class CT
{
	// Test using template types in as template parameters...
	HashMap<V, int> three;
	T one;
	V two;

};

