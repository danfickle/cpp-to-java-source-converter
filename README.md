C++ to Java Translator
======================

[Main source](/src/com/github/danfickle/cpptojavasourceconverter)

This project aims to convert C++ code to high level Java source code. Where that is not possible, a TODO should appear in the generated source code.

Status
------
The project is about three quarters of the way to an initial release. The below items will not be handled by the initial release:
+ goto statement
+ untyped malloc
+ template code generation (generics to be used instead).
+ C++/C std lib
+ order of operation differences
+ platform dependent/undefined behavior
+ unions
+ dynamic cast and other RTTI features
+ multiple inheritance
+ C++11 features
+ overloaded operator new and operator delete memory management

Everything else should work with the initial release.

Example Code
------------
The following lines give an indication of what generated code using pointers looks like.

````cpp
 // Test plain assignment...
 f[4] = 111;
 *f = 112;
 (((*(f + 2)))) = 113;
 *(f--) = 114;

 // Test compound assignment...
 f[1] *= 3;
 (*f) += 4;

 // Test pointer comparison
 f < &f[1];
 &f[0] == f;

 // Test addressof ptr
 &f;
````

````java
 f.ptrOffset(4).set(111);
 f.set(112);
 ((((f.ptrOffset(+2))))).set(113);
 (f.ptrPostDec()).set(114);

 f.ptrOffset(1).set(f.ptrOffset(1).get() * 3);
 (f).set((f.get()) + 4);

 f.ptrCompare() < f.ptrOffset(1).ptrCompare();
 f.ptrOffset(0).ptrCompare() == f.ptrCompare();

 f.ptrAddressOf();
````

Required JARs
-------------
This fork will build with Maven. Eclipse CDT is not
in Maven central, or any repository I could find, but
for your convenience, private copies of
certain JARS are shipped in the lib/ directory:
+ org.eclipse.cdt.core_5.4.1.201209170703.jar
+ org.eclipse.equinox.common_3.6.100.v20120522-1841.jar
+ com.ibm.icu_50.1.1.v201304230130.jar (I needed it at run-time once to fix a logging error)

Please note these are from a separate project with separate licenses.

Historical required JARs:

These are the JAR files I use to get this code to run.
They are from Eclipse Juno (4.2.x) and CDT 8.1.1 for Eclipse Juno.
Additionally, jOOR is required by the runtime template handling code.

+ [Eclipse downloads](http://www.eclipse.org/downloads/)
+ [CDT downloads](http://www.eclipse.org/cdt/downloads.php)
+ [jOOR at Github](https://github.com/jOOQ/jOOR)
+ org.eclipse.cdt.core_5.4.1.201209170703.jar
+ org.eclipse.equinox.common_3.6.100.v20120522-1841.jar
+ joor-0.9.3.jar

License
-------
This project is licensed under the Apache license. The CDT walking code is based loosely on code I found (and lost the url) on the web (also licensed under the Apache license).

TODO
----
This is an incomplete and fluid list of items that need doing before the initial release.

+ General
  + Lots more assertions
  + Destructor calls on static duration objects
  + Cast operators other than C style cast should not resolve overloaded operator cast
  + Basic type classes other than MInteger
  + Global variables
  + Templates
  + Should be able to take the address of an enumerator
  + Putting in comments/defines from original
  + A lot of work on multi dimension arrays.
  + cleanup cpptojavatype method.
  + function pointers.
  + typed malloc.
  + testing cleanup.
  + making logger useful.
  + relacing deprecated methods.
  + make lists.
  + correct names (camel case, capitalized classes etc.)
  + Varargs.
  + Bracketed expressions
  + Special case this pointer and void pointer.
  + exact bit position for bitfield.
+ First pass
  + Recursively evaluate templates and generate expanded cpp code.
  + Generate implicit special methods and output cpp code.
  + Record if a basic variable(int, bool, etc) has its address taken so we can skip objectifying it if it does not.

