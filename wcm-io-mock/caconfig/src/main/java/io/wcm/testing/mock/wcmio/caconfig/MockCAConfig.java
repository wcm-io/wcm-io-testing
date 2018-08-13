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

import java.util.Collection;
import java.util.Map;

import org.apache.jackrabbit.util.Text;
import org.apache.sling.caconfig.resource.spi.ContextPathStrategy;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caconfig.extensions.contextpath.impl.AbsoluteParentContextPathStrategy;
import io.wcm.caconfig.extensions.contextpath.impl.RootTemplateContextPathStrategy;
import io.wcm.testing.mock.aem.context.AemContextImpl;

/**
 * Helps setting up a mock environment for wcm.io Configuration.
 */
@ProviderType
public final class MockCAConfig {

  private MockCAConfig() {
    // static methods only
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
  public static void contextPathStrategyAbsoluteParent(final AemContextImpl context,
      final int... levels) {
    context.registerInjectActivateService(new AbsoluteParentContextPathStrategy(),
        "levels", levels);
  }

  /**
   * Register {@link ContextPathStrategy} that detects context paths by matching parent pages against a list of allowed
   * templates for context root.
   * @param context AEM context
   * @param templatePaths List of template paths allowed for context root pages.
   */
  public static void contextPathStrategyRootTemplate(final AemContextImpl context,
      final String... templatePaths) {
    context.registerInjectActivateService(new RootTemplateContextPathStrategy(),
        "templatePaths", templatePaths);
  }

  /**
   * Writes configuration parameters using the primary configured persistence provider.
   * @param context AEM context
   * @param contextPath Configuration id
   * @param configName Config name
   * @param values Configuration values
   * @deprecated Please use
   *             {@link MockContextAwareConfig#writeConfiguration(org.apache.sling.testing.mock.sling.context.SlingContextImpl, String, String, Map)}
   */
  @Deprecated
  public static void writeConfiguration(AemContextImpl context, String contextPath, String configName, Map<String, Object> values) {
    MockContextAwareConfig.writeConfiguration(context, contextPath, configName, values);
  }

  /**
   * Writes configuration parameters using the primary configured persistence provider.
   * @param context AEM context
   * @param contextPath Configuration id
   * @param configName Config name
   * @param values Configuration values
   * @deprecated Please use
   *             {@link MockContextAwareConfig#writeConfiguration(org.apache.sling.testing.mock.sling.context.SlingContextImpl, String, String, Object[])}
   */
  @Deprecated
  public static void writeConfiguration(AemContextImpl context, String contextPath, String configName, Object... values) {
    MockContextAwareConfig.writeConfiguration(context, contextPath, configName, values);
  }

  /**
   * Writes a collection of configuration parameters using the primary configured persistence provider.
   * @param context AEM context
   * @param contextPath Configuration id
   * @param configName Config name
   * @param values Configuration values
   * @deprecated Please use
   *             {@link MockContextAwareConfig#writeConfigurationCollection(org.apache.sling.testing.mock.sling.context.SlingContextImpl, String, String, Collection)}
   */
  @Deprecated
  public static void writeConfigurationCollection(AemContextImpl context, String contextPath, String configName, Collection<Map<String, Object>> values) {
    MockContextAwareConfig.writeConfigurationCollection(context, contextPath, configName, values);
  }

}
