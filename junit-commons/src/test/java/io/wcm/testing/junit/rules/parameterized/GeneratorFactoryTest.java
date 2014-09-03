/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.testing.junit.rules.parameterized;

/*
 * (C) 2012 Jens Schauder http://blog.schauderhaft.de/
 * Code initially published here https://github.com/schauder/parameterizedTestsWithRules
 * Slightly simplified for wcm.io.
 */
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runners.model.Statement;

//CHECKSTYLE:OFF
public class GeneratorFactoryTest {

  public class CollectingStatement<T> extends Statement {

    public Set<T> values = new HashSet<>();
    private final Generator<T> g;

    public CollectingStatement(final Generator<T> g) {
      this.g = g;
    }

    @Override
    public void evaluate() {
      this.values.add(this.g.value());
    }
  }

  private class ExceptionThrowingStatement extends Statement {

    @Override
    public void evaluate() {
      throw new RuntimeException();
    }

  }

  private class CountingStatement extends Statement {

    private int count = 0;

    @Override
    public void evaluate() {
      this.count++;
    }
  }

  @Test
  public void generatorWithNElementsRunsTheTestNtimes() throws Throwable {
    Generator<Integer> g = GeneratorFactory.list(1, 2, 3, 4);
    CountingStatement probe = new CountingStatement();

    g.apply(probe, null).evaluate();

    assertThat(probe.count, equalTo(4));
  }

  @Test
  public void statementSeesAllValues() throws Throwable {
    Generator<Integer> g = GeneratorFactory.list(1, 2, 3, 4);
    CollectingStatement<Integer> probe = new CollectingStatement<>(g);

    g.apply(probe, null).evaluate();

    assertThat(probe.values,
        CoreMatchers.equalTo((Set)new HashSet<>(asList(1, 2, 3, 4))));
  }

  @Test(expected = Throwable.class)
  public void failuresGetPropagated() throws Throwable {
    Generator<Integer> g = GeneratorFactory.list(1);
    Statement probe = new ExceptionThrowingStatement();

    g.apply(probe, null).evaluate();
  }

  @Test
  public void failureMessageContainsGeneratorValueWhenAnExceptionIsThrown() throws Throwable {
    Generator<String> g = GeneratorFactory.list("xXx");
    Statement probe = new ExceptionThrowingStatement();

    try {
      g.apply(probe, null).evaluate();
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage(), e.getMessage().contains("xXx"));
    }
  }

}
