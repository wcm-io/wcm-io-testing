## Usage

### Import resource data from JSON file in classpath

Although it is possible to create a resource data hierarchy as text fixture manually using either Sling CRUD API
or JCR API this can getting very tedious and affords a lot of boilerplate code. To make this easier a `ContentLoader`
is provided which allows importing sample data stored as a JSON file in the classpath of the unit test and running
the tests against this.

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
    context.load().json("/sample-data.json", "/content/sample/en");
  }

  @Test
  public void testSomething() {
    Resource resource = context.resourceResolver().getResource("/content/sample/en");
    Page page = resource.adaptTo(Page.class);
    // further testing
  }

}

```

This codes creates a new resource at `/content/sample/en` (and - if not existent - the parent resources) and
imports the JSON data to this node. It can be accessed using the Sling Resource or JCR API afterwards.


### Import binary data from file in classpath

It is also possible to import a binary file stored in the classpath beneath the unit tests using the `ContentLoader`.
The data is stored usig a nt:file/nt:resource or nt:resource node type. 

Example code to import a binary file:

```java
context().load().binaryFile("/sample-file.gif", "/content/binary/sample-file.gif");
```

This codes creates a new resource at `/content/binary/sample-file.gif` (and - if not existent - the parent 
resources) and imports the binary data to a jcr:content subnode.


### Building content

For easily building resources and pages a `ContentBuilder` provides convenience methods.

Example:

```java
// create page
context().create().page("/content/sample/en", "/apps/sample/template/homepage");

// create resource
context().create().resource("/content/test1", ImmutableMap.<String, Object>builder()
        .put("prop1", "value1")
        .put("prop2", "value2")
        .build());
```
