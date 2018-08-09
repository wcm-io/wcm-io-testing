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
package io.wcm.testing.mock.aem.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import io.wcm.testing.mock.aem.modelsautoreg.ScriptBindingsModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit.AemContext;

public class AemContextImplTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private String contentRoot;
  private String appsRoot;

  @Before
  public void setUp() throws Exception {
    contentRoot = context.uniqueRoot().content() + "/sample";
    appsRoot = context.uniqueRoot().apps() + "/sample";

    ContentLoader contentLoader = this.context.load();
    contentLoader.json("/json-import-samples/application.json", appsRoot);
    contentLoader.json("/json-import-samples/content.json", contentRoot);
  }

  @After
  public void tearDown() throws Exception {
    this.context.tearDown();
  }

  @Test
  public void testContextObjects() {
    assertNotNull(context.pageManager());
  }

  @Test
  public void testSetCurrentPage() {
    context.currentPage(contentRoot + "/toolbar/profiles");
    assertEquals(contentRoot + "/toolbar/profiles", context.currentPage().getPath());
    assertEquals(contentRoot + "/toolbar/profiles/jcr:content", context.currentResource().getPath());

    context.currentPage(context.pageManager().getPage(contentRoot + "/toolbar"));
    assertEquals(contentRoot + "/toolbar", context.currentPage().getPath());
    assertEquals(contentRoot + "/toolbar/jcr:content", context.currentResource().getPath());

    context.currentPage((Page)null);
    assertNull(context.currentPage());

    context.currentPage((String)null);
    assertNull(context.currentPage());
  }

  @Test
  public void testSetCurrentPageWithResourceFromOtherPage() {
    Resource otherResource = context.resourceResolver().getResource(contentRoot + "/jcr:content/par");

    context.currentPage(contentRoot + "/toolbar/profiles");
    context.currentResource(otherResource);

    assertEquals(contentRoot + "/toolbar/profiles", context.currentPage().getPath());
    assertEquals(otherResource.getPath(), context.currentResource().getPath());

    context.currentPage(context.pageManager().getPage(contentRoot + "/toolbar"));
    assertEquals(contentRoot + "/toolbar", context.currentPage().getPath());
    assertEquals(contentRoot + "/toolbar/jcr:content", context.currentResource().getPath());

    context.currentPage((Page)null);
    assertNull(context.currentPage());

    context.currentPage((String)null);
    assertNull(context.currentPage());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetCurrentPageNonExisting() {
    context.currentPage("/non/existing");
  }

  @Test
  public void testServiceLogin() throws Exception {
    ResourceResolverFactory resourceResolverFactory = context.getService(ResourceResolverFactory.class);
    ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(null);
    assertNotNull(resolver);
    resolver.close();
  }

  @Test
  public void testResourceResolverFactoryActivatorConfig() {
    String mappedPath = context.resourceResolver().map(contentRoot + "/toolbar/profiles");
    // unlike sling AEM does not define a default mapping from /content to /
    assertEquals(contentRoot + "/toolbar/profiles", mappedPath);
  }

  @Test
  public void testInjectCurrentPageInComponent(){
    SlingHttpServletRequest request = context.request();
    context.currentPage(contentRoot + "/toolbar");

    ScriptBindingsModel component = request.adaptTo(ScriptBindingsModel.class);

    assertNotNull(component.getCurrentPage());
    assertEquals(component.getCurrentPage().getPath(), contentRoot + "/toolbar");
  }

}
