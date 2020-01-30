/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.testing.mock.wcmio.wcm;

import org.apache.sling.testing.mock.osgi.context.AbstractContextPlugin;
import org.apache.sling.testing.mock.osgi.context.ContextPlugin;
import org.jetbrains.annotations.NotNull;

import io.wcm.testing.mock.aem.context.AemContextImpl;
import io.wcm.wcm.commons.bundleinfo.impl.BundleInfoServiceImpl;
import io.wcm.wcm.commons.caservice.impl.WcmPathPreprocessor;
import io.wcm.wcm.commons.component.impl.ComponentPropertyResolverFactoryImpl;

/**
 * Mock context plugins.
 */
public final class ContextPlugins {

  private ContextPlugins() {
    // constants only
  }

  /**
   * Context plugin for wcm.io WCM Commons.
   */
  public static final @NotNull ContextPlugin<AemContextImpl> WCMIO_WCM = new AbstractContextPlugin<AemContextImpl>() {
    @Override
    public void afterSetUp(@NotNull AemContextImpl context) throws Exception {
      setUp(context);
    }
  };

  /**
   * Set up request context and Sling Models Extensions.
   * @param context AEM context
   */
  static void setUp(AemContextImpl context) {

    // WCM Commons
    context.registerInjectActivateService(new BundleInfoServiceImpl());
    context.registerInjectActivateService(new WcmPathPreprocessor());
    context.registerInjectActivateService(new ComponentPropertyResolverFactoryImpl());

  }

}
