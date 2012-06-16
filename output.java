package yourpackage;

class Globals {
	static class class_outside_namespace implements
			CppType<class_outside_namespace> {
		byte a;
		char b;
		short c;
		int d;
		int e;
		long f;
		int g;
		short h;
		long i;

		class_outside_namespace() {
		}

		public void destruct() {
		}

		public class_outside_namespace op_assign(class_outside_namespace right) {
			if (right != this) {
				a = right.a;
				b = right.b;
				c = right.c;
				d = right.d;
				e = right.e;
				f = right.f;
				g = right.g;
				h = right.h;
				i = right.i;
			}
			return this;
		}

		class_outside_namespace(class_outside_namespace right) {
			a = right.a;
			b = right.b;
			c = right.c;
			d = right.d;
			e = right.e;
			f = right.f;
			g = right.g;
			h = right.h;
			i = right.i;
		}

		public class_outside_namespace copy() {
			return new class_outside_namespace(this);
		}
	}

	static class_outside_namespace cls1 = new class_outside_namespace();
	static int variable;
	static AnonClass3 anon_class3 = new MISSING();
	static foo foo_bar_top_level = new foo();
	static foo[] foo_bar_array_top_level1 = (foo[]) CreateHelper.allocateArray(
			foo.class, 10);
	static foo[][] foo_bar_array_top_level2 = (foo[][]) CreateHelper
			.allocateArray(foo.class, 10, 40);
	static int[] array_top_level1 = new int[10];
	static byte[][] array_top_level2 = new byte[20][5];
	static int top_level_int = 5;
	static foo top_level_foo = new foo(3);
	static PtrInteger top_level_ptr = 0;
	static PtrInteger top_level_ptr2 = new int[25];
}

class CppString implements CppType<CppString> {
	int m_count;

	CppString(int i) {
		this.m_count = i;
	}

	public void destruct() {
	}

	public CppString op_assign(CppString right) {
		if (right != this) {
			m_count = right.m_count;
		}
		return this;
	}

	CppString(CppString right) {
		m_count = right.m_count;
	}

	public CppString copy() {
		return new CppString(this);
	}
}

class mypair<T> implements CppType<mypair> {
	T[] values;

	mypair(T first, T second) {
		this.values = (T[]) CreateHelper.allocateArray(T.class, 2);
		values[0].op_assign(first);
		values[1].op_assign(second);
	}

	public void destruct() {
		DestructHelper.destructArray(this.values);
	}

	public mypair op_assign(mypair right) {
		if (right != this) {
			CPP.assignArray(values, right.values);
		}
		return this;
	}

	mypair(mypair right) {
		values = (T[]) CPP.copyArray(right.values);
	}

	public mypair copy() {
		return new mypair(this);
	}
}

class HashMap<T, V> implements CppType<HashMap> {
	T one;
	V two;

	HashMap() {
		this.one = new T();
		this.two = new V();
	}

	public void destruct() {
		this.two.destruct();
		this.one.destruct();
	}

	public HashMap op_assign(HashMap right) {
		if (right != this) {
			one.op_assign(right.one);
			two.op_assign(right.two);
		}
		return this;
	}

	HashMap(HashMap right) {
		one = right.one.copy();
		two = right.two.copy();
	}

	public HashMap copy() {
		return new HashMap(this);
	}
}

class CT<V, T> implements CppType<CT> {
	HashMap<V, Integer> three;
	T one;
	V two;

	CT() {
		this.three = new HashMap<V, Integer>();
		this.one = new T();
		this.two = new V();
	}

	public void destruct() {
		this.two.destruct();
		this.one.destruct();
		this.three.destruct();
	}

	public CT op_assign(CT right) {
		if (right != this) {
			three.op_assign(right.three);
			one.op_assign(right.one);
			two.op_assign(right.two);
		}
		return this;
	}

	CT(CT right) {
		three = right.three.copy();
		one = right.one.copy();
		two = right.two.copy();
	}

	public CT copy() {
		return new CT(this);
	}
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

class foo implements CppType<foo> {
	foo() {
	}

	public void destruct() {
	}

	foo(foo right) {
		int i = 10;
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

	static int static_int;
	static String static_string = new String();

	public foo op_assign(foo right) {
		return this;
	}

	public foo copy() {
		return new foo(this);
	}
}

class test extends foo implements CppType<test> {
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

