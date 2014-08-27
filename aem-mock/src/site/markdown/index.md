## About AEM Mocks

Mock implementation of selected AEM APIs.

### Maven Dependency

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.testing.aem-mock</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Documentation

* [Usage](usage.html)
* [API Documentation](apidocs/)
* [Changelog](changes-report.html)

### Implemented mock features

The mock implementation supports:

* Access to [mocked OSGi][osgi-mock], [mocked JCR][jcr-mock] and [mocked Sling][sling-mock] environment
* Resource access using different resource resolver types (see [Sling Mocks Usage][sling-mock-usage])
* Implementation of AEM WCM API objects `PageManager`, `Page` and `Template`
* Implementation of AEM DAM API objects `Asset` and `Rendition`
* JUnit rule for easy access to all context objects and registering adapter factories and OSGi services
* Registers OSGi services and adapter factories supported by the mock implementations
* Full support for Sling Models

The following features are *not supported*:

* Other parts of the AEM API


[osgi-mock]: http://wcm.io/testing/osgi-mock/
[jcr-mock]: http://wcm.io/testing/jcr-mock/
[sling-mock]: http://wcm.io/testing/sling-mock/
[sling-mock-usage]: http://wcm.io/testing/sling-mock/usage-mocks.html
