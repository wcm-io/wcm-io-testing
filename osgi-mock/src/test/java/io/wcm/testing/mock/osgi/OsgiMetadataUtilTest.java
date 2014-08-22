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
package io.wcm.testing.mock.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.w3c.dom.Document;

@SuppressWarnings("javadoc")
public class OsgiMetadataUtilTest {

  @Test
  public void testMetadata() {
    Document doc = OsgiMetadataUtil.geDocument(ServiceWithMetadata.class);

    Set<String> serviceInterfaces = OsgiMetadataUtil.getServiceInterfaces(doc);
    assertEquals(3, serviceInterfaces.size());
    assertTrue(serviceInterfaces.contains("org.apache.sling.models.spi.Injector"));
    assertTrue(serviceInterfaces.contains("org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessorFactory"));
    assertTrue(serviceInterfaces.contains("java.lang.Comparable"));

    Map<String, Object> props = OsgiMetadataUtil.getProperties(doc);
    assertEquals(3, props.size());
    assertEquals(5000, props.get("service.ranking"));
    assertEquals("The Apache Software Foundation", props.get("service.vendor"));
    assertEquals("org.apache.sling.models.impl.injectors.OSGiServiceInjector", props.get("service.pid"));
  }

  @Test
  public void testNoMetadata() {
    Document doc = OsgiMetadataUtil.geDocument(ServiceWithoutMetadata.class);

    Set<String> serviceInterfaces = OsgiMetadataUtil.getServiceInterfaces(doc);
    assertEquals(0, serviceInterfaces.size());

    Map<String, Object> props = OsgiMetadataUtil.getProperties(doc);
    assertEquals(0, props.size());
  }

  static class ServiceWithMetadata {
    // empty class
  }

  static class ServiceWithoutMetadata {
    // empty class
  }

}
