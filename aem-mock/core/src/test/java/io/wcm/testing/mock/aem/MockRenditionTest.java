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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Rendition;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockRenditionTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Rendition rendition;

  @Before
  public void setUp() throws Exception {
    context.load().json("/json-import-samples/dam.json", "/content/dam/sample");

    Resource resource = this.context.resourceResolver()
        .getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/original");
    this.rendition = resource.adaptTo(Rendition.class);
  }

  @Test
  public void testProperties() {
    assertEquals("original", rendition.getName());
    assertEquals("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/original", rendition.getPath());
    assertEquals("image/jpeg", rendition.getMimeType());
    assertEquals("admin", rendition.getProperties().get(JcrConstants.JCR_LAST_MODIFIED_BY, String.class));
    assertNotNull(rendition.hashCode());
  }

  @Test
  public void testAsset() {
    assertEquals("/content/dam/sample/portraits/scott_reynolds.jpg", rendition.getAsset().getPath());
  }

  @Test
  public void testStream() {
    assertNotNull(rendition.getStream());
  }

  @Test
  public void testSize() {
    assertEquals(0L, rendition.getSize());
  }

  @Test
  public void testEquals() throws Exception {
    Rendition rendition1 = this.context.resourceResolver()
        .getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/original").adaptTo(Rendition.class);
    Rendition rendition2 = this.context.resourceResolver()
        .getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/original").adaptTo(Rendition.class);
    Rendition rendition3 = this.context.resourceResolver()
        .getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/cq5dam.thumbnail.48.48.png").adaptTo(Rendition.class);

    assertTrue(rendition1.equals(rendition2));
    assertFalse(rendition1.equals(rendition3));
  }

}
