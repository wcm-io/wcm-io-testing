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
package io.wcm.testing.mock.aem.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import io.wcm.testing.junit.rules.parameterized.Generator;
import io.wcm.testing.junit.rules.parameterized.GeneratorFactory;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

public class AemContextImplTest {

  @Rule
  public Generator<ResourceResolverType> resourceResolverType = GeneratorFactory.list(
      ResourceResolverType.JCR_MOCK,
      ResourceResolverType.RESOURCERESOLVER_MOCK
      );

  private AemContextImpl context;

  @Before
  public void setUp() throws Exception {
    this.context = new AemContextImpl();
    this.context.setResourceResolverType(resourceResolverType.value());
    this.context.setUp();

    ContentLoader contentLoader = this.context.load();
    contentLoader.json("/json-import-samples/application.json", "/apps/sample");
    contentLoader.json("/json-import-samples/content.json", "/content/sample/en");
  }

  @After
  public void tearDown() throws Exception {
    this.context.tearDown();
  }

  @Test
  public void testContextObjects() {
    assertNotNull(context.pageManager());
  }

  @Test
  public void testSetCurrentPage() {
    context.currentPage("/content/sample/en/toolbar/profiles");
    assertEquals("/content/sample/en/toolbar/profiles", context.currentPage().getPath());
    assertEquals("/content/sample/en/toolbar/profiles/jcr:content", context.currentResource().getPath());

    context.currentPage(context.pageManager().getPage("/content/sample/en/toolbar"));
    assertEquals("/content/sample/en/toolbar", context.currentPage().getPath());
    assertEquals("/content/sample/en/toolbar/jcr:content", context.currentResource().getPath());

    context.currentPage((Page)null);
    assertNull(context.currentPage());

    context.currentPage((String)null);
    assertNull(context.currentPage());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetCurrentPageNonExisting() {
    context.currentPage("/non/existing");
  }

}
