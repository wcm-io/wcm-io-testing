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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListRuleWithCallbacksTest {

  @SuppressWarnings("unchecked")
  private final Callback<Integer> setUpCallback = mock(Callback.class);

  @SuppressWarnings("unchecked")
  private final Callback<Integer> tearDownCallback = mock(Callback.class);

  @Rule
  public Generator<Integer> generator = new ListGenerator<Integer>(Arrays.asList(new Integer[] {
      23, 42, 5
  }), this.setUpCallback, this.tearDownCallback);

  @Test
  public void allGivenNumbersAreX() {
    Integer currentValue = this.generator.value();
    assertTrue(Arrays.asList(5, 42, 23).contains(currentValue));
    verify(this.setUpCallback, times(1)).execute(currentValue);
  }

}
