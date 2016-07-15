/* Copyright (c) pro!vision GmbH. All rights reserved. */
package io.wcm.testing.mock.aem.context;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.testing.mock.sling.NodeTypeDefinitionScanner;
import org.apache.sling.testing.mock.sling.ResourceResolverType;

import com.google.common.collect.ImmutableList;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

public final class TestAemContext {

  /**
   * All resource resolver types the unit tests of aem-mock should run with.
   * Without jackrabbit because it required special "unique root" handling.
   */
  public static final ResourceResolverType[] ALL_TYPES = new ResourceResolverType[] {
    ResourceResolverType.JCR_MOCK,
    ResourceResolverType.RESOURCERESOLVER_MOCK,
    ResourceResolverType.JCR_OAK
  };

  private TestAemContext() {
    // static methods only
  }

  public static AemContext newAemContext() {
    return new AemContext(new SetUpCallback(), ALL_TYPES);
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final class SetUpCallback implements AemContextCallback {

    @Override
    public void execute(AemContext context) throws PersistenceException, IOException {
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
