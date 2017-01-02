/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import org.osgi.annotation.versioning.ProviderType;

import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Helps setting up a mock environment for wcm.io Handler.
 */
@ProviderType
public final class MockHandler {

  private MockHandler() {
    // static methods only
  }

  /**
   * Set up all mandatory OSGi services for wcm.io Handler support.
   * @param context Aem context
   * @deprecated Please use {@link ContextPlugins#WCMIO_HANDLER} plugin.
   */
  @Deprecated
  public static void setUp(AemContext context) {
    ContextPlugins.setUp(context);
  }

}
