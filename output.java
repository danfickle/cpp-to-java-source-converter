package yourpackage;

class Globals {
	class class_outside_namespace implements HasDestructor {
		byte a;
		char b;
		short c;
		int d;
		int e;
		long f;
		int g;
		short h;
		long i;
	}

	class_outside_namespace cls1 = new class_outside_namespace();
	int variable;
	AnonClass3 anon_class3 = new AnonClass3();
}

class CppString implements HasDestructor {
	int m_count;

	public CppString(int i) {
		m_count = i;
	}
}

class mypair<T> implements HasDestructor {
	T[] values;

	public mypair(T first, T second) {
		values[0].op_assign(first);
		values[1].op_assign(second);
	}
}

class HashMap<T, V> implements HasDestructor {
	T one = new T();
	V two = new V();
}

class CT<V, T> implements HasDestructor {
	HashMap<V, Integer> three = new HashMap<V, Integer>();
	T one = new T();
	V two = new V();
}

enum AnonEnum0 {
	value1(0), value2(1);
	final int val;

	AnonEnum0(final int enumVal) {
		val = enumVal;
	}

	static AnonEnum0 fromValue(final int enumVal) {
		switch (enumVal) {
		case 0:
			return value1;
		case 1:
			return value2;
		default:
			throw new ClassCastException();
		}
	}
}

enum AnonEnum1 {
	value3(0), value4(1);
	final int val;

	AnonEnum1(final int enumVal) {
		val = enumVal;
	}

	static AnonEnum1 fromValue(final int enumVal) {
		switch (enumVal) {
		case 0:
			return value3;
		case 1:
			return value4;
		default:
			throw new ClassCastException();
		}
	}
}

class foo implements HasDestructor {
	public foo() {
	}

	public void destruct() {
	}

	public foo(int i) {
		i = AnonEnum0.value1.val;
		i = AnonEnum1.value4.val;
	}

	public foo(int i, short t) {
	}

	public int func_with_defaults1(int one) {
		return func_with_defaults1(one, 5);
	}

	public void func_with_defaults2() {
		func_with_defaults2(7);
	}

	public int func_with_defaults_and_definition(int one, int two) {
		two = 4;
		return two;
	}

	public int func_with_defaults_and_definition(int one) {
		return func_with_defaults_and_definition(one, 3);
	}
}

class test extends foo implements HasDestructor {
	enum test_enum {
		val1(0), val2(1020), val3((1020) + 1), val4(1045);
		final int val;

		test_enum(final int enumVal) {
			val = enumVal;
		}

		static test_enum fromValue(final int enumVal) {
			switch (enumVal) {
			case 0:
				return val1;
			case 1020:
				return val2;
			case (1020) + 1:
				return val3;
			case 1045:
				return val4;
			default:
				throw new ClassCastException();
			}
		}
	}

	class AnonClass0 implements HasDestructor {
		int a;
		int b;
	}

	AnonClass0 anon_class1 = new AnonClass0();
	AnonClass0 anon_class2 = new AnonClass0();

	class AnonClass1 implements HasDestructor {
		int c;
		int d;
	}

	AnonClass1 anon_class3 = new AnonClass1();
	AnonClass1 anon_class4 = new AnonClass1();

	class subby implements HasDestructor {
		int k;
	}

	foo test_foo_with_init = new foo();

	public int get__test_with_bit_field() {
		return __bitfields & 1;
	}

	public void set__test_with_bit_field(int val) {
		__bitfields &= ~1;
		__bitfields |= (val << 0) & 1;
	}

	public int func2() {
		test_with_bit_field = 8;
		return test_with_bit_field;
	}

	public int func3(foo a,foo b,Ptr<foo> c,foo d,foo e,RefInteger f){
		foo j;
		while ((j=b) != null){}
		foo l;if ((l=b) != null){}
		for (foo a5=b;(a5) != null;a5=b){}
		while ((b) != null){}if ((b) != null){}
		for (int i=0;i < 10;i++){
			for (i=1;i < 5;i++){
				break;
			}
			continue;
		}
		int i=test_enum.val1.val + test_enum.val2.val;
		PtrByte ptr1;
		Ptr<int> ptr2;
		PtrShort ptr3;
		PtrInteger ptr4;
		PtrBoolean ptr5;
		PtrInteger ptr6;
		foo ptr7;
		foo ptr8;
		ptr7=a;
		ptr8=ptr7;
		return 1072;
	}
	
	public foo func4(int a, int b, short c) {
		foo[] sd = new foo[15];
		for (int gen___i0 = 0; gen___i0 < sd.length; gen__i0++) {
			sd[gen___i0] = new foo();
		}
		foo foo1;
		Ptr<foo> foo2;
		foo foo3 = new foo(sd[1]);
		foo foo4 = new foo(1, 2);
		foo foo5 = new foo(foo4);
		foo foo6 = this;
		foo foo7 = new foo(this);
		int d = (1 + 2) * 4;
		boolean mybool1 = (foo1) != null && (foo6) != null && true;
		boolean mybool2 = (foo1) != null || (foo6) != null || false;
		func2();
		d += 10;
		switch (d) {
		case 1:
		case 2:
			break;
		default:
			return new foo(foo7);
		}
		int decl;
		switch (decl = 1) {
		case 1:
			decl += 5;
			break;
		}
		foo6.func_with_defaults_and_definition(100);
		(foo6).func_with_defaults_and_definition(100, 2);
		return new foo(sd[2]);
	}

	public foo func4(int a, int b) {
		return func4(a, b, 55);
	}

	public void func5() {
		int i = 0;
		if (true)
			i += 5;
		while (false)
			i -= 10;
		for (i = 0; i < 20; i++)
			i--;
		switch (i) {
		case 1:
			return;
		case 2:
			i++;
		}
		if (true)
			for (;;)
				while (true)
					;
		for (int k = 0; (k < 10) != 0; k++)
			k *= 10;
		foo ptr1 = new foo();
		foo ptr2 = new foo(100);
		foo ptr3 = new foo();
		DestructHelper.destructItems(ptr1);
		DestructHelper.destructItems(ptr2);
		DestructHelper.destructArray(ptr3);
	}

	/**
	 * @union
	 */
	class test_union implements HasDestructor {
		int a;
		float b;
	}

	class test_struct implements HasDestructor {
		int a;
		int b;
	}
}

/**
 * @union
 */
class test_union2 implements HasDestructor {
	short a;
	short b;
}

class AnonClass2 implements HasDestructor {
	class AnonClass3 implements HasDestructor {
		int a;
	}

	AnonClass3 anon_class1 = new AnonClass3();
	AnonClass3 anon_class2 = new AnonClass3();
}
