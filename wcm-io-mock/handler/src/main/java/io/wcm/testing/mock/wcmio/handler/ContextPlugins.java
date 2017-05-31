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

import io.wcm.handler.link.impl.DefaultLinkHandlerConfig;
import io.wcm.handler.link.impl.LinkHandlerConfigAdapterFactory;
import io.wcm.handler.media.format.impl.MediaFormatProviderManagerImpl;
import io.wcm.handler.media.impl.DefaultMediaHandlerConfig;
import io.wcm.handler.media.impl.MediaHandlerConfigAdapterFactory;
import io.wcm.handler.url.impl.DefaultUrlHandlerConfig;
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
      setUp(context);
    }
  };

  /**
   * Set up all mandatory OSGi services for wcm.io Handler support.
   * @param context Aem context
   */
  static void setUp(AemContext context) {

    // url handler
    registerOptional(context, "io.wcm.handler.url.impl.SiteRootDetectorImpl"); // since URL Handler 1.1.0
    registerOptional(context, "io.wcm.handler.url.impl.UrlHandlerConfigAdapterFactory"); // URL Handler 1.0.0
    registerOptional(context, "io.wcm.handler.url.impl.UrlHandlerAdapterFactory"); // since URL Handler 1.1.0
    context.registerInjectActivateService(new DefaultUrlHandlerConfig());

    // media handler
    context.registerInjectActivateService(new MediaHandlerConfigAdapterFactory());
    context.registerInjectActivateService(new DefaultMediaHandlerConfig());
    context.registerInjectActivateService(new MediaFormatProviderManagerImpl());

    // link handler
    context.registerInjectActivateService(new LinkHandlerConfigAdapterFactory());
    context.registerInjectActivateService(new DefaultLinkHandlerConfig());

    // rich text handler
    registerOptional(context, "io.wcm.handler.richtext.impl.DefaultRichTextHandlerConfig"); // since Rich Text Handler 1.1.0

  }

  /**
   * Registers an OSGi service if the class exists. Ignores the call if not.
   * @param className Class name
   */
  private static void registerOptional(AemContext context, String className) {
    try {
      Class clazz = Class.forName(className);
      context.registerInjectActivateService(clazz.newInstance());
    }
    catch (ClassNotFoundException ex) {
      // ignore
    }
    catch (InstantiationException | IllegalAccessException ex) {
      throw new RuntimeException("Unable to instantiate " + className, ex);
    }
  }

}
