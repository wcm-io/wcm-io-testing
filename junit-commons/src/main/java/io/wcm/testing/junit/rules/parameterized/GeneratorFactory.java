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

/**
 * Factory class for parameter generator rules.
 */
public final class GeneratorFactory {

  private GeneratorFactory() {
    // static methods only
  }

  /**
   * Generates a {@link ListGenerator} instance.
   * @param values List of values.
   * @return Parameter generator rule
   */
  @SafeVarargs
  public static <T> Generator<T> list(final T... values) {
    return new ListGenerator<T>(asList(values));
  }

}
