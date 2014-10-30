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
package io.wcm.testing.mock.wcmio.sling.models;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import io.wcm.sling.commons.request.RequestContext;
import io.wcm.sling.models.injectors.impl.AemObjectInjector;
import io.wcm.sling.models.injectors.impl.SlingObjectOverlayInjector;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

import org.apache.sling.models.spi.Injector;
import org.junit.Rule;
import org.junit.Test;

public class MockSlingExtensionsTest {

  @Rule
  public AemContext context = new AemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      MockSlingExtensions.setUp(callbackContext);
    }
  });

  @Test
  public void testInjectors() {
    boolean aemObjectInjectorFound = false;
    boolean slingObjectOverlayInjectorFound = false;

    Injector[] injectors = context.getServices(Injector.class, null);
    for (Injector injector : injectors) {
      if (injector instanceof AemObjectInjector) {
        aemObjectInjectorFound = true;
      }
      if (injector instanceof SlingObjectOverlayInjector) {
        slingObjectOverlayInjectorFound = true;
      }
    }

    assertTrue(aemObjectInjectorFound);
    assertTrue(slingObjectOverlayInjectorFound);
  }

  @Test
  public void testSetRequestContext() {
    assertNull(context.getService(RequestContext.class).getThreadRequest());

    MockSlingExtensions.setRequestContext(context, context.request());

    assertSame(context.request(), context.getService(RequestContext.class).getThreadRequest());
  }

}