	static class AnonClass0 implements CppType<AnonClass0> {
		int a;
		int b;

		AnonClass0() {
		}

		public void destruct() {
		}

		public AnonClass0 op_assign(AnonClass0 right) {
			if (right != this) {
				a = right.a;
				b = right.b;
			}
			return this;
		}

		AnonClass0(AnonClass0 right) {
			a = right.a;
			b = right.b;
		}

		public AnonClass0 copy() {
			return new AnonClass0(this);
		}
	}

	AnonClass0 anon_class1;
	AnonClass0 anon_class2;

	static class AnonClass1 implements CppType<AnonClass1> {
		int c;
		int d;

		AnonClass1() {
		}

		public void destruct() {
		}

		public AnonClass1 op_assign(AnonClass1 right) {
			if (right != this) {
				c = right.c;
				d = right.d;
			}
			return this;
		}

		AnonClass1(AnonClass1 right) {
			c = right.c;
			d = right.d;
		}

		public AnonClass1 copy() {
			return new AnonClass1(this);
		}
	}

	AnonClass1 anon_class3;
	AnonClass1 anon_class4;

	static class subby implements CppType<subby> {
		int k;

		subby() {
		}

		public void destruct() {
		}

		public subby op_assign(subby right) {
			if (right != this) {
				k = right.k;
			}
			return this;
		}

		subby(subby right) {
			k = right.k;
		}

		public subby copy() {
			return new subby(this);
		}
	}

