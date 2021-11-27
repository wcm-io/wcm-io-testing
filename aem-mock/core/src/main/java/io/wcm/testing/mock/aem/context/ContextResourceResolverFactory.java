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
package io.wcm.testing.mock.aem.context;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Create resolve resolver instance and initialize it depending on it's type.
 */
@ProviderType
final class ContextResourceResolverFactory {

  private static final Logger log = LoggerFactory.getLogger(ContextResourceResolverFactory.class);

  private ContextResourceResolverFactory() {
    // static methods only
  }

  public static @NotNull ResourceResolverFactory get(@NotNull final ResourceResolverType resourceResolverType,
      @NotNull final BundleContext bundleContext) {
    try {
      log.debug("Start initialize resource resolver factory, bundleContext={}", bundleContext);
      ResourceResolverFactory factory = MockSling.newResourceResolverFactory(resourceResolverType, bundleContext);

      switch (resourceResolverType) {
        case JCR_MOCK:
          initializeJcrMock(factory);
          break;
        case JCR_OAK:
          initializeJcrOak(factory);
          break;
        case RESOURCERESOLVER_MOCK:
          initializeResourceResolverMock(factory);
          break;
        case NONE:
          initializeResourceResolverNone(factory);
          break;
        default:
          throw new IllegalArgumentException("Invalid resource resolver type: " + resourceResolverType);
      }

      log.debug("Finished initializing resource resolver factory, bundleContext={}", bundleContext);

      return factory;
    }
    /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
      log.error("Failed initializing resource resolver factory, bundleContext={}", bundleContext, ex);
      throw new RuntimeException("Unable to initialize " + resourceResolverType + " resource resolver factory: " + ex.getMessage(), ex);
    }
  }

  private static void initializeJcrMock(ResourceResolverFactory factory) throws RepositoryException, LoginException {
    ResourceResolver resolver = factory.getResourceResolver(null);
    try {
      registerDefaultAemNamespaces(resolver);
    }
    finally {
      resolver.close();
    }
  }

  @SuppressWarnings("unused")
  private static void initializeJcrOak(ResourceResolverFactory factory) {
    // nothing to do - namespaces are registered automatically together with node types
  }

  @SuppressWarnings("unused")
  private static void initializeResourceResolverMock(ResourceResolverFactory factory) {
    // nothing to do
  }

  @SuppressWarnings("unused")
  private static void initializeResourceResolverNone(ResourceResolverFactory factory) {
    // nothing to do
  }

  /**
   * Registers default AEM JCR namespaces.
   * @param resolver Resource resolver
   */
  @SuppressWarnings("null")
  @SuppressFBWarnings("STYLE")
  private static void registerDefaultAemNamespaces(ResourceResolver resolver) throws RepositoryException {
    Session session = resolver.adaptTo(Session.class);
    NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
    namespaceRegistry.registerNamespace("cq", "http://www.day.com/jcr/cq/1.0");
    namespaceRegistry.registerNamespace("dam", "http://www.day.com/dam/1.0 ");
  }

}
