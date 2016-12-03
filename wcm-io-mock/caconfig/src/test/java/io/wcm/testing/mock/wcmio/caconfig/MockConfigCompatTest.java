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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import io.wcm.caconfig.application.spi.ApplicationProvider;
import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;

public class MockConfigCompatTest {

  private static final String APP_ID_1 = "/apps/app1";
  private static final String APP_ID_2 = "/apps/app2";

  public static final Parameter<String> PARAM_1  =
      ParameterBuilder.create("param1", String.class, APP_ID_1).defaultValue("def1").build();
  private static final Parameter<String> PARAM_2 =
      ParameterBuilder.create("param2", String.class, APP_ID_2).defaultValue("def2").build();

  @Rule
  public AemContext context = new AemContextBuilder().plugin(WCMIO_CACONFIG).build();

  @Before
  public void setUp() {
    context.registerService(ConfigurationFinderStrategy.class,
        MockCAConfig.configurationFinderStrategyAbsoluteParent(APP_ID_1, 2));

    context.registerService(ApplicationProvider.class,
        MockCAConfig.applicationProvider(APP_ID_1, "/content"));

    context.registerService(ParameterProvider.class,
        MockCAConfig.parameterProvider(MockConfigCompatTest.class));
    context.registerService(ParameterProvider.class,
        MockCAConfig.parameterProvider(ImmutableSet.<Parameter<?>>builder().add(PARAM_2).build()));

    context.currentPage(context.create().page("/content/region/site/en", "/apps/templates/sample"));

    MockCAConfig.writeConfiguration(context, "/content/region/site",
        ImmutableValueMap.of("param1", "value1"));
  }

  @Test
  public void testConfig() {
    Configuration config = context.request().adaptTo(Configuration.class);
    assertNotNull(config);
    assertEquals("/content/region/site", config.getConfigurationId());
    assertEquals("value1", config.get(PARAM_1));
    assertEquals("def2", config.get(PARAM_2));
  }

}
