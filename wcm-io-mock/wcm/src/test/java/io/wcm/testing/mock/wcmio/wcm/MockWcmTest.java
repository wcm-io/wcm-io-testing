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

import static io.wcm.testing.mock.wcmio.wcm.ContextPlugins.WCMIO_WCM;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.wcm.commons.bundleinfo.BundleInfoService;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

public class MockWcmTest {

  @Rule
  public AemContext context = new AemContextBuilder().plugin(WCMIO_WCM).build();

  @Test
  public void testServices() {
    assertNotNull(context.getService(ComponentPropertyResolverFactory.class));
    assertNotNull(context.getService(BundleInfoService.class));
  }

}
