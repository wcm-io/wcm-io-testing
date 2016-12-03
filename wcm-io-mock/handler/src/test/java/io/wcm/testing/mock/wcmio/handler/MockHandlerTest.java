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

import static io.wcm.testing.mock.wcmio.config.ContextPlugins.WCMIO_CONFIG;
import static io.wcm.testing.mock.wcmio.handler.ContextPlugins.WCMIO_HANDLER;
import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.richtext.RichTextHandler;
import io.wcm.handler.url.UrlHandler;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;

public class MockHandlerTest {

  @Rule
  public AemContext context = new AemContextBuilder().plugin(WCMIO_SLING, WCMIO_CONFIG, WCMIO_HANDLER).build();

  @Before
  public void setUp() {
    context.currentPage(context.create().page("/content/region/site/en", "/apps/templates/sample"));
  }

  @Test
  public void testHandler() {
    assertNotNull(context.request().adaptTo(UrlHandler.class));
    assertNotNull(context.request().adaptTo(MediaHandler.class));
    assertNotNull(context.request().adaptTo(LinkHandler.class));
    assertNotNull(context.request().adaptTo(RichTextHandler.class));
  }

}
