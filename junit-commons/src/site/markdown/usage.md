## Usage

### Parameterized JUnit Rule

The [rules feature][junit-rules] is a hidden gem in JUnit. This library provides
a "parameterized" JUnit Rule, which is an alternative to the runner-based 
[Parameterized tests feature][junit-parametrized-tests] provided by JUnit.
The problem with the runner-based concept is that this cannot be combined with other JUnit runners e.g. for 
[Mockito JUnit Runner][mockito-testrunner]. This rules-based approach can be combined with any Runner or other rules.

Simple example for using the parameterized Generator rule:

```java
public class SimpleExampleTest {

  @Rule
  public Generator<String> params = GeneratorFactory.list("alpha", "beta", "gamma");

  @Test
  public void testSomething() throws Exception {
    assertTrue(this.params.value().length() >= 4);
  }

}
```

The annotated test method `testSomething` is executed once for each value provided in the list argument, the rule's
`value()` method provides access to the current value in the iteration. Using generics any object type can be used 
for the parameter list.

A variant supports providing callbacks for setUp and tearDown methods that should be executed for and after each
parameter test iteration. Although the standard `@Before` and `@After` annotated JUnit methods are supported as well
this is useful if the parameterized rule is wrapped in another rule which has to apply special setUp and tearDown logic.

Example with callbacks:

```java
public class CallbackExampleTest {

  private Callback<String> setUpCallback = new Callback<String>() {
    public void execute(String currentValue) {
      // some actions before starting a test iteration
    }
  };

  private Callback<String> tearDownCallback = new Callback<String>() {
    public void execute(String currentValue) {
      // some actions after starting a test iteration
    }
  };

  @Rule
  public Generator<String> params = new ListGenerator<String>(
      Arrays.asList("alpha", "beta", "gamma"), this.setUpCallback, this.tearDownCallback);

  @Test
  public void testSomething() throws Exception {
    assertTrue(this.params.value().length() >= 4);
  }

}
```

*Acknowledgement: The "parametrized" JUnit rule from the JUnit Commons project is based on work by [Jens Schauder](http://blog.schauderhaft.de/).*


[junit-rules]: https://github.com/junit-team/junit/wiki/Rules
[junit-parametrized-tests]: https://github.com/junit-team/junit/wiki/Parameterized-tests
[mockito-testrunner]: http://docs.mockito.googlecode.com/hg/latest/org/mockito/runners/MockitoJUnitRunner.html
