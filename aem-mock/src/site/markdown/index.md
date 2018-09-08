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


### Further Resources

* [adaptTo() 2018 Talk: JUnit 5 and Sling/AEM Mocks][adaptto-talk-2018-junit5-sling-aem-mocks]
* [adaptTo() 2016 Talk: Unit Testing with Sling & AEM Mocks][adaptto-2016-talk-unittesting-sling-aem-mocks]
* [adaptTo() 2014 Lightning Talk: Mock AEM & Co for Unit Tests][adaptto-talk-2014-mock-aem-unit-tests]


[osgi-mock]: http://sling.apache.org/documentation/development/osgi-mock.html
[jcr-mock]: http://sling.apache.org/documentation/development/jcr-mock.html
[sling-mock]: http://sling.apache.org/documentation/development/sling-mock.html
[sling-mock-rrtypes]: http://sling.apache.org/documentation/development/sling-mock.html#resource-resolver-types
[adaptto-talk-2018-junit5-sling-aem-mocks]: https://adapt.to/2018/en/schedule/junit-5-and-sling-aem-mocks.html
[adaptto-2016-talk-unittesting-sling-aem-mocks]: https://adapt.to/2016/en/schedule/unit-testing-with-sling-aem-mocks.html
[adaptto-talk-2014-mock-aem-unit-tests]: https://adapt.to/2014/en/schedule/lightning-talks.html
