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

import static org.apache.jackrabbit.vault.packaging.JcrPackage.NT_VLT_PACKAGE_DEFINITION;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.google.common.collect.ImmutableList;

/**
 * Mock implementation of {@link ResourceCollectionManager}.
 */
@Component(service = ResourceCollectionManager.class)
public final class MockResourceCollectionManager implements ResourceCollectionManager {

  private static final Logger log = LoggerFactory.getLogger(MockResourceCollectionManager.class);

  @Reference
  private Packaging packaging;

  @Override
  public ResourceCollection createCollection(Node node) {
    try {
      JcrPackageManager packageManager = packaging.getPackageManager(node.getSession());
      JcrPackage jcrPackage = packageManager.open(node.getParent().getParent());
      if (jcrPackage == null) {
        log.info("Resource collection at {} is not a package.", node.getPath());
        return null;
      }
      return new MockResourceCollection(jcrPackage);
    }
    catch (RepositoryException ex) {
      log.warn("Unable to create collection.", ex);
    }
    return null;
  }

  @Override
  public List<ResourceCollection> getCollectionsForNode(Node baseNode) {
    Map<String, ResourceCollection> resourceCollections = new TreeMap<>();
    try {
      getCollectionsForNode(baseNode, resourceCollections);
    }
    catch (Exception ex) {
      log.warn("Unable to get resource collections for node.", ex);
    }
    return ImmutableList.copyOf(resourceCollections.values());
  }

  private void getCollectionsForNode(Node baseNode, Map<String, ResourceCollection> resourceCollections)
      throws RepositoryException {
    if (baseNode.isNodeType(NT_VLT_PACKAGE_DEFINITION)) {
      ResourceCollection collection = createCollection(baseNode);
      if (collection != null) {
        resourceCollections.put(baseNode.getPath(), collection);
      }
    }
    else {
      NodeIterator nodes = baseNode.getNodes();
      while (nodes.hasNext()) {
        Node node = nodes.nextNode();
        getCollectionsForNode(node, resourceCollections);
      }
    }
  }


  // --- unsupported operations ---

  @Override
  public List<ResourceCollection> getCollections(Session session) {
    throw new UnsupportedOperationException();
  }

}
