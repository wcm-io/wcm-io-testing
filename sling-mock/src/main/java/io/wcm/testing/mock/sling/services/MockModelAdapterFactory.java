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
package io.wcm.testing.mock.sling.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.models.impl.ModelAdapterFactory;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

/**
 * Mock {@link ModelAdapterFactory} implementation.
 */
public class MockModelAdapterFactory extends ModelAdapterFactory {

  private final BundleContext bundleContext;

  /**
   * @param componentContext OSGi component context
   */
  public MockModelAdapterFactory(ComponentContext componentContext) {
    bundleContext = componentContext.getBundleContext();
    // activate service in simulated OSGi environment
    activate(componentContext);
  }

  @Override
  public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {

    // get all injectors for component context
    // this is done because mock OSGi service does not support automatically binding and unbinding services
    try {
      ServiceReference[] injectorReferences = bundleContext.getServiceReferences(Injector.class.getName(), null);
      for (ServiceReference serviceReference : injectorReferences) {
        Injector injector = (Injector)bundleContext.getService(serviceReference);
        bindInjector(injector, getServiceProperties(serviceReference));
      }

      // do adapter work
      AdapterType result = super.getAdapter(adaptable, type);

      // unbind all injectors
      for (ServiceReference serviceReference : injectorReferences) {
        Injector injector = (Injector)bundleContext.getService(serviceReference);
        unbindInjector(injector, getServiceProperties(serviceReference));
      }

      return result;
    }
    catch (InvalidSyntaxException ex) {
      throw new RuntimeException("Unable to get injector references.", ex);
    }

  }

  private Map<String,Object> getServiceProperties(ServiceReference reference) {
    Map<String,Object> props = new HashMap<>();
    String[] propertyKeys = reference.getPropertyKeys();
    for (String key : propertyKeys) {
      props.put(key, reference.getProperty(key));
    }
    return props;
  }

}
