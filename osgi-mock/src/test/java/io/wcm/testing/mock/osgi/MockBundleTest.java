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
package io.wcm.testing.mock.osgi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class MockBundleTest {

  private Bundle bundle;

  @Before
  public void setUp() {
    this.bundle = MockOsgiFactory.newBundle();
  }

  @Test
  public void testBundleId() {
    assertTrue(this.bundle.getBundleId() > 0);
  }

  @Test
  public void testBundleContxt() {
    assertNotNull(this.bundle.getBundleContext());
  }

  @Test
  public void testGetEntry() {
    assertNotNull(this.bundle.getEntry("/META-INF/test.txt"));
    assertNull(this.bundle.getEntry("/invalid"));
  }

}
