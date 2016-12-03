/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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
package io.wcm.testing.mock.wcmio.config;

import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.caconfig.impl.ConfigurationInheritanceStrategyMultiplexer;
import org.apache.sling.caconfig.impl.ConfigurationResolverImpl;
import org.apache.sling.caconfig.impl.def.DefaultConfigurationInheritanceStrategy;
import org.apache.sling.caconfig.impl.def.DefaultConfigurationPersistenceStrategy;
import org.apache.sling.caconfig.impl.metadata.ConfigurationMetadataProviderMultiplexer;
import org.apache.sling.caconfig.impl.override.ConfigurationOverrideManager;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.management.impl.ConfigurationManagerImpl;
import org.apache.sling.caconfig.management.impl.ConfigurationPersistenceStrategyMultiplexer;
import org.apache.sling.caconfig.management.impl.ContextPathStrategyMultiplexerImpl;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.caconfig.resource.impl.ConfigurationResourceResolverImpl;
import org.apache.sling.caconfig.resource.impl.ConfigurationResourceResolvingStrategyMultiplexer;
import org.apache.sling.caconfig.resource.impl.def.DefaultConfigurationResourceResolvingStrategy;
import org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caconfig.application.impl.ApplicationAdapterFactory;
import io.wcm.caconfig.application.impl.ApplicationFinderImpl;
import io.wcm.caconfig.application.impl.ApplicationImplementationPicker;
import io.wcm.caconfig.application.spi.AbstractPathApplicationProvider;
import io.wcm.caconfig.application.spi.ApplicationProvider;
import io.wcm.config.api.Parameter;
import io.wcm.config.core.impl.ConfigurationAdapterFactory;
import io.wcm.config.core.impl.ConfigurationFinderStrategyBridge;
import io.wcm.config.core.impl.ParameterOverrideProviderBridge;
import io.wcm.config.core.impl.ParameterProviderBridge;
import io.wcm.config.core.persistence.impl.ToolsConfigPagePersistenceProvider;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.config.spi.helpers.AbstractAbsoluteParentConfigurationFinderStrategy;
import io.wcm.config.spi.helpers.AbstractParameterProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Helps setting up a mock environment for wcm.io Configuration.
 */
@ProviderType
public final class MockCAConfig {

  private MockCAConfig() {
    // static methods only
  }

  /**
   * Set up all mandatory OSGi services for wcm.io Configuration support.
   * @param context Aem context
   */
  public static void setUpCompat(AemContext context) {

    // sling caconfig
    registerConfigurationResolver(context);
    context.registerInjectActivateService(new ConfigurationMetadataProviderMultiplexer());
    context.registerInjectActivateService(new ConfigurationManagerImpl());

    // application detection
    context.registerInjectActivateService(new ApplicationFinderImpl());
    context.registerInjectActivateService(new ApplicationImplementationPicker());
    context.registerInjectActivateService(new io.wcm.config.core.impl.application.ApplicationImplementationPicker());
    context.registerInjectActivateService(new ApplicationAdapterFactory());

    // persistence providers
    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        ImmutableValueMap.of("enabled", true));

    // bridge services
    context.registerInjectActivateService(new ConfigurationFinderStrategyBridge());
    context.registerInjectActivateService(new ParameterOverrideProviderBridge());
    context.registerInjectActivateService(new ParameterProviderBridge());

    // adapter factory
    context.registerInjectActivateService(new ConfigurationAdapterFactory());

  }

  /**
   * {@link ConfigurationFinderStrategy} that supports one or multiple fixed levels in content hierarchy where
   * configurations are supported.
   * @param applicationId Application ID
   * @param levels List of absolute levels where configuration is supported.
   *          Levels are used in the same way as {@link Text#getAbsoluteParent(String, int)}.
   *          Example:<br>
   *          <code>Text.getAbsoluteParent("/foo/bar/test", 1) == "/foo/bar"</code>
   * @return Configuration finder strategy
   */
  public static ConfigurationFinderStrategy configurationFinderStrategyAbsoluteParent(
      final String applicationId, final int... levels) {
    return new AbstractAbsoluteParentConfigurationFinderStrategy(applicationId, levels) {
      // nothing to override
    };
  }

  /**
   * {@link ParameterProvider} providing list of parameters from given parameter set.
   * @param parameters Parameter set
   * @return Parameter provider
   */
  public static ParameterProvider parameterProvider(final Set<Parameter<?>> parameters) {
    return new AbstractParameterProvider(parameters) {
      // nothing to override
    };
  }

  /**
   * {@link ParameterProvider} providing list of parameters from given from reading all public static fields from a
   * given class definition.
   * @param type Class definition
   * @return Parameter provider
   */
  public static ParameterProvider parameterProvider(final Class<?> type) {
    return new AbstractParameterProvider(type) {
      // nothing to override
    };
  }

  /**
   * {@link ApplicationProvider} that supports detecting an application based on one or multiple fixed paths subtrees.
   * @param applicationId Application id
   * @param paths List of paths/subtrees this application belongs to
   * @return Application provider
   */
  public static ApplicationProvider applicationProvider(final String applicationId, final String... paths) {
    return new AbstractPathApplicationProvider(applicationId, applicationId, paths) {
      // nothing to override
    };
  }

  /**
   * Writes configuration parameters using the primary configured persistence provider.
   * @param context AEM context
   * @param contextPath Configuration id
   * @param values Configuration values
   */
  public static void writeConfiguration(AemContext context, String contextPath, Map<String, Object> values) {
    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    Resource contextResource = context.resourceResolver().getResource(contextPath);
    configManager.persistConfiguration(contextResource, ParameterProviderBridge.DEFAULT_CONFIG_NAME,
        new ConfigurationPersistData(values));
  }

  /**
   * Register all services for {@link ConfigurationResourceResolver}.
   * @param context Sling context
   */
  private static ConfigurationResourceResolver registerConfigurationResourceResolver(AemContext context) {
    context.registerInjectActivateService(new DefaultContextPathStrategy());
    context.registerInjectActivateService(new ContextPathStrategyMultiplexerImpl());
    context.registerInjectActivateService(new DefaultConfigurationResourceResolvingStrategy());
    context.registerInjectActivateService(new ConfigurationResourceResolvingStrategyMultiplexer());
    return context.registerInjectActivateService(new ConfigurationResourceResolverImpl());
  }

  /**
   * Register all services for {@link ConfigurationResolver}.
   * @param context Sling context
   */
  private static ConfigurationResolver registerConfigurationResolver(AemContext context) {
    registerConfigurationResourceResolver(context);
    context.registerInjectActivateService(new DefaultConfigurationPersistenceStrategy());
    context.registerInjectActivateService(new ConfigurationPersistenceStrategyMultiplexer());
    context.registerInjectActivateService(new DefaultConfigurationInheritanceStrategy());
    context.registerInjectActivateService(new ConfigurationInheritanceStrategyMultiplexer());
    context.registerInjectActivateService(new ConfigurationOverrideManager());
    return context.registerInjectActivateService(new ConfigurationResolverImpl());
  }

}
