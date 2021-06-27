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
package io.wcm.testing.mock.aem.junit5;

import static org.apache.sling.hamcrest.ResourceMatchers.props;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test with specialized context parameter in {@code BeforeAll}-method.
 */
@ExtendWith(AemContextExtension.class)
class BeforeAllSpecializedContextParamTest {

  private static final String RESOURCE1_PATH = "/content/test";

  @BeforeAll
  static void setUpAll(JcrMockAemContext context) {
    context.create().resource(RESOURCE1_PATH, "prop1", "value1");
  }

  @Test
  void test1(JcrMockAemContext context) {
    assertThat(context.resourceResolver().getResource(RESOURCE1_PATH), props("prop1", "value1"));
  }

  @Test
  void test2(JcrMockAemContext context) {
    assertThat(context.resourceResolver().getResource(RESOURCE1_PATH), props("prop1", "value1"));
  }

}
