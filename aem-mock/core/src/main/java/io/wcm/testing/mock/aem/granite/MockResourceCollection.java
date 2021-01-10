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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.jetbrains.annotations.NotNull;

import com.adobe.granite.workflow.collection.ResourceCollection;

/**
 * Mock implementation of {@link ResourceCollection}.
 */
class MockResourceCollection implements ResourceCollection {

  private final String packagePath;
  private final WorkspaceFilter packageFilter;
  private final Session session;

  MockResourceCollection(String packagePath, WorkspaceFilter packageFilter, Session session) {
    this.packagePath = packagePath;
    this.packageFilter = packageFilter;
    this.session = session;
  }

  @Override
  public @NotNull List<Node> list(String[] allowedNodesTypes) throws RepositoryException {
    List<Node> nodes = new ArrayList<>();
    FilterPathCollector filterPathCollector = new FilterPathCollector();

    if (packageFilter.getFilterSets().size() == 1) {
      String filterPath = packageFilter.getFilterSets().get(0).getRoot();
      addNode(nodes, filterPath, allowedNodesTypes);
    }
    else {
      packageFilter.dumpCoverage(session, filterPathCollector, true);
      List<String> list = filterPathCollector.getPaths();
      for (String filterPath : list) {
        addNode(nodes, filterPath, allowedNodesTypes);
      }
    }
    return nodes;
  }

  @Override
  public String getPath() {
    return packagePath;
  }

  private void addNode(List<Node> nodes, String path, String[] allowedNodeTypes) throws RepositoryException {
    if (session.nodeExists(path)) {
      Node node = session.getNode(path);
      if (isAllowedNodeType(node, allowedNodeTypes)) {
        nodes.add(node);
      }
    }
  }

  private boolean isAllowedNodeType(Node node, String[] allowedNodesTypes) throws RepositoryException {
    for (String nodeType : allowedNodesTypes) {
      if (node.isNodeType(nodeType)) {
        return true;
      }
    }
    return false;
  }

  private static class FilterPathCollector implements ProgressTrackerListener {
    private final List<String> paths = new ArrayList<>();
    @Override
    public void onMessage(ProgressTrackerListener.Mode mode, String action, String path) {
      paths.add(path);
    }
    @Override
    public void onError(Mode mode, String string, Exception exception) {
      // ignore
    }
    public List<String> getPaths() {
      return paths;
    }
  }


  // --- unsupported operations ---

  @Override
  public void remove(Node node) {
    throw new UnsupportedOperationException();
  }

  // AEM 6.5.5
  @SuppressWarnings("unused")
  public boolean hasNode(String arg0) {
    throw new UnsupportedOperationException();
  }

}
