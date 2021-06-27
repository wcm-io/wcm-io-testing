/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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

import static com.day.cq.commons.Externalizer.AUTHOR;
import static com.day.cq.commons.Externalizer.LOCAL;
import static com.day.cq.commons.Externalizer.PUBLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.Externalizer;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockExternalizerTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private ResourceResolver resolver;

  private MockExternalizer underTest;

  @Before
  public void setUp() {
    resolver = context.resourceResolver();
    underTest = (MockExternalizer)context.getService(Externalizer.class);
    assertNotNull(underTest);
  }

  @Test
  public void testExternalLink() {
    assertEquals("http://localhost:4502/mypath", underTest.externalLink(resolver, LOCAL, "/mypath"));
    assertEquals("http://localhost:4502/mypath", underTest.externalLink(resolver, AUTHOR, "/mypath"));
    assertEquals("http://localhost:4503/mypath", underTest.externalLink(resolver, PUBLISH, "/mypath"));

    assertEquals("https://localhost:4502/mypath", underTest.externalLink(resolver, LOCAL, "https", "/mypath"));
    assertEquals("https://localhost:4502/mypath", underTest.externalLink(resolver, AUTHOR, "https", "/mypath"));
    assertEquals("https://localhost:4503/mypath", underTest.externalLink(resolver, PUBLISH, "https", "/mypath"));
  }

  @Test
  public void testExternalLink_CustomMapping() {
    underTest.setMapping(LOCAL, "http://lh:4502");
    underTest.setMapping(AUTHOR, "https://author.sample.com");
    underTest.setMapping(PUBLISH, "https://www.sample.com");
    underTest.setMapping("custom", "http://custom.sample.com");

    assertEquals("http://lh:4502/mypath", underTest.externalLink(resolver, LOCAL, "/mypath"));
    assertEquals("https://author.sample.com/mypath", underTest.externalLink(resolver, AUTHOR, "/mypath"));
    assertEquals("https://www.sample.com/mypath", underTest.externalLink(resolver, PUBLISH, "/mypath"));
    assertEquals("http://custom.sample.com/mypath", underTest.externalLink(resolver, "custom", "/mypath"));

    assertEquals("https://lh:4502/mypath", underTest.externalLink(resolver, LOCAL, "https", "/mypath"));
    assertEquals("https://author.sample.com/mypath", underTest.externalLink(resolver, AUTHOR, "https", "/mypath"));
    assertEquals("https://www.sample.com/mypath", underTest.externalLink(resolver, PUBLISH, "https", "/mypath"));
    assertEquals("https://custom.sample.com/mypath", underTest.externalLink(resolver, "custom", "https", "/mypath"));
  }

  @Test
  public void testExternalLink_NonExisingMapping() {
    assertThrows(IllegalArgumentException.class, () -> {
      underTest.externalLink(resolver, "non-existing", "/mypath");
    });
    assertThrows(IllegalArgumentException.class, () -> {
      underTest.externalLink(resolver, "non-existing", "https", "/mypath");
    });
  }

  @Test
  public void testAuthorLink() {
    assertEquals("http://localhost:4502/mypath", underTest.authorLink(resolver, "/mypath"));
    assertEquals("https://localhost:4502/mypath", underTest.authorLink(resolver, "https", "/mypath"));
  }

  @Test
  public void testPublishLink() {
    assertEquals("http://localhost:4503/mypath", underTest.publishLink(resolver, "/mypath"));
    assertEquals("https://localhost:4503/mypath", underTest.publishLink(resolver, "https", "/mypath"));
  }

  @Test
  public void testAbsoluteLink() {
    assertEquals("http://localhost:4502/mypath", underTest.absoluteLink(resolver, "http", "/mypath"));
    assertEquals("https://localhost:80/mypath", underTest.absoluteLink(context.request(), "https", "/mypath"));
    assertEquals("http://localhost:4502/mypath", underTest.absoluteLink("http", "/mypath"));
  }

  @Test
  public void testRelativeLink() {
    assertEquals("/mypath", underTest.relativeLink(context.request(), "/mypath"));
  }

}
