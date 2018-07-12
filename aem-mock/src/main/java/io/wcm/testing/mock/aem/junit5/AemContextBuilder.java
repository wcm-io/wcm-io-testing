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

import org.apache.sling.testing.mock.osgi.context.ContextCallback;
import org.apache.sling.testing.mock.osgi.context.ContextPlugin;
import org.apache.sling.testing.mock.osgi.context.ContextPlugins;
import org.apache.sling.testing.mock.osgi.context.OsgiContextImpl;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Builder class for creating {@link AemContext} instances with different sets of parameters.
 */
@ProviderType
public final class AemContextBuilder {

  private final ContextPlugins plugins = new ContextPlugins();
  private ResourceResolverType[] resourceResolverTypes;
  private Map<String, Object> resourceResolverFactoryActivatorProps;

  /**
   * Create builder with default resource resolver type.
   */
  public AemContextBuilder() {
    // use default resource resolver type
  }

  /**
   * Create builder with given resource resolver type.
   * @param resourceResolverTypes Resource resolver type(s).
   */
  public AemContextBuilder(ResourceResolverType... resourceResolverTypes) {
    this.resourceResolverType(resourceResolverTypes);
  }

  /**
   * @param types Resource resolver type(s).
   * @return this
   */
  public AemContextBuilder resourceResolverType(ResourceResolverType... types) {
    this.resourceResolverTypes = types;
    return this;
  }

  /**
   * @param <T> context type
   * @param plugin Context plugin which listens to context lifecycle events.
   * @return this
   */
  @SafeVarargs
  public final <T extends OsgiContextImpl> AemContextBuilder plugin(ContextPlugin<T>... plugin) {
    plugins.addPlugin(plugin);
    return this;
  }

  /**
   * @param <T> context type
   * @param beforeSetUpCallback Allows the application to register an own callback function that is called before the
   *          built-in setup rules are executed.
   * @return this
   */
  @SafeVarargs
  public final <T extends OsgiContextImpl> AemContextBuilder beforeSetUp(ContextCallback<T>... beforeSetUpCallback) {
    plugins.addBeforeSetUpCallback(beforeSetUpCallback);
    return this;
  }

  /**
   * @param <T> context type
   * @param afterSetUpCallback Allows the application to register an own callback function that is called after the
   *          built-in setup rules are executed.
   * @return this
   */
  @SafeVarargs
  public final <T extends OsgiContextImpl> AemContextBuilder afterSetUp(ContextCallback<T>... afterSetUpCallback) {
    plugins.addAfterSetUpCallback(afterSetUpCallback);
    return this;
  }

  /**
   * @param <T> context type
   * @param beforeTearDownCallback Allows the application to register an own callback function that is called before the
   *          built-in teardown rules are executed.
   * @return this
   */
  @SafeVarargs
  public final <T extends OsgiContextImpl> AemContextBuilder beforeTearDown(ContextCallback<T>... beforeTearDownCallback) {
    plugins.addBeforeTearDownCallback(beforeTearDownCallback);
    return this;
  }

  /**
   * @param <T> context type
   * @param afterTearDownCallback Allows the application to register an own callback function that is after before the
   *          built-in teardown rules are executed.
   * @return this
   */
  @SafeVarargs
  public final <T extends OsgiContextImpl> AemContextBuilder afterTearDown(ContextCallback<T>... afterTearDownCallback) {
    plugins.addAfterTearDownCallback(afterTearDownCallback);
    return this;
  }

  /**
   * Allows to override OSGi configuration parameters for the Resource Resolver Factory Activator service.
   * @param props Configuration properties
   * @return this
   */
  public AemContextBuilder resourceResolverFactoryActivatorProps(Map<String, Object> props) {
    this.resourceResolverFactoryActivatorProps = props;
    return this;
  }

  ResourceResolverType[] getResourceResolverTypes() {
    return this.resourceResolverTypes;
  }

  Map<String, Object> getResourceResolverFactoryActivatorProps() {
    return this.resourceResolverFactoryActivatorProps;
  }

  ContextPlugins getPlugins() {
    return this.plugins;
  }

}
