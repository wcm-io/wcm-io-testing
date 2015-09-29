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
package io.wcm.testing.mock.aem.junit;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import io.wcm.testing.mock.aem.context.TestAemContext;

import java.io.IOException;

import org.apache.sling.api.resource.PersistenceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AemContextTest {

  private final AemContextCallback contextSetup = mock(AemContextCallback.class);
  private final AemContextCallback contextTeardown = mock(AemContextCallback.class);

  // Run all unit tests for each resource resolver types listed here
  @Rule
  public AemContext context = new AemContext(
      contextSetup,
      contextTeardown,
      TestAemContext.ALL_TYPES
      );

  @Before
  public void setUp() throws IOException, PersistenceException {
    verify(contextSetup).execute(context);
  }

  @Test
  public void testRequest() {
    assertNotNull(context.request());
  }

  @After
  public void tearDown() {
    // reset required because mockito gets puzzled with the parameterized JUnit rule
    // TODO: better solution?
    reset(contextSetup);
  }

}
