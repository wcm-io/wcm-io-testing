/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.testing.mock.aem.context;

import static org.apache.sling.jcr.resource.JcrResourceConstants.NT_SLING_ORDERED_FOLDER;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Manages unique root paths in JCR repository.
 * This is important for resource resolver types like JCR_JACKRABBIT
 * where the repository is not cleaned for each test run. This class provides
 * unique root paths for each run, and cleans them up when done.
 */
@ProviderType
public final class UniqueRoot extends org.apache.sling.testing.mock.sling.context.UniqueRoot {

  private Resource damRoot;

  UniqueRoot(AemContextImpl context) {
    super(context);
  }

  /**
   * Gets (and creates if required) a unique path at <code>/content/dam/xxx</code>.
   * The path (incl. all children) is automatically removed when the unit test completes.
   * @return Unique content path
   */
  public String dam() {
    if (damRoot == null) {
      damRoot = getOrCreateResource("/content/dam/" + uniquePathPart, NT_SLING_ORDERED_FOLDER);
    }
    return damRoot.getPath();
  }

  @Override
  protected void cleanUp() {
    deleteResources(damRoot);
    super.cleanUp();
  }

}
