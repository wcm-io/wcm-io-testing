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
package io.wcm.testing.mock.jcr;

import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.JcrConstants;

/**
 * Collection of mocked node type instances.
 */
public final class MockNodeTypes {

  private MockNodeTypes() {
    // constants only
  }

  /**
   * Node type NT_UNSTRUCTURED
   */
  public static final NodeType NT_UNSTRUCTURED = new MockNodeType(JcrConstants.NT_UNSTRUCTURED);

  /**
   * Node type NT_FOLDER
   */
  public static final NodeType NT_FOLDER = new MockNodeType(JcrConstants.NT_FOLDER);

  /**
   * Node type NT_FILE
   */
  public static final NodeType NT_FILE = new MockNodeType(JcrConstants.NT_FILE);

}
