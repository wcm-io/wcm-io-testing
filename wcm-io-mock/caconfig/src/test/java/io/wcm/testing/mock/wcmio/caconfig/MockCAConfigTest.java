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

import static io.wcm.testing.mock.wcmio.caconfig.ContextPlugins.WCMIO_CACONFIG;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.wcmio.caconfig.example.SimpleConfig;

public class MockCAConfigTest {

  @Rule
  public AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .plugin(WCMIO_CACONFIG)
      .build();

  @Before
  public void setUp() {
    MockContextAwareConfig.registerAnnotationClasses(context, SimpleConfig.class);

    MockCAConfig.contextPathStrategyAbsoluteParent(context, 2);

    context.currentPage(context.create().page("/content/region/site/en", "/apps/templates/sample"));

    MockCAConfig.writeConfiguration(context, "/content/region/site",
        SimpleConfig.class.getName(), ImmutableValueMap.of("param1", "value1"));
  }

  @Test
  public void testConfig() {
    Resource resource = context.request().getResource();
    SimpleConfig config = resource.adaptTo(ConfigurationBuilder.class).as(SimpleConfig.class);
    assertNotNull(config);
    assertEquals("value1", config.param1());
    assertEquals("def2", config.param2());
  }

}
