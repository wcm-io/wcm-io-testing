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

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.testing.mock.osgi.context.ContextCallback;
import org.apache.sling.testing.mock.osgi.context.ContextPlugins;
import org.apache.sling.testing.mock.osgi.context.OsgiContextImpl;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.osgi.annotation.versioning.ProviderType;

import com.google.common.collect.ImmutableList;

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
  public AemContext(final ResourceResolverType... resourceResolverTypes) {
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
  public <T extends OsgiContextImpl> AemContext(final ContextCallback<T> afterSetUpCallback, final ResourceResolverType... resourceResolverTypes) {
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
  public <U extends OsgiContextImpl, V extends OsgiContextImpl> AemContext(final ContextCallback<U> afterSetUpCallback,
      final ContextCallback<V> beforeTearDownCallback,
      final ResourceResolverType... resourceResolverTypes) {
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
  AemContext(final ContextPlugins contextPlugins,
      final Map<String, Object> resourceResolverFactoryActivatorProps,
      final ResourceResolverType... resourceResolverTypes) {

    this.plugins = contextPlugins;

    // set custom ResourceResolverFactoryActivator config, but set AEM default values for all parameter not given here
    Map<String, Object> mergedProps = resourceResolverFactoryActivatorPropsMergeWithAemDefault(resourceResolverFactoryActivatorProps);
    setResourceResolverFactoryActivatorProps(mergedProps);

    if (resourceResolverTypes == null || resourceResolverTypes.length == 0) {
      this.resourceResolverTypes = new ResourceResolverType[] {
          MockSling.DEFAULT_RESOURCERESOLVER_TYPE
      };
    }
    else {
      this.resourceResolverTypes = resourceResolverTypes;
    }

    if (this.resourceResolverTypes.length == 1) {
      // user default rule that directly executes each test method once
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
      this.delegate = new ListGenerator<ResourceResolverType>(ImmutableList.copyOf(this.resourceResolverTypes),
          parameterizedSetUpCallback, parameterizedTearDownCallback);
    }
  }

  /**
   * Merges the given custom Resource Resolver Factory Activator OSGi configuration with the default configuration
   * applied in AEM 6. The custom configuration has higher precedence.
   * @param customProps Custom config
   * @return Merged config
   */
  private Map<String, Object> resourceResolverFactoryActivatorPropsMergeWithAemDefault(Map<String, Object> customProps) {
    Map<String, Object> props = new HashMap<>();

    props.put("resource.resolver.searchpath", new String[] {
        "/apps",
        "/libs",
        "/apps/foundation/components/primary",
        "/libs/foundation/components/primary",
    });
    props.put("resource.resolver.manglenamespaces", true);
    props.put("resource.resolver.allowDirect", true);
    props.put("resource.resolver.virtual", new String[] {
        "/:/"
    });
    props.put("resource.resolver.mapping", new String[] {
        "/-/"
    });
    props.put("resource.resolver.map.location", "/etc/map");
    props.put("resource.resolver.default.vanity.redirect.status", "");
    props.put("resource.resolver.virtual", "302");
    props.put("resource.resolver.enable.vanitypath", true);
    props.put("resource.resolver.vanitypath.maxEntries", -1);
    props.put("resource.resolver.vanitypath.bloomfilter.maxBytes", 1024000);
    props.put("resource.resolver.optimize.alias.resolution", true);
    props.put("resource.resolver.vanitypath.whitelist", new String[] {
        "/apps/",
        "/libs/",
        "/content/"
    });
    props.put("resource.resolver.vanitypath.blacklist", new String[] {
        "/content/usergenerated"
    });
    props.put("resource.resolver.vanity.precedence", false);
    props.put("resource.resolver.providerhandling.paranoid", false);

    if (customProps != null) {
      props.putAll(customProps);
    }

    return props;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return this.delegate.apply(base, description);
  }

}
