## Usage

The factory class `MockSling` allows to instantiate the different mock implementations.

### Sling Resource Resolver

Example:

```java
// get a resource resolver
ResourceResolver resolver = MockSling.newResourceResolver();

// get a resource resolver backed by a specific repository type
ResourceResolver resolver = MockSling.newResourceResolver(ResourceResolverType.JCR_MOCK);
```
The following types are supported currently: [Resource Resolver Types](resource-resolver-types.html)

### Adapter Factories

You can register your own or existing adapter factories to support adaptions e.g. for classes extending `SlingAdaptable`.

Example:

```java
// register adapter factory
BundleContext bundleContext = MockOsgiFactory.newBundleContext();
MockSling.setAdapterManagerBundleContext(bundleContext);
bundleContext.registerService(myAdapterFactory);

// test adaption
MyClass object = resource.adaptTo(MyClass.class);

// cleanup after unit test
MockSling.clearAdapterManagerBundleContext();
```

Make sure you clean up the adapter manager bundle association after running the unit test otherwise it can 
interfere with the following tests. In the [AEM Mocks][aem-mock] this is done automatically using a JUnit rule.


### SlingScriptHelper

Example:

```java
// get script helper
SlingScriptHelper scriptHelper = MockSling.newSlingScriptHelper();

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
ResourceResolver resourceResolver = MockSling.newResourceResolver();
MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resourceResolver);

request.setQueryString("param1=aaa&param2=bbb");
request.setResource(resourceResolver.getResource("/content/sample"));

MockRequestPathInfo requestPathInfo = (MockRequestPathInfo)request.getRequestPathInfo();
requestPathInfo.setSelectorString("selector1.selector2");
requestPathInfo.setExtension("html");
```


[jcr-mock]: http://wcm.io/testing/jcr-mock/
[aem-mock]: http://wcm.io/testing/aem-mock/
