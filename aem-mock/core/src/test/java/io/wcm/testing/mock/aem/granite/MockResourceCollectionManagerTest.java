/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.testing.mock.aem.granite;

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static org.apache.jackrabbit.vault.packaging.JcrPackage.NT_VLT_PACKAGE_DEFINITION;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockResourceCollectionManagerTest {

  private static final String PKG_ROOT = "/var/workflow/packages";

  @Rule
  public AemContext context = TestAemContext.newAemContext(
      ResourceResolverType.JCR_MOCK,
      ResourceResolverType.JCR_OAK);

  private ResourceCollectionManager underTest;

  private Page samplePage;
  private Asset sampleAsset;

  @Before
  public void setUp() throws Exception {
    underTest = context.getService(ResourceCollectionManager.class);

    samplePage = context.create().page("/content/mysite/page1");
    sampleAsset = context.create().asset("/content/dam/asset1.jpg", 10, 10, "image/jpeg");
  }

  @Test
  public void testCreateCollection_NoPackage() {
    Resource anyResource = context.create().resource(PKG_ROOT + "/pkg1");

    ResourceCollection result = underTest.createCollection(anyResource.adaptTo(Node.class));

    assertNull(result);
  }

  @Test
  public void testCreateCollection_WithPackage() {
    Resource pkg = createPackage(PKG_ROOT + "/pkg1",
        samplePage.getPath(),
        sampleAsset.getPath());

    ResourceCollection result = underTest.createCollection(pkg.adaptTo(Node.class));

    assertNotNull(result);
  }

  @Test
  public void testGetCollectionsForNode() {
    // TODO: implement test
  }

  private Resource createPackage(String path, String... filterPaths) {
    context.create().resource(path,
        JCR_PRIMARYTYPE, NameConstants.NT_PAGE);
    context.create().resource(path + "/jcr:content",
        JCR_PRIMARYTYPE, "cq:PageContent");
    Resource definition = context.create().resource(path + "/jcr:content/vlt:definition",
        JCR_PRIMARYTYPE, NT_VLT_PACKAGE_DEFINITION);
    for (int i = 0; i < filterPaths.length; i++) {
      context.create().resource(definition.getPath() + "/filter/item" + i,
          "root", filterPaths[i]);
    }
    return definition;
  }

}
