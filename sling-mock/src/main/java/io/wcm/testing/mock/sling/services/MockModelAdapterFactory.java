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

import io.wcm.testing.mock.osgi.MockOsgiFactory;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.models.impl.ModelAdapterFactory;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessorFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

/**
 * Mock {@link ModelAdapterFactory} implementation.
 */
@Component
@Service(AdapterFactory.class)
public class MockModelAdapterFactory extends ModelAdapterFactory {

  private final BundleContext bundleContext;

  /**
   * @param componentContext OSGi component context
   */
  public MockModelAdapterFactory(ComponentContext componentContext) {
    bundleContext = componentContext.getBundleContext();

    // register service listener to collect injectors
    // this is done because mock OSGi service does not support automatically binding and unbinding services
    bundleContext.addServiceListener(new InjectorServiceListener());

    // activate service in simulated OSGi environment
    activate(componentContext);
  }

  /**
   * Constructor with default component context
   */
  public MockModelAdapterFactory() {
    this(MockOsgiFactory.newComponentContext());
  }

  private class InjectorServiceListener implements ServiceListener {

    @Override
    public void serviceChanged(ServiceEvent event) {
      Object service = bundleContext.getService(event.getServiceReference());
      if (service instanceof Injector) {
        if (event.getType() == ServiceEvent.REGISTERED) {
          bindInjector((Injector)service,
              getServiceProperties(event.getServiceReference()));
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING) {
          unbindInjector((Injector)service,
              getServiceProperties(event.getServiceReference()));
        }
      }
      if (service instanceof InjectAnnotationProcessorFactory) {
        if (event.getType() == ServiceEvent.REGISTERED) {
          bindInjectAnnotationProcessorFactory((InjectAnnotationProcessorFactory)service,
              getServiceProperties(event.getServiceReference()));
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING) {
          unbindInjectAnnotationProcessorFactory((InjectAnnotationProcessorFactory)service,
              getServiceProperties(event.getServiceReference()));
        }
      }
    }

    private Map<String, Object> getServiceProperties(ServiceReference reference) {
      Map<String, Object> props = new HashMap<>();
      String[] propertyKeys = reference.getPropertyKeys();
      for (String key : propertyKeys) {
        props.put(key, reference.getProperty(key));
      }
      return props;
    }

  }

}
