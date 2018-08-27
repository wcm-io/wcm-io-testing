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
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
  private boolean isSetUp;

  /**
   * Initialize AEM context.
   */
  public AemContext() {
    this(new ContextPlugins(), null, true, MockSling.DEFAULT_RESOURCERESOLVER_TYPE);
  }

  /**
   * Initialize AEM context.
   * @param resourceResolverType Resource resolver type.
   */
  public AemContext(@NotNull final ResourceResolverType resourceResolverType) {
    this(new ContextPlugins(), null, true, resourceResolverType);
  }

  /**
   * Initialize AEM context.
   * @param contextPlugins Context plugins
   * @param resourceResolverFactoryActivatorProps Resource resolver factory activator properties
   * @param registerSlingModelsFromClassPath Automatic registering of all Sling Models found in the classpath on
   *          startup.
   * @param resourceResolverType Resource resolver type.
   */
  AemContext(@NotNull final ContextPlugins contextPlugins,
      @Nullable final Map<String, Object> resourceResolverFactoryActivatorProps,
      final boolean registerSlingModelsFromClassPath,
      @Nullable final ResourceResolverType resourceResolverType) {

    this.plugins = contextPlugins;

    // set custom ResourceResolverFactoryActivator config, but set AEM default values for all parameter not given here
    Map<String, Object> mergedProps = resourceResolverFactoryActivatorPropsMergeWithAemDefault(resourceResolverFactoryActivatorProps);
    setResourceResolverFactoryActivatorProps(mergedProps);
    setRegisterSlingModelsFromClassPath(registerSlingModelsFromClassPath);

    // set resource resolver type
    setResourceResolverType(resourceResolverType);
  }

  /**
   * This is called by {@link AemContextExtension} to set up context.
   */
  protected void setUpContext() {
    isSetUp = true;
    plugins.executeBeforeSetUpCallback(this);
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

  boolean isSetUp() {
    return this.isSetUp;
  }

}
