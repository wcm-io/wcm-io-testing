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

import org.apache.sling.testing.mock.osgi.context.AbstractContextPlugin;
import org.apache.sling.testing.mock.osgi.context.ContextPlugin;
import org.jetbrains.annotations.NotNull;

import io.wcm.testing.mock.aem.context.AemContextImpl;

/**
 * Mock context plugins.
 * TODO: remove this plugin as it has not effect?
 */
public final class ContextPlugins {

  private ContextPlugins() {
    // constants only
  }

  /**
   * Context plugin for wcm.io Context-Aware Configuration.
   */
  public static final @NotNull ContextPlugin<AemContextImpl> WCMIO_CACONFIG = new AbstractContextPlugin<AemContextImpl>() {
    @Override
    public void afterSetUp(AemContextImpl context) throws Exception {
      setUp(context);
    }
  };

  /**
   * Set up all mandatory OSGi services for wcm.io Context-Aware Configuration support.
   * @param context AEM context
   */
  private static void setUp(AemContextImpl context) {

    // nothing to do - yet

  }

}
