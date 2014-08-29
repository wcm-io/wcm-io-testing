## Usage

### Getting OSGi mock objects

The factory class `MockOsgi` allows to instantiate the different mock implementations.

Example:

```java
// get bundle context
BundleContext bundleContext = MockOsgi.newBundleContext();

// get component context
Dictionary<String,Object> properties = new Hashtable<>();
properties.put("prop1", "value1");
BundleContext bundleContext = MockOsgi.newComponentContext(properties);
```

It is possible to simulate registering of OSGi services (backed by a simple hash map internally):

```java
// register service
bundleContext.registerService(MyClass.class, myService, properties);

// get service instance
ServiceReference ref = bundleContext.getServiceReference(MyClass.class.getName());
MyClass service = bundleContext.getService(ref);
```
