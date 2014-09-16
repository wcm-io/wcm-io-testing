## Usage

### AEM Context JUnit Rule

The AEM mock context can be injected into a JUnit test using a custom JUnit rule named `AemContext`.
This rules takes care of all initialization and cleanup tasks required to make sure all unit tests can run 
independently (and in parallel, if required).

Example:

```java
public class ExampleTest {

  @Rule
  public final AemContext context = new AemContext();

  @Test
  public void testSomething() {
    Resource resource = context.resourceResolver().getResource("/content/sample/en");
    Page page = resource.adaptTo(Page.class);
    // further testing
  }

}

```

It is possible to combine such a unit test with a `@RunWith` annotation e.g. for
[Mockito JUnit Runner][mockito-testrunner].

The `AemContext` object provides access to mock implementations of:

* OSGi Component Context
* OSGi Bundle Context
* Sling Resource Resolver
* Sling Request
* Sling Response
* Sling Script Helper

Additionally it supports:

* Registering OSGi services
* Registering adapter factories
* Accessing JSON Importer


### JSON Data as Test Fixture

Although it is possible to create a resource data hierarchy as text fixture manually using either Sling CRUD API
or JCR API this can getting very tedious and affords a lot of boilerplate code. To make this easier a `ContentLoader`
is provided which allows importing sample data stored as a JSON file in the classpath of the unit test and running
the tests against this. This importer is provided by the [Sling Mocks][sling-mock] implementation.

Example JSON data:

```json
{
  "jcr:primaryType": "cq:Page",
  "jcr:content": {
    "jcr:primaryType": "cq:PageContent",
    "jcr:title": "English",
    "cq:template": "/apps/sample/templates/homepage",
    "sling:resourceType": "sample/components/homepage",
    "jcr:createdBy": "admin",
    "jcr:created": "Thu Aug 07 2014 16:32:59 GMT+0200",
    "par": {
      "jcr:primaryType": "nt:unstructured",
      "sling:resourceType": "foundation/components/parsys",
      "colctrl": {
        "jcr:primaryType": "nt:unstructured",
        "layout": "2;cq-colctrl-lt0",
        "sling:resourceType": "foundation/components/parsys/colctrl"
      }
    }
  }
}
```

Example unit test:

```java
public class ExampleTest {

  @Rule
  public final AemContext context = new AemContext();

  @Before
  public void setUp() throws Exception {
    context.contentLoader().importTo("/sample-data.json", "/content/sample/en");
  }

  @Test
  public void testSomething() {
    Resource resource = context.resourceResolver().getResource("/content/sample/en");
    Page page = resource.adaptTo(Page.class);
    // further testing
  }

}

```


### Choosing Resource Resolver Mock Type

The AEM mock context supports different resource resolver types (provided by the [Sling Mocks][sling-mock]
implementation). Example:

```java
public class ExampleTest {

  @Rule
  public final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

}

```

Different resource resolver mock types are supported with pros and cons, see
[Resource Resolver Types][sling-mock-rrtypes] for details.

It is even possible to supply multiple resource resolver types in the constructor argument - in this case the
unit test is run multiple times, once for each type. But this is only relevant if you want to develop your own
unit test support components that should be compatible with all resource resolver types. Normally you pick
just one type which fits best for your testing needs.


### Getting and Manipulating Pages

Example for accessing AEM API for reading and writing data:

```java
public class ExampleTest {

  @Rule
  public final AemContext context = new AemContext();

  @Test
  public void testSomething() {
    PageManager pageManager = context.resourceResolver().adaptTo(PageManager.class);
    Page page = pageManager.getPage("/content/sample/en");
    Template template = page.getTemplate();
    Iterator<Page> childPages = page.listChildren();
    // further testing
  }

  @Test
  public void testPageManagerOperations() {
    PageManager pageManager = context.resourceResolver().adaptTo(PageManager.class);
    Page page = pageManager.create("/content/sample/en", "test1",
        "/apps/sample/templates/homepage", "title1");
    // further testing
    pageManager.delete(page, false);
  }

}

```


### Simulating Sling Request

Example for preparing a sling request with custom request data:

```java
// prepare sling request
MockSlingHttpServletRequest request = (MockSlingHttpServletRequest)context.getRequest();

request.setQueryString("param1=aaa&param2=bbb");
request.setResource(resourceResolver.getResource("/content/sample"));

MockRequestPathInfo requestPathInfo = (MockRequestPathInfo)request.getRequestPathInfo();
requestPathInfo.setSelectorString("selector1.selector2");
requestPathInfo.setExtension("html");

// set current page
context.currentPage("/content/sample/en");
```

### Registering OSGi service

Example for registering and getting an OSGi service for a unit test:

```java
// register OSGi service
context.registerService(MyClass.class, myService);

// or alternatively: inject dependencies, activate and register OSGi service
context.registerInjectActivateService(myService);

// get OSGi service
MyClass service = context.slingScriptHelper().getService(MyClass.class);

// or alternatively: get OSGi service via bundle context
ServiceReference ref = context.bundleContext().getServiceReference(MyClass.class.getName());
MyClass service2 = bundleContext.getService(ref);
```


### Adapter Factories

You can register your own or existing adapter factories to support adaptions e.g. for classes extending `SlingAdaptable`.

Example:

```java
// register adapter factory
context.registerService(myAdapterFactory);

// test adaption
MyClass object = resource.adaptTo(MyClass.class);
```

You do not have to care about cleaning up the registrations - this is done automatically by the `AemContext` rule.


### Sling Models

Example:

```java
@Model(adaptables = SlingHttpServletRequest.class)
interface RequestAttributeModel {

  @Inject
  String getProp1();

}

RequestAttributeModel model = context.request().adaptTo(RequestAttributeModel.class);
```

### Application-specific AEM context

When building unit test suites for your AEM application you have usually to execute always some application-specific
preparation tasks, e.g. register custom services, adapter factories or import sample content. This can be done
in a convenience class using a `SetupCallback`. Example:

```java
public final class AppAemContext {

  public static AemContext newAemContext() {
    return new AemContext(new SetUpCallback());
  }

  private static final class SetUpCallback implements AemContextCallback {

    @Override
    public void execute(AemContext context) throws PersistenceException, IOException {

      // application-specific services for unit tests
      context.registerService(AdapterFactory.class, new AppAdapterFactory());
      context.registerService(new AemObjectInjector());

      // import sample content
      context.contentLoader().importTo("/sample-content.json", "/content/sample/en");

      // set default current page
      context.currentPage("/content/sample/en");
    }

  }

}
```

In the unit test you can use this customized AEM context:

```java
public class MyTest {

  @Rule
  public final AemContext context = AppAemContext.newAemContext();

  @Test
  public void testSomething() {
    // do test
  }

}
```




[mockito-testrunner]: http://docs.mockito.googlecode.com/hg/latest/org/mockito/runners/MockitoJUnitRunner.html
[sling-mock]: http://wcm.io/testing/sling-mock/
[sling-mock-rrtypes]: http://wcm.io/testing/sling-mock/resource-resolver-types.html
