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
import java.util.List;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Parameter generator rule based on a list of values.
 * @param <T> Parameter type
 */
public final class ListGenerator<T> implements Generator<T> {

  private final ValueContainer<T> currentValue = new ValueContainer<T>();
  private final AccessibleErrorCollector errorCollector = new AccessibleErrorCollector();
  private final List<T> values;

  private final Callback<T> setUpCallback;
  private final Callback<T> tearDownCallback;

  /**
   * @param values Parameter values
   */
  public ListGenerator(final List<T> values) {
    this(values, null, null);
  }

  /**
   * @param values Parameter values
   * @param setUpCallback Callback method that is called for each value iteration before test method is executed.
   * @param tearDownCallback Callback method that is called for each value iteration after test method was executed.
   */
  public ListGenerator(final List<T> values, final Callback<T> setUpCallback, final Callback<T> tearDownCallback) {
    this.values = values;
    this.setUpCallback = setUpCallback;
    this.tearDownCallback = tearDownCallback;
  }

  @Override
  public T value() {
    return this.currentValue.get();
  }

  @Override
  public Statement apply(final Statement test, final Description description) {
    return new RepeatedStatement<T>(test,
        new SyncingIterable<T>(this.values, this.currentValue),
        this.errorCollector,
        this.setUpCallback, this.tearDownCallback);
  }

}
