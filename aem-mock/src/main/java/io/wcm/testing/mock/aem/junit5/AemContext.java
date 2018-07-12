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
package io.wcm.testing.mock.aem.junit5;

import java.util.Map;

import org.apache.sling.testing.mock.osgi.context.ContextPlugins;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.testing.mock.aem.context.AemContextImpl;

/**
 * AEM Mock parameter object with resource resolver type
 * defaulting to {@link ResourceResolverType#RESOURCERESOLVER_MOCK}.
 * <p>
 * Additionally you can subclass this class and provide further parameters
 * via {@link AemContextBuilder}.
 * </p>
 * <p>
 * For convenience a set of subclasses already exist to use different resource resolver types.
 * </p>
 * @see ResourceResolverMockAemContext
 * @see JcrMockAemContext
 * @see JcrOakAemContext
 * @see NoResourceResolverTypeAemContext
 */
@ConsumerType
public class AemContext extends AemContextImpl {

  private final ContextPlugins plugins;

  /**
   * Initialize AEM context.
   * @param resourceResolverType Resource resolver type.
   */
  protected AemContext(final ResourceResolverType resourceResolverType) {
    this(new ContextPlugins(), null, resourceResolverType);
  }

  /**
   * Initialize AEM context.
   * @param builder AEM context builder
   */
  protected AemContext(final AemContextBuilder builder) {
    this(builder.getPlugins(),
        builder.getResourceResolverFactoryActivatorProps(),
        // TODO: junit5: add support for multiple resource resolver types
        builder.getResourceResolverTypes()[0]);
  }

  /**
   * Initialize AEM context.
   * @param contextPlugins Context plugins
   * @param resourceResolverFactoryActivatorProps Resource resolver factory activator properties
   * @param resourceResolverType Resource resolver type.
   */
  AemContext(final ContextPlugins contextPlugins,
      final Map<String, Object> resourceResolverFactoryActivatorProps,
      final ResourceResolverType resourceResolverType) {

    this.plugins = contextPlugins;

    // set custom ResourceResolverFactoryActivator config, but set AEM default values for all parameter not given here
    Map<String, Object> mergedProps = resourceResolverFactoryActivatorPropsMergeWithAemDefault(resourceResolverFactoryActivatorProps);
    setResourceResolverFactoryActivatorProps(mergedProps);

    setResourceResolverType(resourceResolverType);
  }

  /**
   * This is called by {@link AemContextExtension} to set up context.
   */
  protected void setUpContext() {
    super.setUp();
  }

  /**
   * This is called by {@link AemContextExtension} to tear down context.
   */
  protected void tearDownContext() {
    super.tearDown();
  }

  ContextPlugins getContextPlugins() {
    return plugins;
  }

}