	foo test_foo_with_init;

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
				(foo[]) CreateHelper.allocateArray(foo.class, 15), 0, __stack);
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
			return StackHelper.cleanup(foo7.copy(), __stack, 0);
		}
		int decl;
		switch (decl = 1) {
		case 1:
			decl += 5;
			break;
		}
		foo6.func_with_defaults_and_definition(100);
		(foo6).func_with_defaults_and_definition(100, 2);
		return StackHelper.cleanup(sd[2].copy(), __stack, 0);
	}

	foo func4(int a, int b) {
		return func4(a, b, 55);
	}

	void func5() {
		Object[] __stack = new Object[2];
		int i = 0;
		if (true) {
			i += 5;
		}
		while (false) {
			i -= 10;
		}
		for (i = 0; i < 20; i++) {
			i--;
		}
		switch (i) {
		case 1:
			return;
		case 2:
			i++;
		}
		if (true) {
			for (;;) {
				while (true) {
					;
				}
			}
		}
		for (int k = 0; k < 10; k++) {
			k *= 10;
		}
		foo ptr1 = new foo();
		foo ptr2 = new foo(100);
		foo ptr3 = (foo) CreateHelper.allocateArray(foo.class, 100);
		(ptr1).destruct();
		(ptr2).destruct();
		DestructHelper.destructArray(ptr3);
		int[] basic = new int[100];
		short[][] basic2 = new short[5][10];
		if (false) {
			return;
		}
		foo[] foos_array = StackHelper.addItem(
				(foo[]) CreateHelper.allocateArray(foo.class, 45 + 2), 0,
				__stack);
		foo[][] foos_array2 = StackHelper.addItem(
				(foo[][]) CreateHelper.allocateArray(foo.class, 50, 20), 1,
				__stack);
		PtrInteger p = new int[2];
		int r;
		StackHelper.cleanup(null, __stack, 0);
	}

	void func6(foo a) {
		Object[] __stack = new Object[6];
		func6(StackHelper.addItem(new foo(1), 0, __stack));
		foo foo_for_func = StackHelper.addItem(new foo(), 1, __stack);
		func6(StackHelper.addItem(foo_for_func.copy(), 2, __stack));
		{
			foo foo_bar = StackHelper.addItem(new foo(), 3, __stack);
			StackHelper.cleanup(null, __stack, 3);
		}
		if (true) {
			foo foo_bar = StackHelper.addItem(new foo(), 3, __stack);
			StackHelper.cleanup(null, __stack, 3);
		}
		while (true) {
			foo foo_bar = StackHelper.addItem(new foo(), 3, __stack);
			StackHelper.cleanup(null, __stack, 3);
		}
		switch (1) {
		case 1: {
			foo foo_bar = StackHelper.addItem(new foo(), 3, __stack);
			StackHelper.cleanup(null, __stack, 3);
		}
		}
		do {
			foo foo_bar = StackHelper.addItem(new foo(), 3, __stack);
			StackHelper.cleanup(null, __stack, 3);
		} while (false);
		if (true) {
			foo foo_bar = StackHelper.addItem(new foo(), 3, __stack);
			while (true) {
				foo foo_baz = StackHelper.addItem(new foo(), 4, __stack);
				for (;;) {
					foo foo_bug = StackHelper.addItem(new foo(), 5, __stack);
					StackHelper.cleanup(null, __stack, 5);
				}
				StackHelper.cleanup(null, __stack, 4);
			}
			StackHelper.cleanup(null, __stack, 3);
		}
		StackHelper.cleanup(null, __stack, 0);
	}

	void func7() {
		Object[] __stack = new Object[6];
		for (int i = 0; i < 10; i++) {
			foo bar = StackHelper.addItem(new foo(), 0, __stack);
			if (false) {
				StackHelper.cleanup(null, __stack, 0);
				break;
			}
			if (true) {
				{
					StackHelper.cleanup(null, __stack, 0);
					continue;
				}
			}
			StackHelper.cleanup(null, __stack, 0);
		}
		for (int i = 0; i < 20; i++) {
			if (true) {
				break;
			} else {
				continue;
			}
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
		switch (10) {
		case 5:
			break;
		case 11: {
			foo foo_in_case = StackHelper.addItem(new foo(), 3, __stack);
			{
				StackHelper.cleanup(null, __stack, 3);
				break;
			}
		}
		}
		while (true) {
			foo foo_in_while = StackHelper.addItem(new foo(), 3, __stack);
			switch (5) {
			case 7:
				break;
			case 9: {
				StackHelper.cleanup(null, __stack, 3);
				continue;
			}
			}
			StackHelper.cleanup(null, __stack, 3);
		}
		StackHelper.addItem(new foo(1), 3, __stack);
		func6(StackHelper.addItem(new foo(), 4, __stack));
		StackHelper.addItem(func8(), 5, __stack);
		StackHelper.cleanup(null, __stack, 0);
	}

	foo func8() {
		Object[] __stack = new Object[1];
		foo foo_bax = StackHelper.addItem(new foo(), 0, __stack);
		if (false) {
			return StackHelper.cleanup(new foo(), __stack, 0);
		} else {
			return StackHelper.cleanup(foo_bax.copy(), __stack, 0);
		}
		StackHelper.cleanup(null, __stack, 0);
	}

	test() {
		this.anon_class1 = new MISSING();
		this.anon_class2 = new MISSING();
		this.anon_class3 = new MISSING();
		this.anon_class4 = new MISSING();
		this.test_foo_with_init = new foo();
		this.foo_bar_array = (foo[]) CreateHelper.allocateArray(foo.class, 10);
		this.foo_baz_array = (foo[][]) CreateHelper.allocateArray(foo.class,
				10, 25);
		this.basic_array = new int[1];
		this.not_so_basic_array = new int[5][7];
		this.foo_bar = new foo();
		int i = 10;
	}

	test(int i) {
		this.anon_class1 = new MISSING();
		this.anon_class2 = new MISSING();
		this.anon_class3 = new MISSING();
		this.anon_class4 = new MISSING();
		this.test_foo_with_init = new foo();
		this.foo_bar_array = (foo[]) CreateHelper.allocateArray(foo.class, 10);
		this.foo_baz_array = (foo[][]) CreateHelper.allocateArray(foo.class,
				10, 25);
		this.basic_array = new int[1];
		this.not_so_basic_array = new int[5][7];
		this.foo_bar = new foo();
		int j = i;
	}

	public void destruct() {
		int i = 55;
		this.foo_bar.destruct();
		DestructHelper.destructArray(this.foo_baz_array);
		DestructHelper.destructArray(this.foo_bar_array);
		this.test_foo_with_init.destruct();
		this.anon_class4.destruct();
		this.anon_class3.destruct();
		this.anon_class2.destruct();
		this.anon_class1.destruct();
		super.destruct();
	}

	foo[] foo_bar_array;
	foo[][] foo_baz_array;
	int[] basic_array;
	int[][] not_so_basic_array;
	foo foo_bar;

	/**
	 * @union
	 */
	static class test_union implements CppType<test_union> {
		int a;
		float b;

		test_union() {
		}

		public void destruct() {
		}

		public test_union op_assign(test_union right) {
			if (right != this) {
				a = right.a;
				b = right.b;
			}
			return this;
		}

		test_union(test_union right) {
			a = right.a;
			b = right.b;
		}

		public test_union copy() {
			return new test_union(this);
		}
	}

	static class test_struct implements CppType<test_struct> {
		int a;
		int b;

		test_struct() {
		}

		public void destruct() {
		}

		public test_struct op_assign(test_struct right) {
			if (right != this) {
				a = right.a;
				b = right.b;
			}
			return this;
		}

		test_struct(test_struct right) {
			a = right.a;
			b = right.b;
		}

		public test_struct copy() {
			return new test_struct(this);
		}
	}

	public test op_assign(test right) {
		if (right != this) {
			super.op_assign(right);
			anon_class1.op_assign(right.anon_class1);
			anon_class2.op_assign(right.anon_class2);
			anon_class3.op_assign(right.anon_class3);
			anon_class4.op_assign(right.anon_class4);
			test_foo_with_init.op_assign(right.test_foo_with_init);
			test_with_bit_field = right.test_with_bit_field;
			CPP.assignArray(foo_bar_array, right.foo_bar_array);
			CPP.assignArray(foo_baz_array, right.foo_baz_array);
			CPP.assignBasicArray(basic_array, right.basic_array);
			CPP.assignMultiArray(not_so_basic_array, right.not_so_basic_array);
			foo_bar.op_assign(right.foo_bar);
		}
		return this;
	}

	test(test right) {
		super(right);
		anon_class1 = right.anon_class1.copy();
		anon_class2 = right.anon_class2.copy();
		anon_class3 = right.anon_class3.copy();
		anon_class4 = right.anon_class4.copy();
		test_foo_with_init = right.test_foo_with_init.copy();
		test_with_bit_field = right.test_with_bit_field;
		foo_bar_array = (foo[]) CPP.copyArray(right.foo_bar_array);
		foo_baz_array = (foo[][]) CPP.copyArray(right.foo_baz_array);
		basic_array = (int[]) CPP.copyBasicArray(right.basic_array);
		not_so_basic_array = (int[][]) CPP
				.copyMultiArray(right.not_so_basic_array);
		foo_bar = right.foo_bar.copy();
	}

	public test copy() {
		return new test(this);
	}
}

