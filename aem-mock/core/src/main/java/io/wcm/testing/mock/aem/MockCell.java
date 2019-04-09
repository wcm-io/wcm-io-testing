/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import java.util.Iterator;

import com.day.cq.wcm.api.designer.Cell;

/**
 * Mock implementation of {@link Cell}.
 */
class MockCell implements Cell {

  // --- unsupported operations ---

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Cell getParent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getPaths() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getSearchPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getSearchPaths() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<String> paths() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<String> searchPaths() {
    throw new UnsupportedOperationException();
  }

}
