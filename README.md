C++ to Java Translator
======================

[Main source](/src/com/github/danfickle/cpptojavasourceconverter)

This project aims to convert C++ code to high level Java source code. Where that is not possible, an error should appear in the generated source code.

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
+ dynamic cast, etc.
+ multiple inheritance
+ C++11 features

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
These are the JAR files I use to get this code to run.
They are from Eclipse Juno (4.2.x), CDT 8.1.1 for Eclipse Juno and StringTemplate 4.07

+ [Eclipse downloads](http://www.eclipse.org/downloads/)
+ [CDT downloads](http://www.eclipse.org/cdt/downloads.php)
+ [StringTemplate downloads](http://stringtemplate.org/download.html)
+ org.eclipse.cdt.core_5.4.1.201209170703.jar
+ org.eclipse.equinox.common_3.6.100.v20120522-1841.jar
+ ST-4.0.7.jar

License
-------
This project is licensed under the Apache license. The CDT walking code is based loosely on code I found (and lost the url) on the web (also licensed under the Apache license). If you know the source of the code please file an issue.

TODO
----
This is an incomplete and fluid list of items that need doing before the initial release.

+ General
  + integer promotion/demotion
  + Object array/pointer interface.
  + cleanup cpptojavatype method.
  + putting global declarations other than classes and enums into a class.
  + function pointers.
  + typed malloc.
  + testing cleanup.
  + making logger useful.
  + relacing deprecated methods.
  + make lists.
+ test-references
+ test-enums
+ test-bitfield
  + exact bit position for bitfield.
+ test-basic-types
  + multi dimension arrays.
+ test-class
  + top level class being marked as nested and nested inside Global.
+ test-operator-overloading
  + operator overloads being generated but not being used.
+ test-pointers

