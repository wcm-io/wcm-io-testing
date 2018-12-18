/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2018 wcm.io
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

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.testing.mock.sling.NodeTypeDefinitionScanner;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

public final class TestAemContext {

  /**
   * All resource resolver types the unit tests of aem-mock should run with.
   * Without jackrabbit because it required special "unique root" handling.
   */
  public static final @NotNull ResourceResolverType @NotNull [] ALL_TYPES = new @NotNull ResourceResolverType[] {
    ResourceResolverType.JCR_MOCK,
    ResourceResolverType.RESOURCERESOLVER_MOCK,
    ResourceResolverType.JCR_OAK
  };

  private TestAemContext() {
    // static methods only
  }

  public static @NotNull AemContext newAemContext() {
    return new AemContext(new SetUpCallback(), ALL_TYPES);
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final class SetUpCallback implements AemContextCallback {

    @Override
    public void execute(@NotNull AemContext context) throws PersistenceException, IOException {
      try {
        // register manually because in project's unit tests itself MANIFEST.MF is not available
        NodeTypeDefinitionScanner.get().register(context.resourceResolver().adaptTo(Session.class),
            ImmutableList.of("SLING-INF/nodetypes/aem-core-replication.cnd",
                "SLING-INF/nodetypes/aem-tagging.cnd",
                "SLING-INF/nodetypes/aem-commons.cnd",
                "SLING-INF/nodetypes/aem-dam.cnd"),
                context.resourceResolverType().getNodeTypeMode());
      }
      catch (RepositoryException ex) {
        throw new RuntimeException("Unable to register AEM nodetypes: " + ex.getMessage(), ex);
      }
    }

  }

}
