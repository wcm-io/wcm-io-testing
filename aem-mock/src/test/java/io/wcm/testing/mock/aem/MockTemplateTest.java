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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Template;

public class MockTemplateTest {

  @Rule
  public AemContext context = new AemContext();

  private Template template;

  @Before
  public void setUp() throws Exception {
    context.contentLoader().importTo("/json-import-samples/application.json", "/apps/sample");

    Resource resource = this.context.resourceResolver().getResource("/apps/sample/templates/homepage");
    this.template = resource.adaptTo(Template.class);
  }

  @Test
  public void testProperties() {
    assertEquals("/apps/sample/templates/homepage", this.template.getPath());
    assertEquals("homepage", this.template.getName());
    assertEquals("Sample Homepage", this.template.getTitle());
    assertEquals("Homepage", this.template.getShortTitle());
    assertNotNull(this.template.getDescription());
    assertNull(this.template.getIconPath());
    assertEquals("/apps/sample/templates/homepage/thumbnail.png", this.template.getThumbnailPath());
    assertEquals((Long)110L, this.template.getRanking());

  }

}
