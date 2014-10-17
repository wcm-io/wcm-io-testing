## About AEM Mocks

Mock implementation of selected AEM APIs.

### Maven Dependency

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.testing.aem-mock</artifactId>
  <version>1.2.0-SNAPSHOT</version>
</dependency>
```

### Documentation

* [Usage](usage.html)
* [Test content loading and creation](usage-content-loader-builder.html)
* [API documentation](apidocs/)
* [Changelog](changes-report.html)


### Implemented mock features

The mock implementation supports:

* Access to [mocked OSGi][osgi-mock], [mocked JCR][jcr-mock] and [mocked Sling][sling-mock] environment provided by the Apache Sling project
* Resource access using different resource resolver types (see [Resource Resolver Types][sling-mock-rrtypes])
* Implementation of AEM WCM API objects `PageManager`, `Page` and `Template`
* Implementation of AEM DAM API objects `Asset` and `Rendition`
* JUnit rule `AemContext` for easy access to all context objects and registering adapter factories and OSGi services
* Import and create test content for unit tests (see [Test content loading and creation](usage-content-loader-builder.html))
* Registers OSGi services and adapter factories supported by the mock implementations
* Full support for Sling Models
* Setting run modes
* Layer adapter factory

The following features are *not supported*:

* Other parts of the AEM API


[osgi-mock]: http://sling.apache.org/documentation/development/osgi-mock.html
[jcr-mock]: http://sling.apache.org/documentation/development/jcr-mock.html
[sling-mock]: http://sling.apache.org/documentation/development/sling-mock.html
[sling-mock-rrtypes]: http://sling.apache.org/documentation/development/sling-mock.html#resource-resolver-types
