## Usage

The factory class `MockSlingFactory` allows to instantiate the different mock implementations.

### Sling Resource Resolver

Examples:

```java
// get a resource resolver
ResourceResolver resolver = MockSlingFactory.newResourceResolver();

// get a resource resolver backed by a specific repository type
ResourceResolver resolver = MockSlingFactory.newResourceResolver(ResourceResolverType.JCR_MOCK);
```

The following resource resolver types are supported:
* **JCR_MOCK** (default)
  * Based on the [JCR Mocks][jcr-mock] implementation
  * Uses the productive [Sling JCR resource provider implementation][jcr-resource] internally to do the Resource-JCR mapping
  * Is quite fast because data is stored only in-memory
* **JCR_JACKRABBIT**
  * Uses a real JCR Jackrabbit implementation (not Oak) as provded by [sling/commons/testing][sling-comons-testing]
  * Uses the productive [Sling JCR resource provider implementation][jcr-resource] internally to do the Resource-JCR mapping
  * Takes some seconds for startup on the first access 
  * All node types that are used when reading/writing data have to be registered
  * Beware: The repository is not cleared for each unit test, so make sure us use a unique node path for each unit test.
* **RESOURCERESOLVER_MOCK** 
  * Simulates an In-Memory resource tree, does not provide adaptions to JCR API.
  * Based on the [Sling resourceresolver-mock implementation][resourceresolver-mock] implementation
  * You can use it to make sure the code you want to test does not contain references to JCR API.
  * Behaves slightly different from JCR resource mapping e.g. handling binary and date values.
  * This resource resolver type is very fast because data is stored in memory and no JCR mapping is applied.

### Adapter Factories

You can register your own or existing adapter factories to support adaptions e.g. for classes extending `SlingAdaptable`.

Example:

```java
// register adapter factory
MockSlingFactory.registerAdapterFactory(myAdapterFactory);

// test adaption
MyClass object = resource.adaptTo(MyClass.class);

// cleanup after unit test
MockSlingFactory.clearAdapterRegistrations();
```

Make sure you clean up the registrations after running the unit test otherwise they can interfere with the following
tests. In the [AEM Mocks][aem-mock] this is done automatically using a JUnit rule.


### SlingScriptHelper

Example:

```java
// get script helper
SlingScriptHelper scriptHelper = MockSlingFactory.newSlingScriptHelper();

// get request
SlingHttpServletRequest request = scriptHelper.getRequest();

// get service
MyService object = scriptHelper.getService(MyService.class);
```

To support getting OSGi services you have to register them via the `BundleContext` interface of the
[JCR Mocks][jcr-mock] before. You can use an alternative factory method for the `SlingScriptHelper` providing
existing instances of request, response and bundle context. 

### Servlet-related Sling API Mocks

Example for preparing a sling request with custom request data:

```java
// prepare sling request
ResourceResolver resourceResolver = MockSlingFactory.newResourceResolver();
MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resourceResolver);

request.setQueryString("param1=aaa&param2=bbb");
request.setResource(resourceResolver.getResource("/content/sample"));

MockRequestPathInfo requestPathInfo = (MockRequestPathInfo)request.getRequestPathInfo();
requestPathInfo.setSelectorString("selector1.selector2");
requestPathInfo.setExtension("html");
```


[jcr-mock]: http://wcm.io/testing/jcr-mock/
[jcr-resource]: http://svn.apache.org/repos/asf/sling/trunk/bundles/jcr/resource
[sling-comons-testing]: http://svn.apache.org/repos/asf/sling/trunk/bundles/commons/testing
[resourceresolver-mock]: http://svn.eu.apache.org/repos/asf/sling/trunk/testing/resourceresolver-moc
[aem-mock]: http://wcm.io/testing/aem-mock/
