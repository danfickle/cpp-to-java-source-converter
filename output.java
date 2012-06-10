package yourpackage;

class Globals {
	class class_outside_namespace implements CppType {
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

	class_outside_namespace cls1 = StackHelper.addItem(
			new class_outside_namespace(), 1, __stack);
	int variable;
	AnonClass3 anon_class3 = StackHelper.addItem(new MISSING(), 5, __stack);
	foo foo_bar_top_level = StackHelper.addItem(new foo(), 6, __stack);
	foo[] foo_bar_array_top_level1 = StackHelper.addItem(
			CreateHelper.allocateArray(foo.class, 10), 7, __stack);
	foo[][] foo_bar_array_top_level2 = StackHelper.addItem(
			CreateHelper.allocateArray(foo.class, 10, 40), 8, __stack);
	int[] array_top_level1 = new int[10];
	byte[][] array_top_level2 = new byte[20][5];
	int top_level_int = 5;
	foo top_level_foo = StackHelper.addItem(new foo(3), 9, __stack);
	PtrInteger top_level_ptr = 0;
	PtrInteger top_level_ptr2 = new int[25];
}

class CppString implements CppType {
	int m_count;

	CppString(int i) {
		m_count = i;
	}
}

class mypair<T> implements CppType {
	T[] values = StackHelper.addItem(CreateHelper.allocateArray(T.class, 2), 0,
			__stack);

	mypair(T first, T second) {
		values[0].op_assign(first);
		values[1].op_assign(second);
	}
}

class HashMap<T, V> implements CppType {
	T one = StackHelper.addItem(new T(), 0, __stack);
	V two = StackHelper.addItem(new V(), 1, __stack);
}

class CT<V, T> implements CppType {
	HashMap<V, Integer> three = StackHelper.addItem(new HashMap<V, Integer>(),
			2, __stack);
	T one = StackHelper.addItem(new T(), 3, __stack);
	V two = StackHelper.addItem(new V(), 4, __stack);
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

class foo implements CppType {
	foo() {
	}

	void destruct() {
	}

	foo(int i) {
		i = AnonEnum0.value1.val;
		i = AnonEnum1.value4.val;
	}

	foo(int i, short t) {
	}

	int func_with_defaults1(int one) {
		return func_with_defaults1(one, 5);
	}

	void func_with_defaults2() {
		func_with_defaults2(7);
	}

	int func_with_defaults_and_definition(int one, int two) {
		two = 4;
		return two;
	}

	int func_with_defaults_and_definition(int one) {
		return func_with_defaults_and_definition(one, 3);
	}
}

class test extends foo implements CppType {
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

	class AnonClass0 implements CppType {
		int a;
		int b;
	}

	AnonClass0 anon_class1 = StackHelper.addItem(new MISSING(), 0, __stack);
	AnonClass0 anon_class2 = StackHelper.addItem(new MISSING(), 1, __stack);

	class AnonClass1 implements CppType {
		int c;
		int d;
	}

	AnonClass1 anon_class3 = StackHelper.addItem(new MISSING(), 2, __stack);
	AnonClass1 anon_class4 = StackHelper.addItem(new MISSING(), 3, __stack);

	class subby implements CppType {
		int k;
	}

	foo test_foo_with_init = StackHelper.addItem(new foo(), 4, __stack);

	int get__test_with_bit_field() {
		return __bitfields & 1;
	}

	void set__test_with_bit_field(int val) {
		__bitfields &= ~1;
		__bitfields |= (val << 0) & 1;
	}

	int func2() {
		test_with_bit_field = 8;
		return test_with_bit_field;
	}

	int func3(foo a,foo b,Ptr<foo> c,foo d,foo e,RefInteger f){
		foo j;
		while ((j=b) != null){}
		foo l;
		if ((l=b) != null){}
		for (foo a5=b;(a5) != null;a5=b){}
		while ((b) != null){}
		if ((b) != null){}
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
	
	foo func4(int a, int b, short c) {
		Object[] __stack = new Object[5];
		foo[] sd = StackHelper.addItem(
				CreateHelper.allocateArray(foo.class, 15), 0, __stack);
		foo foo1;
		Ptr<foo> foo2;
		foo foo3 = StackHelper.addItem(new foo(sd[1]), 1, __stack);
		foo foo4 = StackHelper.addItem(new foo(1, 2), 2, __stack);
		foo foo5 = StackHelper.addItem(new foo(foo4), 3, __stack);
		foo foo6 = this;
		foo foo7 = StackHelper.addItem(new foo(this), 4, __stack);
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
			return StackHelper.cleanup(new foo(foo7), __stack, 0);
		}
		int decl;
		switch (decl = 1) {
		case 1:
			decl += 5;
			break;
		}
		foo6.func_with_defaults_and_definition(100);
		(foo6).func_with_defaults_and_definition(100, 2);
		return StackHelper.cleanup(new foo(sd[2]), __stack, 0);
	}

	foo func4(int a, int b) {
		return func4(a, b, 55);
	}

