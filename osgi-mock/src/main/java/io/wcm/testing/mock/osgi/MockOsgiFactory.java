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
package io.wcm.testing.mock.osgi;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

/**
 * Factory for mock OSGi objects.
 */
public final class MockOsgiFactory {

  private MockOsgiFactory() {
    // static methods only
  }

  /**
   * @return Mocked {@link Bundle} instance
   */
  public static Bundle newBundle() {
    return new MockBundle();
  }

  /**
   * @return Mocked {@link BundleContext} instance
   */
  public static BundleContext newBundleContext() {
    return newBundle().getBundleContext();
  }

  /**
   * Simulates a bundle event on the given bundle context (that is forwarded to registered bundle listeners).
   * @param bundleContext Bundle context
   * @param bundleEvent Bundle event
   */
  public static void sendBundleEvent(BundleContext bundleContext, BundleEvent bundleEvent) {
    ((MockBundleContext)bundleContext).sendBundleEvent(bundleEvent);
  }

  /**
   * @return Mocked {@link ComponentContext} instance
   */
  public static ComponentContext newComponentContext() {
    return new MockComponentContext((MockBundleContext)newBundleContext());
  }

  /**
   * @param properties Properties
   * @return Mocked {@link ComponentContext} instance
   */
  public static ComponentContext newComponentContext(final Dictionary<String, Object> properties) {
    return new MockComponentContext((MockBundleContext)newBundleContext(), properties);
  }

  /**
   * @param loggerContext Context class for logging
   * @return Mocked {@link LogService} instance
   */
  public static LogService newLogService(final Class<?> loggerContext) {
    return new MockLogService(loggerContext);
  }

}
