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
package io.wcm.testing.mock.wcmio.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.spi.ApplicationProvider;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class MockConfigTest {

  private static final String APP_ID_1 = "/apps/app1";
  private static final String APP_ID_2 = "/apps/app2";

  public static final Parameter<String> PARAM_1  =
      ParameterBuilder.create("param1", String.class, APP_ID_1).defaultValue("def1").build();
  private static final Parameter<String> PARAM_2 =
      ParameterBuilder.create("param2", String.class, APP_ID_2).defaultValue("def2").build();

  @Rule
  public AemContext context = new AemContext(new AemContextCallback() {
    @Override
    public void execute(AemContext callbackContext) {
      callbackContext.registerService(ConfigurationFinderStrategy.class,
          MockConfig.configurationFinderStrategyAbsoluteParent(APP_ID_1, 2));

      callbackContext.registerService(ApplicationProvider.class,
          MockConfig.applicationProvider(APP_ID_1, "/content"));

      callbackContext.registerService(ParameterProvider.class,
          MockConfig.parameterProvider(MockConfigTest.class));
      callbackContext.registerService(ParameterProvider.class,
          MockConfig.parameterProvider(ImmutableSet.<Parameter<?>>builder().add(PARAM_2).build()));

      MockConfig.setUp(callbackContext);

      callbackContext.currentPage(callbackContext.create().page("/content/region/site/en", "/apps/templates/sample"));

      MockConfig.writeConfiguration(callbackContext, "/content/region/site", ImmutableMap.<String, Object>builder()
          .put("param1", "value1")
          .build());
    }
  });

  @Test
  public void testConfig() {
    Configuration config = context.request().adaptTo(Configuration.class);
    assertNotNull(config);
    assertEquals("/content/region/site", config.getConfigurationId());
    assertEquals("value1", config.get(PARAM_1));
    assertEquals("def2", config.get(PARAM_2));
  }

}
