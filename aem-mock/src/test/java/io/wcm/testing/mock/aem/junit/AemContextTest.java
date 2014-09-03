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
package io.wcm.testing.mock.aem.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import io.wcm.testing.mock.sling.MockSling;
import io.wcm.testing.mock.sling.ResourceResolverType;
import io.wcm.testing.mock.sling.contentimport.JsonImporter;
import io.wcm.testing.mock.sling.services.MockMimeTypeService;
import io.wcm.testing.mock.sling.servlet.MockSlingHttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.annotations.Model;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

public class AemContextTest {

  // Run all unit tests for each resource resolver types listed here
  @Rule
  public AemContext context = new AemContext(
      ResourceResolverType.JCR_MOCK,
      ResourceResolverType.RESOURCERESOLVER_MOCK
      );

  @Before
  public void setUp() throws PersistenceException, IOException {
    JsonImporter jsonImporter = this.context.jsonImporter();
    jsonImporter.importTo("/json-import-samples/application.json", "/apps/sample");
    jsonImporter.importTo("/json-import-samples/content.json", "/content/sample/en");
  }

  @Test
  public void testContextObjects() {
    assertNotNull(context.componentContext());
    assertNotNull(context.bundleContext());
    assertNotNull(context.resourceResolver());
    assertNotNull(context.request());
    assertNotNull(context.response());
    assertNotNull(context.slingScriptHelper());
    assertNotNull(context.pageManager());
  }

  @Test
  public void testSlingBindings() {
    SlingBindings bindings = (SlingBindings)context.request().getAttribute(SlingBindings.class.getName());
    assertNotNull(bindings);
    assertSame(context.request(), bindings.get(SlingBindings.REQUEST));
    assertSame(context.response(), bindings.get(SlingBindings.RESPONSE));
    assertSame(context.slingScriptHelper(), bindings.get(SlingBindings.SLING));
  }

  @Test
  public void testRegisterService() {
    Set<String> myService = new HashSet<String>();
    context.registerService(Set.class, myService);

    Set<?> serviceResult = context.slingScriptHelper().getService(Set.class);
    assertSame(myService, serviceResult);
  }

  @Test
  public void testRegisterServiceWithProperties() {
    Map<String, Object> props = new HashMap<>();
    props.put("prop1", "value1");

    Set<String> myService = new HashSet<String>();
    context.registerService(Set.class, myService, props);

    ServiceReference serviceReference = context.bundleContext().getServiceReference(Set.class.getName());
    Object serviceResult = context.bundleContext().getService(serviceReference);
    assertSame(myService, serviceResult);
    assertEquals("value1", serviceReference.getProperty("prop1"));
  }

  @Test
  public void testSetCurrentResource() {
    context.currentResource("/content/sample/en/jcr:content/par/colctrl");
    assertEquals("/content/sample/en/jcr:content/par/colctrl", context.request().getResource().getPath());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetCurrentResourceNonExisting() {
    context.currentResource("/non/existing");
  }

  @Test
  public void testSetCurrentPage() {
    context.currentPage("/content/sample/en/toolbar/profiles");
    assertEquals("/content/sample/en/toolbar/profiles/jcr:content", context.request().getResource().getPath());
  }

  @Test
  public void testSlingModelsRequestAttribute() {
    MockSlingHttpServletRequest request = (MockSlingHttpServletRequest)context.request();
    request.setAttribute("prop1", "myValue");
    RequestAttributeModel model = context.request().adaptTo(RequestAttributeModel.class);
    assertEquals("myValue", model.getProp1());
  }

  @Test
  public void testSlingModelsOsgiService() {
    context.registerService(new MockMimeTypeService());

    ResourceResolver resolver = MockSling.newResourceResolver();
    OsgiServiceModel model = resolver.adaptTo(OsgiServiceModel.class);
    assertNotNull(model.getMimeTypeService());
    assertEquals("text/html", model.getMimeTypeService().getMimeType("html"));
  }

  @Test
  public void testSlingModelsInvalidAdapt() {
    OsgiServiceModel model = context.request().adaptTo(OsgiServiceModel.class);
    assertNull(model);
  }

  @Test
  public void testAdaptToInterface() {
    context.addModelsForPackage("io.wcm.testing.mock.aem.junit");

    MockSlingHttpServletRequest request = new MockSlingHttpServletRequest();
    request.setAttribute("prop1", "myValue");
    ServiceInterface model = request.adaptTo(ServiceInterface.class);
    assertNotNull(model);
    assertEquals("myValue", model.getPropValue());
  }

  @Test
  public void testRegisterInjectActivate() {
    context.registerInjectActivateService(new Object());
  }


  @Model(adaptables = SlingHttpServletRequest.class)
  public interface RequestAttributeModel {
    @Inject
    String getProp1();
  }

  @Model(adaptables = ResourceResolver.class)
  public interface OsgiServiceModel {
    @Inject
    MimeTypeService getMimeTypeService();
  }

  public interface ServiceInterface {
    String getPropValue();
  }

  @Model(adaptables = SlingHttpServletRequest.class, adapters = ServiceInterface.class)
  public static class ServiceInterfaceImpl implements ServiceInterface {

    @Inject
    private String prop1;

    @Override
    public String getPropValue() {
      return this.prop1;
    }
  }

}
