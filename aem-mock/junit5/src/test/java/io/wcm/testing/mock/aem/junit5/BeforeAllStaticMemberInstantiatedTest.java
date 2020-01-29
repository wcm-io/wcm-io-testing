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
import static org.junit.Assert.assertThat;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test with static initialized context field and {@code BeforeAll}.
 */
@ExtendWith(AemContextExtension.class)
class BeforeAllStaticMemberInstantiatedTest {

  private static final String RESOURCE1_PATH = "/content/test";

  private static AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  @BeforeAll
  static void setUpAll() {
    context.create().resource(RESOURCE1_PATH, "prop1", "value1");
  }

  @Test
  void test1() {
    assertThat(context.resourceResolver().getResource(RESOURCE1_PATH), props("prop1", "value1"));
  }

  @Test
  void test2() {
    assertThat(context.resourceResolver().getResource(RESOURCE1_PATH), props("prop1", "value1"));
  }

}
