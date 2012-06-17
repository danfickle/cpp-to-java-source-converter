package com.github.danfickle.cpptojavasourceconverter.helper;

class PTR
{
	private PTR() { }
	
	static PtrInteger newIntPtr() { return new PtrIntegerOne(); }
	static PtrInteger newIntPtr(int val) { return new PtrIntegerOne(val); }
	static PtrInteger newIntPtr(int[] arr) { return new PtrIntegerMulti(arr); }
	static PtrInteger newIntPtr(PtrInteger right)
	{
		if (right instanceof PtrIntegerMulti)
			return new PtrIntegerMulti((PtrIntegerMulti) right);
		else
			return new PtrIntegerOne((PtrIntegerOne) right);
	}
}

interface PtrInteger
{
	int get();
	int set(int item);
	PtrInteger postinc();
	PtrInteger postdec();
	PtrInteger adjust(int cnt);
	PtrInteger add(int cnt);
}

//class Sample
//{
//	static void meth()
//	{
//		int [] a = new int[5];
//		int b = 0;
//		
//		PtrInteger c = PTR.newIntPtr();
//		PtrInteger d = PTR.newIntPtr(b);
//		PtrInteger e = PTR.newIntPtr(new int[10]);
//		PtrInteger f = PTR.newIntPtr(a);
//		int h = f.get();            // h = *f or h = f[0]
//		int i = f.add(2).get();     // i = f[2] or i = *(f + 2)
//		int j = e.add(5).set(2);    // j = e[5] = 2  or j = *(e + 5) = 2 
//		PtrInteger k = e.add(5);    // int * k = &e[5] or k = e + 5 
//		int l = k.postdec().get();  // l = *(k--)
//		k.adjust(-2);               // k -= 2
//
//		k.set(111);                 // *k = 111 or k[0] = 111
//		k.add(5).set(112);          // k[5] = 112 or *(k + 5) = 112
//		k.postdec().set(114);       // *(k--) = 114
//	}
//}

class PtrIntegerOne implements PtrInteger
{
	private int val;

	PtrIntegerOne() { }
	PtrIntegerOne(int item) { val = item; }
	PtrIntegerOne(PtrIntegerOne right) { val = right.val; }
	
	public int get() { return val; }	
	public int set(int item) { val = item; return val; }
	public PtrInteger postinc() { throw new IllegalStateException(); }
	public PtrInteger postdec() { throw new IllegalStateException(); }

	public PtrInteger adjust(int cnt) 
	{
		if (cnt != 0)
			throw new IllegalArgumentException();

		return this;
	}

	public PtrInteger add(int cnt)
	{
		if (cnt != 0)
			throw new IllegalArgumentException();
	
		return this;
	}
}

class PtrIntegerMulti implements PtrInteger
{
	private final int[] val;
	private int currentOffset;

	private PtrIntegerMulti(int[] arr, int offset)
	{
		val = arr;
		currentOffset = offset;
	}

	PtrIntegerMulti(PtrIntegerMulti right)
	{
		val = right.val; 
		currentOffset = right.currentOffset;
	}

	PtrIntegerMulti(int[] arr) { val = arr; }
	public int get() { return val[currentOffset]; }	
	public int set(int item) { val[currentOffset] = item; return item; }

	public PtrInteger postinc()
	{
		return new PtrIntegerMulti(val, currentOffset++);
	}

	public PtrInteger postdec()
	{
		return new PtrIntegerMulti(val, currentOffset--);
	}

	public PtrInteger adjust(int cnt) 
	{
		currentOffset += cnt;
		return this;
	}

	public PtrInteger add(int cnt) 
	{
		return new PtrIntegerMulti(val, currentOffset + cnt);
	}
}
