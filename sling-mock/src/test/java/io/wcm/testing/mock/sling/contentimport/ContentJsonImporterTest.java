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
package io.wcm.testing.mock.sling.contentimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import io.wcm.testing.junit.rules.parameterized.Generator;
import io.wcm.testing.junit.rules.parameterized.GeneratorFactory;
import io.wcm.testing.mock.sling.MockSlingFactory;
import io.wcm.testing.mock.sling.ResourceResolverType;

import java.io.IOException;
import java.util.Calendar;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class ContentJsonImporterTest {

  //CHECKSTYLE:OFF
  // Run all unit tests for each resource resolver typ listed here
  @Rule
  public final Generator<ResourceResolverType> resourceResolverType = GeneratorFactory.list(
      ResourceResolverType.RESOURCERESOLVER_MOCK,
      ResourceResolverType.JCR_MOCK
      );
  //CHECKSTYLE:ON

  private ResourceResolver resourceResolver;

  @Before
  public void setUp() throws RepositoryException, PersistenceException, IOException {
    this.resourceResolver = MockSlingFactory.newResourceResolver(this.resourceResolverType.value());
    JsonImporter jsonImporter = new JsonImporter(this.resourceResolver);

    if (this.resourceResolverType.value() == ResourceResolverType.JCR_MOCK) {
      // dummy namespace registrations to make sure sling JCR resolver does not get mixed up with the prefixes
      NamespaceRegistry namespaceRegistry = this.resourceResolver.adaptTo(Session.class).getWorkspace().getNamespaceRegistry();
      namespaceRegistry.registerNamespace("sling", "http://mock/sling");
      namespaceRegistry.registerNamespace("cq", "http://mock/cq");
      namespaceRegistry.registerNamespace("dam", "http://mock/dam");
    }

    jsonImporter.importTo("/json-import-samples/content.json", "/content/sample/en");
  }

  @Test
  public void testPageResourceType() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en");
    if (this.resourceResolverType.value() == ResourceResolverType.JCR_MOCK
        || this.resourceResolverType.value() == ResourceResolverType.JCR_JACKRABBIT) {
      assertEquals("cq:Page", resource.getResourceType());
    }
    else {
      assertNull(resource.getResourceType());
    }
  }

  @Test
  public void testPageJcrPrimaryType() throws RepositoryException {
    Resource resource = this.resourceResolver.getResource("/content/sample/en");
    assertPrimaryNodeType(resource, "cq:Page");
  }

  @Test
  public void testPageContentResourceType() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/toolbar/profiles/jcr:content");
    assertEquals("sample/components/contentpage", resource.getResourceType());
  }

  @Test
  public void testPageContentJcrPrimaryType() throws RepositoryException {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/toolbar/profiles/jcr:content");
    assertPrimaryNodeType(resource, "cq:PageContent");
  }

  @Test
  public void testPageContentProperties() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/toolbar/profiles/jcr:content");
    ValueMap props = resource.getValueMap();
    assertEquals(true, props.get("hideInNav", Boolean.class));

    Calendar calendar = props.get("cq:lastModified", Calendar.class);
    assertNotNull(calendar);
    assertEquals(2009, calendar.get(Calendar.YEAR));
    assertEquals(10, calendar.get(Calendar.MONTH));
    assertEquals(5, calendar.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  public void testContentResourceType() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/jcr:content/header");
    assertEquals("sample/components/header", resource.getResourceType());
  }

  @Test
  public void testContentJcrPrimaryType() throws RepositoryException {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/jcr:content/header");
    assertPrimaryNodeType(resource, JcrConstants.NT_UNSTRUCTURED);
  }

  @Test
  public void testContentProperties() {
    Resource resource = this.resourceResolver.getResource("/content/sample/en/jcr:content/header");
    ValueMap props = resource.getValueMap();
    assertEquals("/content/dam/sample/header.png", props.get("imageReference", String.class));
  }

  private void assertPrimaryNodeType(final Resource resource, final String nodeType) throws RepositoryException {
    Node node = resource.adaptTo(Node.class);
    if (node != null) {
      assertEquals(nodeType, node.getPrimaryNodeType().getName());
    }
    else {
      ValueMap props = resource.getValueMap();
      assertEquals(nodeType, props.get(JcrConstants.JCR_PRIMARYTYPE));
    }
  }

}
