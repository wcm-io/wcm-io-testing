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
package io.wcm.testing.mock.wcmio.handler;

import org.apache.sling.testing.mock.osgi.context.AbstractContextPlugin;
import org.apache.sling.testing.mock.osgi.context.ContextPlugin;

import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Mock context plugins.
 */
public final class ContextPlugins {

  private ContextPlugins() {
    // constants only
  }

  /**
   * Context plugin for wcm.io Handler
   */
  public static final ContextPlugin<AemContext> WCMIO_HANDLER = new AbstractContextPlugin<AemContext>() {
    @Override
    public void afterSetUp(AemContext context) throws Exception {
      MockHandler.setUp(context);
    }
  };

}
