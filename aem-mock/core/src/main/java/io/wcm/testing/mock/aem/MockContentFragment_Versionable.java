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
package io.wcm.testing.mock.aem;

import java.util.Iterator;

import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.VersionDef;
import com.adobe.cq.dam.cfm.Versionable;
import com.adobe.cq.dam.cfm.VersionedContent;

/**
 * Mock implementation of {@link Versionable}.
 */
class MockContentFragment_Versionable implements Versionable {

  // --- unsupported operations ---

  @Override
  public VersionDef createVersion(String label, String comment) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public VersionedContent getVersionedContent(VersionDef version) throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<VersionDef> listVersions() throws ContentFragmentException {
    throw new UnsupportedOperationException();
  }

}
