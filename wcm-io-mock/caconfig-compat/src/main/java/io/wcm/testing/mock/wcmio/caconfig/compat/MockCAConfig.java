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
package io.wcm.testing.mock.wcmio.caconfig.compat;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caconfig.application.spi.ApplicationProvider;
import io.wcm.config.api.Parameter;
import io.wcm.config.core.impl.ParameterProviderBridge;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.config.spi.helpers.AbstractAbsoluteParentConfigurationFinderStrategy;
import io.wcm.config.spi.helpers.AbstractParameterProvider;
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
   * {@link ApplicationProvider} that supports detecting an application based on one or multiple fixed paths subtrees.
   * @param applicationId Application id
   * @param paths List of paths/subtrees this application belongs to
   * @return Application provider
   */
  public static ApplicationProvider applicationProvider(final String applicationId, final String... paths) {
    final Pattern[] patterns = new Pattern[paths.length];
    for (int i = 0; i < paths.length; i++) {
      patterns[i] = Pattern.compile("^" + Pattern.quote(paths[i]) + "(/.+)?$");
    }
    return applicationProvider(applicationId, patterns);
  }

  /**
   * {@link ApplicationProvider} that supports detecting an application based on one or multiple fixed paths subtrees.
   * @param applicationId Application id
   * @param pathPatterns Path patterns
   * @return Application provider
   */
  public static ApplicationProvider applicationProvider(final String applicationId, final Pattern... pathPatterns) {
    return new ApplicationProvider() {
      @Override
      public String getLabel() {
        return applicationId;
      }
      @Override
      public String getApplicationId() {
        return applicationId;
      }
      @Override
      public boolean matches(Resource resource) {
        for (Pattern pattern : pathPatterns) {
          if (pattern.matcher(resource.getPath()).matches()) {
            return true;
          }
        }
        return false;
      }
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

}
