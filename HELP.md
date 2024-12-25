# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https:
* [Spring Boot Maven Plugin Reference Guide](https:
* [Create an OCI image](https:
* [Spring Web](https:

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https:
* [Serving Web Content with Spring MVC](https:
* [Building REST services with Spring](https:

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

