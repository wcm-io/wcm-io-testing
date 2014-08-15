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
import org.junit.runners.model.Statement;

class RepeatedStatement<T> extends Statement {

  private final Statement test;
  private final Iterable<T> values;
  private final AccessibleErrorCollector errorCollector;

  private final Callback<T> setUpCallback;
  private final Callback<T> tearDownCallback;

  public RepeatedStatement(final Statement test, final Iterable<T> values,
      final AccessibleErrorCollector errorCollector,
      final Callback<T> setUpCallback, final Callback<T> tearDownCallback) {
    this.test = test;
    this.values = values;
    this.errorCollector = errorCollector;
    this.setUpCallback = setUpCallback;
    this.tearDownCallback = tearDownCallback;
  }

  @Override
  // CHECKSTYLE:OFF
  public void evaluate() throws Throwable {
    // CHECKSTYLE:ON
    for (T v : this.values) {
      try {
        if (this.setUpCallback != null) {
          this.setUpCallback.execute(v);
        }
        this.test.evaluate();
        if (this.tearDownCallback != null) {
          this.tearDownCallback.execute(v);
        }
      }
      catch (Throwable t) {
        this.errorCollector.addError(new AssertionError("For value: "
            + v, t));
      }
    }
    this.errorCollector.verify();
  }
}
