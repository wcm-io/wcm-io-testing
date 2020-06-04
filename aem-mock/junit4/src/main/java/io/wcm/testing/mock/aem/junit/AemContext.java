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
package io.wcm.testing.mock.aem.junit;

import java.util.Arrays;
import java.util.Map;

import org.apache.sling.testing.mock.osgi.context.ContextCallback;
import org.apache.sling.testing.mock.osgi.context.ContextPlugins;
import org.apache.sling.testing.mock.osgi.context.OsgiContextImpl;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.testing.junit.rules.parameterized.Callback;
import io.wcm.testing.junit.rules.parameterized.ListGenerator;
import io.wcm.testing.mock.aem.context.AemContextImpl;

/**
 * JUnit rule for setting up and tearing down AEM context objects for unit tests.
 * You can use {@link AemContextBuilder} alternatively to the constructors on this class - it offers
 * more options and fine-grained control about setting up the test context.
 */
@ProviderType
public final class AemContext extends AemContextImpl implements TestRule {

  private final ContextPlugins plugins;
  private final ResourceResolverType[] resourceResolverTypes;
  private final TestRule delegate;

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.</li>
   * </ul>
   * @param resourceResolverTypes Resource resolver type(s).
   */
  public AemContext(@NotNull final ResourceResolverType @NotNull... resourceResolverTypes) {
    this(new ContextPlugins(), null, resourceResolverTypes);
  }

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.
   * </li>
   * </ul>
   * @param <T> context type
   * @param afterSetUpCallback Allows the application to register an own callback function that is called after the
   *          built-in setup rules are executed.
   * @param resourceResolverTypes Resource resolver type(s).
   */
  public <T extends OsgiContextImpl> AemContext(@NotNull final ContextCallback<T> afterSetUpCallback,
      @NotNull final ResourceResolverType @NotNull... resourceResolverTypes) {
    this(new ContextPlugins(afterSetUpCallback), null, resourceResolverTypes);
  }

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.
   * </li>
   * </ul>
   * @param <U> context type
   * @param <V> context type
   * @param afterSetUpCallback Allows the application to register an own callback function that is called after the
   *          built-in setup rules are executed.
   * @param beforeTearDownCallback Allows the application to register an own callback function that is called before the
   *          built-in teardown rules are executed.
   * @param resourceResolverTypes Resource resolver type(s).
   */
  public <U extends OsgiContextImpl, V extends OsgiContextImpl> AemContext(@NotNull final ContextCallback<U> afterSetUpCallback,
      @NotNull final ContextCallback<V> beforeTearDownCallback,
      @NotNull final ResourceResolverType @NotNull... resourceResolverTypes) {
    this(new ContextPlugins(afterSetUpCallback, beforeTearDownCallback), null, resourceResolverTypes);
  }

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.
   * </li>
   * </ul>
   * @param contextPlugins Context plugins
   * @param resourceResolverFactoryActivatorProps Resource resolver factory activator properties
   * @param resourceResolverTypes Resource resolver type(s).
   */
  AemContext(@NotNull final ContextPlugins contextPlugins,
      @Nullable final Map<String, Object> resourceResolverFactoryActivatorProps,
      @NotNull final ResourceResolverType @Nullable... resourceResolverTypes) {
    this(contextPlugins, resourceResolverFactoryActivatorProps, true, resourceResolverTypes);
  }

  /**
   * Initialize AEM context.
   * <p>
   * If context is initialized with:
   * </p>
   * <ul>
   * <li>No resource resolver type - default is used {@link MockSling#DEFAULT_RESOURCERESOLVER_TYPE}.</li>
   * <li>One resource resolver type - exactly this is used.</li>
   * <li>More than one: all unit test methods are executed for all resource resolver types using {@link ListGenerator}.
   * </li>
   * </ul>
   * @param contextPlugins Context plugins
   * @param resourceResolverFactoryActivatorProps Resource resolver factory activator properties
   * @param registerSlingModelsFromClassPath Automatic registering of all Sling Models found in the classpath on
   *          startup.
   * @param resourceResolverTypes Resource resolver type(s).
   */
  AemContext(@NotNull final ContextPlugins contextPlugins,
      @Nullable final Map<String, Object> resourceResolverFactoryActivatorProps,
      final boolean registerSlingModelsFromClassPath,
      @NotNull final ResourceResolverType @Nullable... resourceResolverTypes) {

    this.plugins = contextPlugins;

    // set custom ResourceResolverFactoryActivator config, but set AEM default values for all parameter not given here
    Map<String, Object> mergedProps = resourceResolverFactoryActivatorPropsMergeWithAemDefault(resourceResolverFactoryActivatorProps);
    setResourceResolverFactoryActivatorProps(mergedProps);
    setRegisterSlingModelsFromClassPath(registerSlingModelsFromClassPath);

    if (resourceResolverTypes == null || resourceResolverTypes.length == 0) {
      this.resourceResolverTypes = new ResourceResolverType[] {
          MockSling.DEFAULT_RESOURCERESOLVER_TYPE
      };
    }
    else {
      this.resourceResolverTypes = resourceResolverTypes;
    }

    if (this.resourceResolverTypes.length == 1) {
      // use default rule that directly executes each test method once
      setResourceResolverType(this.resourceResolverTypes[0]);
      this.delegate = new ExternalResource() {
        @Override
        protected void before() {
          plugins.executeBeforeSetUpCallback(AemContext.this);
          AemContext.this.setUp();
          plugins.executeAfterSetUpCallback(AemContext.this);
        }
        @Override
        protected void after() {
          plugins.executeBeforeTearDownCallback(AemContext.this);
          AemContext.this.tearDown();
          plugins.executeAfterTearDownCallback(AemContext.this);
        }
      };
    }
    else {
      // use ListGenerator rule that iterates over list of resource resolver types
      Callback<ResourceResolverType> parameterizedSetUpCallback = new Callback<ResourceResolverType>() {
        @Override
        public void execute(final ResourceResolverType currrentValue) {
          AemContext.this.setResourceResolverType(currrentValue);
          plugins.executeBeforeSetUpCallback(AemContext.this);
          AemContext.this.setUp();
          plugins.executeAfterSetUpCallback(AemContext.this);
        }
      };
      Callback<ResourceResolverType> parameterizedTearDownCallback = new Callback<ResourceResolverType>() {
        @Override
        public void execute(final ResourceResolverType currrentValue) {
          plugins.executeBeforeTearDownCallback(AemContext.this);
          AemContext.this.tearDown();
          plugins.executeAfterTearDownCallback(AemContext.this);
        }
      };
      this.delegate = new ListGenerator<ResourceResolverType>(Arrays.asList(this.resourceResolverTypes),
          parameterizedSetUpCallback, parameterizedTearDownCallback);
    }
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return this.delegate.apply(base, description);
  }

}
