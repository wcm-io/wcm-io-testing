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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.sling.ResourceResolverType;
import io.wcm.testing.mock.sling.contentimport.JsonImporter;

import java.io.IOException;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;

public class MockAemAdapterFactoryTest {

  // Run all unit tests for each resource resolver types listed here
  @Rule
  public AemContext context = new AemContext(
      ResourceResolverType.JCR_MOCK,
      ResourceResolverType.RESOURCERESOLVER_MOCK
      );

  @Before
  public void setUp() throws PersistenceException, IOException {
    JsonImporter jsonImporter = this.context.jsonImporter();
    jsonImporter.importTo("/json-import-samples/application.json", "/apps/sample");
    jsonImporter.importTo("/json-import-samples/content.json", "/content/sample/en");
  }

  @Test
  public void testPageManager() {
    PageManager pageManager = this.context.pageManager();
    assertNotNull(pageManager);

    Page page = pageManager.getPage("/content/sample/en");
    assertNotNull(page);
  }

  public void testPage() {
    Resource sample = this.context.resourceResolver().getResource("/content/sample");
    assertNull(sample.adaptTo(Page.class));

    Resource en = this.context.resourceResolver().getResource("/content/sample/en");
    assertNotNull(en.adaptTo(Page.class));

    Resource enContent = this.context.resourceResolver().getResource("/content/sample/en/jcr:content");
    assertNull(enContent.adaptTo(Page.class));
  }

  @Test
  public void testTemplate() {
    Resource homepage = this.context.resourceResolver().getResource("/apps/sample/templates/homepage");
    assertNotNull(homepage.adaptTo(Template.class));

    Resource component = this.context.resourceResolver().getResource("/apps/sample/components/title");
    assertNull(component.adaptTo(Template.class));
  }

}
