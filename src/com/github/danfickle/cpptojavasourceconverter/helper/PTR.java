package com.github.danfickle.cpptojavasourceconverter.helper;

class PTR
{
	private PTR() { }
	
	static PtrInteger newIntPtr() { return new PtrIntegerOne(); }
	static PtrInteger newIntPtr(int[] arr) { return new PtrIntegerMulti(arr); }
	static PtrInteger addrOfInt(int val) { return new PtrIntegerOne(val); }
	static PtrInteger addrOfInt(PtrInteger right, int offset) { return new PtrIntegerMulti(right, offset); }
	static PtrInteger addrOfInt(int[] right, int offset) { return new PtrIntegerMulti(right, offset); }
}

interface PtrInteger
{
	int get();
	int get(int offset);
	int set(int item);
	int set(int offset, int item);
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
//		PtrInteger d = PTR.addrOfInt(b);
//		PtrInteger e = PTR.newIntPtr(new int[10]);
//		PtrInteger f = PTR.addrOfInt(a, 2);
//		PtrInteger g = PTR.addrOfInt(f, 5);
//		int h = g.get();
//		int i = f.get(2);
//		int j = f.add(-2).get(2);
//		PtrInteger k = g.postdec();
//		
//		k.set(111);
//		k.add(5).set(112);
//		k.set(5, 113);
//		k.postdec().set(114);
//	}
//}

class PtrIntegerOne implements PtrInteger
{
	private int val;

	PtrIntegerOne() { }
	PtrIntegerOne(int item) { val = item; }
	
	public int get(int offset)
	{
		if (offset != 0)
			throw new IllegalArgumentException();
		
		return val;
	}

	public int set(int offset, int item)
	{
		if (offset != 0)
			throw new IllegalArgumentException();

		val = item;
		return val;
	}
	
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

	PtrIntegerMulti(int[] arr) { val = arr; }
	PtrIntegerMulti(int[] arr, int offset) { val = arr; currentOffset = offset; }

	PtrIntegerMulti(PtrInteger right, int offset)
	{
		val = ((PtrIntegerMulti) right).val; 
		currentOffset = ((PtrIntegerMulti) right).currentOffset + offset;
	}
	
	public int get(int offset) { return val[currentOffset + offset]; }
	public int set(int offset, int item) {	val[currentOffset + offset] = item; return item;	}
	public int get() { return val[currentOffset]; }	
	public int set(int item) { val[currentOffset] = item; return item; }

	public PtrInteger postinc()
	{
		currentOffset++;
		return new PtrIntegerMulti(val, currentOffset - 1);
	}

	public PtrInteger postdec()
	{
		currentOffset--;
		return new PtrIntegerMulti(val, currentOffset + 1);
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
