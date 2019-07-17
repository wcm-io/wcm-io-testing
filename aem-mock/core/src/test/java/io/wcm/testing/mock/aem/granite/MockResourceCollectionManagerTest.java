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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.dam.api.DamConstants.NT_DAM_ASSET;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;
import static org.apache.jackrabbit.vault.packaging.JcrPackage.NN_VLT_DEFINITION;
import static org.apache.jackrabbit.vault.packaging.JcrPackage.NT_VLT_PACKAGE_DEFINITION;
import static org.apache.jackrabbit.vault.packaging.JcrPackageDefinition.NN_FILTER;
import static org.apache.jackrabbit.vault.packaging.JcrPackageDefinition.PN_ROOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("null")
public class MockResourceCollectionManagerTest {

  private static final String PKG_ROOT = "/var/workflow/packages";

  @Rule
  public AemContext context = TestAemContext.newAemContext(
      ResourceResolverType.JCR_MOCK,
      ResourceResolverType.JCR_OAK);

  private ResourceCollectionManager underTest;

  private Page samplePage1;
  private Page samplePage2;
  private Asset sampleAsset;

  @Before
  public void setUp() throws Exception {
    underTest = context.getService(ResourceCollectionManager.class);

    samplePage1 = context.create().page("/content/mysite/page1");
    samplePage2 = context.create().page("/content/mysite/page2");
    sampleAsset = context.create().asset("/content/dam/asset1.jpg", 10, 10, "image/jpeg");
  }

  @Test
  public void testCreateCollection_NoPackage() {
    Resource anyResource = context.create().resource(PKG_ROOT + "/anyResource");

    ResourceCollection result = underTest.createCollection(anyResource.adaptTo(Node.class));

    assertNull(result);
  }

  @Test
  public void testCreateCollection_WithPackage() throws RepositoryException {
    createPackage(PKG_ROOT + "/pkg1",
        samplePage1.getPath(),
        sampleAsset.getPath(),
        samplePage2.getPath(),
        "/invalid/path");

    Resource vltDef = context.resourceResolver().getResource(PKG_ROOT + "/pkg1/jcr:content/vlt:definition");
    ResourceCollection result = underTest.createCollection(vltDef.adaptTo(Node.class));

    assertNotNull(result);
    assertEquals(PKG_ROOT + "/pkg1", result.getPath());
    assertPaths(result.list(new String[] { NT_PAGE }), samplePage1.getPath(), samplePage2.getPath());
    assertPaths(result.list(new String[] { NT_DAM_ASSET }), sampleAsset.getPath());
  }

  @Test
  public void testGetCollectionsForNode() throws RepositoryException {
    createPackage(PKG_ROOT + "/pkg1",
        samplePage1.getPath(),
        sampleAsset.getPath());

    createPackage(PKG_ROOT + "/pkg2",
        samplePage2.getPath());

    Resource pkg1 = context.resourceResolver().getResource(PKG_ROOT + "/pkg1");
    Resource pkg2 = context.resourceResolver().getResource(PKG_ROOT + "/pkg2");
    Resource all = context.resourceResolver().getResource(PKG_ROOT);

    List<ResourceCollection> pkg1Collection = underTest.getCollectionsForNode(pkg1.adaptTo(Node.class));
    assertResourceCollections(pkg1Collection, NT_PAGE, samplePage1.getPath());
    assertResourceCollections(pkg1Collection, NT_DAM_ASSET, sampleAsset.getPath());

    List<ResourceCollection> pkg2Collection = underTest.getCollectionsForNode(pkg2.adaptTo(Node.class));
    assertResourceCollections(pkg2Collection, NT_PAGE, samplePage2.getPath());
    assertResourceCollections(pkg2Collection, NT_DAM_ASSET);

    List<ResourceCollection> allCollection = underTest.getCollectionsForNode(all.adaptTo(Node.class));
    assertResourceCollections(allCollection, NT_PAGE, samplePage1.getPath(), samplePage2.getPath());
    assertResourceCollections(allCollection, NT_DAM_ASSET, sampleAsset.getPath());
  }

  private void createPackage(String path, String... filterPaths) {
    Resource page = context.create().resource(path,
        JCR_PRIMARYTYPE, NT_PAGE);
    Resource pageContent = context.create().resource(page, JCR_CONTENT,
        JCR_PRIMARYTYPE, "cq:PageContent");
    Resource vltDef = context.create().resource(pageContent, NN_VLT_DEFINITION,
        JCR_PRIMARYTYPE, NT_VLT_PACKAGE_DEFINITION);
    for (int i = 0; i < filterPaths.length; i++) {
      context.create().resource(vltDef, NN_FILTER + "/item" + i,
          PN_ROOT, filterPaths[i]);
    }
  }

  private void assertPaths(List<Node> nodes, String... paths) throws RepositoryException {
    List<String> nodePaths = new ArrayList<>();
    for (Node node : nodes) {
      nodePaths.add(node.getPath());
    }
    assertEquals(ImmutableList.copyOf(paths), nodePaths);
  }

  private void assertResourceCollections(List<ResourceCollection> resourceCollections,
      String nodeType, String... paths) throws RepositoryException {
    List<String> nodePaths = new ArrayList<>();
    for (ResourceCollection resourceCollection : resourceCollections) {
      for (Node node : resourceCollection.list(new String[] { nodeType })) {
        nodePaths.add(node.getPath());
      }
    }
    assertEquals(ImmutableList.copyOf(paths), nodePaths);
  }

}
