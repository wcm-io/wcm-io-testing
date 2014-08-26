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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

public class CallbackExampleTest {

  private final Callback<String> setUpCallback = new Callback<String>() {
    @Override
    public void execute(final String currentValue) {
      // some actions before starting a test iteration
    }

  };

  private final Callback<String> tearDownCallback = new Callback<String>() {
    @Override
    public void execute(final String currentValue) {
      // some actions after starting a test iteration
    }
  };

  @Rule
  public Generator<String> params = new ListGenerator<String>(
      Arrays.asList("alpha", "beta", "gamma"),
      this.setUpCallback,
      this.tearDownCallback
      );

  @Test
  public void testSomething() throws Exception {
    assertTrue(this.params.value().length() >= 4);
  }

}
