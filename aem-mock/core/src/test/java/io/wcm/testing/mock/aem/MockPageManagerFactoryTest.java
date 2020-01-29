/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageManagerFactory;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockPageManagerFactoryTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private PageManagerFactory underTest;

  @Before
  public void setUp() {
    context.create().page("/content/test");

    underTest = context.getService(PageManagerFactory.class);
  }

  @Test
  public void testGetPageManager() {
    PageManager pageManager = underTest.getPageManager(context.resourceResolver());
    assertNotNull(pageManager);

    Page page = pageManager.getPage("/content/test");
    assertNotNull(page);
  }

}
