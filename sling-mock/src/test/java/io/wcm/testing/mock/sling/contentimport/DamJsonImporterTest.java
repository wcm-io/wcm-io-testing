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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import io.wcm.testing.junit.rules.parameterized.Generator;
import io.wcm.testing.junit.rules.parameterized.GeneratorFactory;
import io.wcm.testing.mock.sling.MockSlingFactory;
import io.wcm.testing.mock.sling.ResourceResolverType;

import java.io.IOException;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class DamJsonImporterTest {

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
      NamespaceRegistry namespaceRegistry = this.resourceResolver.adaptTo(Session.class).getWorkspace().getNamespaceRegistry();
      namespaceRegistry.registerNamespace("sling", "http://mock/sling");
      namespaceRegistry.registerNamespace("cq", "http://mock/cq");
      namespaceRegistry.registerNamespace("dam", "http://mock/dam");
    }

    jsonImporter.importTo("/json-import-samples/dam.json", "/content/dam/sample");
  }

  @Test
  public void testDamAssetMetadata() {
    Resource assetMetadata = this.resourceResolver.getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/metadata");
    ValueMap props = assetMetadata.getValueMap();

    assertEquals("Canon\u0000", props.get("tiff:Make", String.class));
    assertEquals((Long)807L, props.get("tiff:ImageWidth", Long.class));
    assertEquals((Integer)595, props.get("tiff:ImageLength", Integer.class));
    assertEquals(4.64385986328125d, props.get("dam:ApertureValue", Double.class), 0.00000000001d);

    assertArrayEquals(new String[] {
        "stockphotography:business/business_people",
        "properties:style/color",
        "properties:orientation/landscape"
    }, props.get("cq:tags", String[].class));
  }

}
