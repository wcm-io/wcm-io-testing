## About AEM Mocks

Mock implementation of selected AEM APIs.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.testing.aem-mock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.testing.aem-mock)


### Documentation

* [Usage](usage.html)
* [Test content loading and creation](usage-content-loader-builder.html)
* [API documentation (JUnit 4)](junit4/apidocs/)
* [API documentation (JUnit 5)](junit5/apidocs/)
* [Changelog](changes-report.html)


### Version Support Matrix

|AEM Mock version |AEM version supported |JUnit version supported
|-----------------|----------------------|------------------------
|AEM Mock 2.x     |AEM 6.2 or up         |JUnit 4, JUnit 5
|AEM Mock 1.x     |AEM 6.0 or 6.1        |JUnit 4


### Implemented mock features

The mock implementation supports:

* Access to [mocked OSGi][osgi-mock], [mocked JCR][jcr-mock] and [mocked Sling][sling-mock] environment provided by the Apache Sling project
* Resource access using different resource resolver types (see [Resource Resolver Types][sling-mock-rrtypes])
* Implementation of AEM WCM API objects `PageManager`, `Page`, `Template`, `ComponentManager`, `Component`, `TagManager`, `Tag`, `Designer`,
  `ComponentContext`, `EditContext`, `EditConfig`
* Implementation of AEM DAM API objects `Asset` and `Rendition`
* JUnit rule `AemContext` for easy access to all context objects and registering adapter factories and OSGi services
* Import and create test content for unit tests (see [Test content loading and creation](usage-content-loader-builder.html))
* Registers OSGi services and adapter factories supported by the mock implementations
* Full support for Sling Models
* Setting run modes
* Layer adapter factory
* Context Plugins

The following features are *not supported*:

* Other parts of the AEM API


[osgi-mock]: http://sling.apache.org/documentation/development/osgi-mock.html
[jcr-mock]: http://sling.apache.org/documentation/development/jcr-mock.html
[sling-mock]: http://sling.apache.org/documentation/development/sling-mock.html
[sling-mock-rrtypes]: http://sling.apache.org/documentation/development/sling-mock.html#resource-resolver-types
