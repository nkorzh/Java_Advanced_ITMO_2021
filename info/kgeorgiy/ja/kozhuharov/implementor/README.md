## Class Implementor

**Task**: Develop an ``Implementor`` class which will generate implementations of classes and interfaces.  

* Command-line argument: The full name of the class / interface to generate the implementation for.  
* As a result, the java code of the class with the Impl suffix must be generated, extending (implementing) the specified class (interface).
* The generated class should compile without errors.
* The generated class must not be abstract.
* Methods of the generated class should ignore their arguments and return default values.  

In the task, there are three options:

* Simple -- Implementor should be able to implement only interfaces (but not classes). Generics support is not required.
* **Complex** -- The Implementor must be able to implement both classes and interfaces. Generics support is not required.
* Bonus -- The Implementor must be able to implement generic classes and interfaces. The generated code must have the correct type parameters and not generate ``UncheckedWarning``.
