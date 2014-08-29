## Usage

### Import resource data from JSON file

With the `JsonImporter` it is possible to import structured resource and property data from a JSON file stored
in the classpath beneath the unit tests. This data can be used as text fixture for unit tests.

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

Example code to import the JSON data:

```java
ResourceResolver resolver = MockSling.newResourceResolver();
JsonImporter jsonImporter = new JsonImporter(resolver);
jsonImporter.importTo("/sample-data.json", "/content/sample/en");
```

This codes creates a new resource at `/content/sample/en` (and - if not existent - the parent resources) and
imports the JSON data to this node. It can be accessed using the Sling Resource or JCR API afterwards.
