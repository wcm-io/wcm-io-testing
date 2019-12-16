## Managing Maven Dependencies for AEM Mocks

### AEM Mocks Dependency

To use AEM Mocks, you have to include the AEM Mock Dependency for JUnit 5:

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.testing.aem-mock.junit5</artifactId>
  <scope>test</scope>
</dependency>
```

or for JUnit 4:

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.testing.aem-mock.junit4</artifactId>
  <scope>test</scope>
</dependency>
```

Additionally you will usually include additional testing dependencies for JUnit 5/4 itself, and probably other Libraries like Mockito. See also [this guide][migrate-junit4-junit5] how to migrate AEM Mocks from JUnit 4 to JUnit 5.


### Log configuration for Unit Test

It is recommend to also include this dependency:

```xml
<dependency>
  <groupId>org.apache.sling</groupId>
  <artifactId>org.apache.sling.testing.logging-mock</artifactId>
  <version>2.0.0</version>
</dependency>
```

It contains a dependency to `slf4-simple`, and a sensible default configuration for SLF4J within the unit tests.


### Further Sling and AEM dependencies

AEM Mocks (and the underlying libraries like Sling Mocks) are designed to work with a broader range of AEM versions and not only the current version (see [AEM Version Support Matrix][aem-mock-version-support-matrix]). AEM Mocks relies on a set of additional transitive Maven dependencies for certain internal Sling bundles that are not part of the AEM "Uber Jar". For supporting also older version of AEM the versions included by default of those bundles are older ones as well, roughly matching the oldest supported AEM version. This may lead to the problem that some features e.g. in Sling Models that are available in the more recent version of AEM will not work in the unit tests.

The solution is to update the versions of the transitive dependencies to the version that are actually running in the AEM version you are targeting with your application.

To make this easier the wcm.io project maintains the [AEM Dependencies][aem-dependencies] - which is a POM for each AEM version and each AEM service pack which includes:

* The AEM Uber Jar Version
* All "internal" dependencies required for AEM Mocks and Sling Mocks in the matching versions
* Further dependencies includes in AEM but not in the AEM Uber Jar

You can include this POM with `import` scope into your project - example:

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version><!-- AEM/Service Pack version --></version>
  <type>pom</type>
  <scope>import</scope>
</dependency>

```

### Order of dependencies

It is recommended to include the AEM Mock dependencies with `test` scope always _before_ the AEM Uber Jar dependencies within you bundle projects.


[migrate-junit4-junit5]: https://wcm-io.atlassian.net/wiki/x/AYAmJ
[aem-mock-version-support-matrix]: https://wcm.io/testing/aem-mock/#AEM_Version_Support_Matrix
[aem-dependencies]: https://wcm.io/tooling/maven/aem-dependencies.html