	void func5() {
		Object[] __stack = new Object[2];
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
		for (int k = 0; k < 10; k++)
			k *= 10;
		foo ptr1 = new foo();
		foo ptr2 = new foo(100);
		foo ptr3 = CreateHelper.allocateArray(foo.class, 100);
		(ptr1).destruct();
		(ptr2).destruct();
		DestructHelper.destructArray(ptr3);
		int[] basic = new int[100];
		short[][] basic2 = new short[5][10];
		if (false)
			return;
		foo[] foos_array = StackHelper.addItem(
				CreateHelper.allocateArray(foo.class, 45 + 2), 0, __stack);
		foo[][] foos_array2 = StackHelper.addItem(
				CreateHelper.allocateArray(foo.class, 50, 20), 1, __stack);
		PtrInteger p = new int[2];
		int r;
		StackHelper.cleanup(null, __stack, 0);
	}

	void func6(foo a) {
		Object[] __stack = new Object[4];
		func6(StackHelper.addItem(new foo(1), 0, __stack));
		{
			foo foo_bar = StackHelper.addItem(new foo(), 1, __stack);
			StackHelper.cleanup(null, __stack, 1);
		}
		if (true) {
			foo foo_bar = StackHelper.addItem(new foo(), 1, __stack);
			StackHelper.cleanup(null, __stack, 1);
		}
		while (true) {
			foo foo_bar = StackHelper.addItem(new foo(), 1, __stack);
			StackHelper.cleanup(null, __stack, 1);
		}
		switch (1) {
		case 1: {
			foo foo_bar = StackHelper.addItem(new foo(), 1, __stack);
			StackHelper.cleanup(null, __stack, 1);
		}
		}
		do {
			foo foo_bar = StackHelper.addItem(new foo(), 1, __stack);
			StackHelper.cleanup(null, __stack, 1);
		} while (false);
		if (true) {
			foo foo_bar = StackHelper.addItem(new foo(), 1, __stack);
			while (true) {
				foo foo_baz = StackHelper.addItem(new foo(), 2, __stack);
				for (;;) {
					foo foo_bug = StackHelper.addItem(new foo(), 3, __stack);
					StackHelper.cleanup(null, __stack, 3);
				}
				StackHelper.cleanup(null, __stack, 2);
			}
			StackHelper.cleanup(null, __stack, 1);
		}
		StackHelper.cleanup(null, __stack, 0);
	}

	void func7() {
		Object[] __stack = new Object[6];
		for (int i = 0; i < 10; i++) {
			foo bar = StackHelper.addItem(new foo(), 0, __stack);
			if (false)
				break;
			if (true) {
				continue;
			}
			StackHelper.cleanup(null, __stack, 0);
		}
		for (int i = 0; i < 20; i++) {
			if (true)
				break;
			else
				continue;
		}
		foo baz = StackHelper.addItem(new foo(), 0, __stack);
		foo bug = StackHelper.addItem(new foo(), 1, __stack);
		foo dog = StackHelper.addItem(new foo(), 2, __stack);
		for (int i = 0; i < 5; i++) {
			foo house = StackHelper.addItem(new foo(), 3, __stack);
			if (false) {
				foo car = StackHelper.addItem(new foo(), 4, __stack);
				{
					StackHelper.cleanup(null, __stack, 3);
					break;
				}
				StackHelper.cleanup(null, __stack, 4);
			} else {
				StackHelper.cleanup(null, __stack, 3);
				continue;
			}
			StackHelper.cleanup(null, __stack, 3);
		}
		while (true) {
			foo honda = StackHelper.addItem(new foo(), 3, __stack);
			do {
				foo ferrari = StackHelper.addItem(new foo(), 4, __stack);
				if (true) {
					StackHelper.cleanup(null, __stack, 4);
					break;
				} else {
					StackHelper.cleanup(null, __stack, 4);
					continue;
				}
				StackHelper.cleanup(null, __stack, 4);
			} while (false);
			StackHelper.cleanup(null, __stack, 3);
		}
		StackHelper.addItem(new foo(1), 3, __stack);
		func6(StackHelper.addItem(new foo(), 4, __stack));
		StackHelper.addItem(func8(), 5, __stack);
		StackHelper.cleanup(null, __stack, 0);
	}

	foo func8() {
		Object[] __stack = new Object[2];
		return StackHelper
				.cleanup(new foo(StackHelper.addItem(new foo(), 1, __stack)),
						__stack, 0);
	}

	foo[] foo_bar_array = StackHelper.addItem(
			CreateHelper.allocateArray(foo.class, 10), 0, __stack);
	foo[][] foo_baz_array = StackHelper.addItem(
			CreateHelper.allocateArray(foo.class, 10, 25), 1, __stack);
	int[] basic_array = new int[1];
	int[][] not_so_basic_array = new int[5][7];
	foo foo_bar = StackHelper.addItem(new foo(), 2, __stack);

	/**
	 * @union
	 */
	class test_union implements CppType {
		int a;
		float b;
	}

	class test_struct implements CppType {
		int a;
		int b;
	}
}

/**
 * @union
 */
class test_union2 implements CppType {
	short a;
	short b;
}

class AnonClass2 implements CppType {
	class AnonClass3 implements CppType {
		int a;
	}

	AnonClass3 anon_class1 = StackHelper.addItem(new MISSING(), 3, __stack);
	AnonClass3 anon_class2 = StackHelper.addItem(new MISSING(), 4, __stack);
}
