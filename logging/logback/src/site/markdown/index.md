## About Testing Log configuration for Logback

Default configuration for [Logback][logback] logging in unit tests.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.testing.logging.logback/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.testing.logging.logback)


### Documentation

* [Changelog](changes-report.html)


### Overview

The AEM API Uber Jar comes since AEM 6.5.3 with a static logger implementation for Logback which is useful for outputting log messages in unit test. This module contains a default log configuration which can be included in AEM projects as test dependency.

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.testing.logging.logback</artifactId>
  <scope>test</scope>
</dependency>
```

If you want to alternatively use `slf4j-simple` you can add a test dependency to [org.apache.sling.testing.logging-mock][sling-logging-mock] instead.



[logback]: http://logback.qos.ch/
[sling-logging-mock]: https://github.com/apache/sling-org-apache-sling-testing-logging-mock
