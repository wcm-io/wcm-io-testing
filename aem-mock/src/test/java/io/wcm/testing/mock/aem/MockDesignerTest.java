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
package io.wcm.testing.mock.aem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.designer.Designer;

public class MockDesignerTest {

  @Rule
  public AemContext context = new AemContext(AemContextTest.ALL_TYPES);

  private Designer underTest;
  private Page page;

  @Before
  public void setUp() {
    underTest = context.resourceResolver().adaptTo(Designer.class);
    page = context.create().page("/content/page1");
  }

  @Test
  public void testGetDesignPath() {
    assertNull(underTest.getDesignPath(page));
  }

  @Test
  public void testGetDesignPage() {
    assertNull(underTest.getDesign(page));
  }

  @Test
  public void testHasDesign() {
    assertFalse(underTest.hasDesign("/any/id"));
  }

  @Test
  public void testGetDesignString() {
    assertNull(underTest.getDesign("/any/id"));
  }

  @Test
  public void testGetStyleResource() {
    assertNull(underTest.getStyle(page.getContentResource()));
  }

  @Test
  public void testGetStyleResourceString() {
    assertNull(underTest.getStyle(page.getContentResource(), "anyCell"));
  }

}
