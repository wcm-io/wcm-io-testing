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

import org.apache.sling.testing.mock.osgi.context.AbstractContextPlugin;
import org.apache.sling.testing.mock.osgi.context.ContextPlugin;

import io.wcm.config.core.impl.ApplicationImplementationPicker;
import io.wcm.config.core.impl.ConfigurationAdapterFactory;
import io.wcm.config.core.management.impl.ApplicationFinderImpl;
import io.wcm.config.core.management.impl.ConfigurationFinderImpl;
import io.wcm.config.core.management.impl.ParameterOverrideImpl;
import io.wcm.config.core.management.impl.ParameterPersistenceImpl;
import io.wcm.config.core.management.impl.ParameterResolverImpl;
import io.wcm.config.core.persistence.impl.ToolsConfigPagePersistenceProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Mock context plugins.
 */
public final class ContextPlugins {

  private ContextPlugins() {
    // constants only
  }

  /**
   * Context plugin for wcm.io Configuration.
   */
  public static final ContextPlugin<AemContext> WCMIO_CONFIG = new AbstractContextPlugin<AemContext>() {
    @Override
    public void afterSetUp(AemContext context) throws Exception {
      setUp(context);
    }
  };

  /**
   * Set up all mandatory OSGi services for wcm.io Configuration support.
   * @param context Aem context
   */
  static void setUp(AemContext context) {

    // persistence providers
    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        ImmutableValueMap.of("enabled", true));

    // management services
    context.registerInjectActivateService(new ApplicationFinderImpl());
    context.registerInjectActivateService(new ParameterOverrideImpl());
    context.registerInjectActivateService(new ParameterPersistenceImpl());
    context.registerInjectActivateService(new ParameterResolverImpl());
    context.registerInjectActivateService(new ConfigurationFinderImpl());

    // adapter factory
    context.registerInjectActivateService(new ConfigurationAdapterFactory());

    // models implementation picker
    context.registerInjectActivateService(new ApplicationImplementationPicker());

  }

}
