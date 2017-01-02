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

import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.resource.spi.ContextPathStrategy;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caconfig.application.impl.PathApplicationProvider;
import io.wcm.caconfig.application.spi.ApplicationProvider;
import io.wcm.caconfig.extensions.contextpath.impl.AbsoluteParentContextPathStrategy;
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
   * Register {@link ApplicationProvider} that supports detecting an application based on one or multiple fixed paths
   * subtrees.
   * @param context AEM context
   * @param applicationId Application id
   * @param pathPatterns Path patterns
   */
  public static void applicationProvider(final AemContext context,
      final String applicationId, final String... pathPatterns) {
    context.registerInjectActivateService(new PathApplicationProvider(),
        "applicationId", applicationId,
        "pathPatterns", pathPatterns);
  }

  /**
   * Register {@link ContextPathStrategy} that supports one or multiple fixed levels in content hierarchy where
   * configurations are supported.
   * @param context AEM context
   * @param levels List of absolute levels where configuration is supported.
   *          Levels are used in the same way as {@link Text#getAbsoluteParent(String, int)}.
   *          Example:<br>
   *          <code>Text.getAbsoluteParent("/foo/bar/test", 1) == "/foo/bar"</code>
   */
  public static void contextPathStrategyAbsoluteParent(final AemContext context,
      final int... levels) {
    contextPathStrategyAbsoluteParent(context, null, levels);
  }

  /**
   * Register {@link ContextPathStrategy} that supports one or multiple fixed levels in content hierarchy where
   * configurations are supported.
   * @param context AEM context
   * @param applicationId Application ID
   * @param levels List of absolute levels where configuration is supported.
   *          Levels are used in the same way as {@link Text#getAbsoluteParent(String, int)}.
   *          Example:<br>
   *          <code>Text.getAbsoluteParent("/foo/bar/test", 1) == "/foo/bar"</code>
   */
  public static void contextPathStrategyAbsoluteParent(final AemContext context,
      final String applicationId, final int... levels) {
    context.registerInjectActivateService(new AbsoluteParentContextPathStrategy(),
        "applicationId", applicationId,
        "levels", levels);
  }

  /**
   * Writes configuration parameters using the primary configured persistence provider.
   * @param context AEM context
   * @param contextPath Configuration id
   * @param configName Config name
   * @param values Configuration values
   */
  public static void writeConfiguration(AemContext context, String contextPath, String configName, Map<String, Object> values) {
    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    Resource contextResource = context.resourceResolver().getResource(contextPath);
    configManager.persistConfiguration(contextResource, configName, new ConfigurationPersistData(values));
  }

}
