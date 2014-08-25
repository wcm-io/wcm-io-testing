## Usage

### Getting JCR mock objects

The factory class `MockJcrFactory` allows to instantiate the different mock implementations.

Example:

```java
// get session
Session session = MockJcrFactory.newSession();

// get repository
Repository repository = MockJcrFactory.newRepository();
```

The repository is empty and contains only the root node. You can use the JCR API to fill it with content.
