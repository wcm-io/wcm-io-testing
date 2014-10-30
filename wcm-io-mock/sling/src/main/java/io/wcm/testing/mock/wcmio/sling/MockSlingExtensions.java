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
package io.wcm.testing.mock.wcmio.sling;

import io.wcm.sling.commons.request.RequestContext;
import io.wcm.sling.models.injectors.impl.AemObjectInjector;
import io.wcm.sling.models.injectors.impl.ModelsImplConfiguration;
import io.wcm.sling.models.injectors.impl.SlingObjectOverlayInjector;
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import com.google.common.collect.ImmutableMap;

/**
 * Helps setting up a mock environment for wcm.io Configuration.
 */
@ProviderType
public final class MockSlingExtensions {

  private MockSlingExtensions() {
    // static methods only
  }

  /**
   * Set up all mandatory OSGi services for wcm.io Configuration support.
   * @param context Aem context
   */
  public static void setUp(AemContext context) {

    // register request context
    context.registerService(RequestContext.class, new MockRequestContext());

    // register sling models extensions
    context.registerInjectActivateService(new ModelsImplConfiguration(),
        ImmutableMap.<String, Object>of("requestThreadLocal", true));

    context.registerInjectActivateService(new AemObjectInjector());
    context.registerInjectActivateService(new SlingObjectOverlayInjector());
  }

  /**
   * Simulate setting current request in request context.
   * @param context AEM Context
   * @param request Request
   */
  public static void setRequestContext(AemContext context, SlingHttpServletRequest request) {
    MockRequestContext requestContext = (MockRequestContext)context.getService(RequestContext.class);
    requestContext.setRequest(request);
  }

}
