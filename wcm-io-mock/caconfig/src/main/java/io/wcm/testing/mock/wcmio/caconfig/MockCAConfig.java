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
package io.wcm.testing.mock.wcmio.caconfig;

import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caconfig.application.spi.AbstractPathApplicationProvider;
import io.wcm.caconfig.application.spi.ApplicationProvider;
import io.wcm.config.core.impl.ParameterProviderBridge;
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

}
