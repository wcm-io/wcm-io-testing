## About OSGi Mocks

Mock implementation of selected OSGi APIs.

### Maven Dependency

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.testing.osgi-mock</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Documentation

* [Usage](usage.html)
* [API Documentation](apidocs/)
* [Changelog](changes-report.html)

### Implemented mock features

The mock implementation supports:

* Instantiating OSGi `Bundle`, `BundleContext` and `ComponentContext` objects and navigate between them.
* Read and write properties on them.
* Register OSGi services and get references to service instances
* Mock implementation of `LogService` which logs to SLF4J in JUnit context

The following features are *not supported*:

* Activation and deactivation methods of services are not called automatically
* No dependency injection takes place, the dependencies has to be set manually (e.g. via reflection)