/**
 * @union
 */
class test_union2 implements CppType<test_union2> {
	short a;
	short b;

	test_union2() {
	}

	public void destruct() {
	}

	public test_union2 op_assign(test_union2 right) {
		if (right != this) {
			a = right.a;
			b = right.b;
		}
		return this;
	}

	test_union2(test_union2 right) {
		a = right.a;
		b = right.b;
	}

	public test_union2 copy() {
		return new test_union2(this);
	}
}

class AnonClass2 implements CppType<AnonClass2> {
	static class AnonClass3 implements CppType<AnonClass3> {
		int a;

		AnonClass3() {
		}

		public void destruct() {
		}

		public AnonClass3 op_assign(AnonClass3 right) {
			if (right != this) {
				a = right.a;
			}
			return this;
		}

		AnonClass3(AnonClass3 right) {
			a = right.a;
		}

		public AnonClass3 copy() {
			return new AnonClass3(this);
		}
	}

	AnonClass3 anon_class1;
	AnonClass3 anon_class2;

	AnonClass2() {
		this.anon_class1 = new MISSING();
		this.anon_class2 = new MISSING();
	}

	public void destruct() {
		this.anon_class2.destruct();
		this.anon_class1.destruct();
	}

	public AnonClass2 op_assign(AnonClass2 right) {
		if (right != this) {
			anon_class1.op_assign(right.anon_class1);
			anon_class2.op_assign(right.anon_class2);
		}
		return this;
	}

	AnonClass2(AnonClass2 right) {
		anon_class1 = right.anon_class1.copy();
		anon_class2 = right.anon_class2.copy();
	}

	public AnonClass2 copy() {
		return new AnonClass2(this);
	}
}
