## wcm.io Testing

Helper tools for supporting Unit Tests, Integration test and test automation in AEM-based projects.


### Overview

* [AEM Mocks](aem-mock/): Mock implementations for running unit tests in AEM context without having to run a real AEM or Sling instance:
* Mock Helper: Helper for setting up Mock contexts for wcm.io subprojects.
    * [Sling](wcm-io-mock/sling/): Helps setting up mock environment for wcm.io Sling Commons and Sling Models Extensions.
    * [WCM](wcm-io-mock/wcm/): Helps setting up mock environment for wcm.io WCM Commons.
    * [Context-Aware Configuration](wcm-io-mock/caconfig/): Helps setting up mock environment for wcm.io Context-Aware Configuration.
    * [Handler](wcm-io-mock/handler/): Helps setting up mock environment for wcm.io Handler.
* Logging configuration
    * [Lockback](logging/logback/): Default configuration for Logback logging in unit tests.
* [JUnit Commons](junit-commons/): Common extensions of JUnit 4 for supporting AEM Mocks JUnit 4 integration.


### Mocking stack

Initially wcm.io provided mocking implementations for JCR, OSGi and Sling. These implementations are now part of the Apache Sling project and maintained by the Sling Community:

* [Sling Mocks](https://sling.apache.org/documentation/development/sling-mock.html)
* [OSGi Mocks](https://sling.apache.org/documentation/development/osgi-mock.html)
* [JCR Mocks](https://sling.apache.org/documentation/development/jcr-mock.html)


### GitHub Repository

Sources: https://github.com/wcm-io/wcm-io-testing
