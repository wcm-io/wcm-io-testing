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
package io.wcm.testing.mock.sling;

import io.wcm.testing.mock.osgi.MockOsgi;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.resourceresolver.impl.CommonResourceResolverFactoryImpl;
import org.apache.sling.resourceresolver.impl.ResourceAccessSecurityTracker;
import org.apache.sling.resourceresolver.impl.ResourceResolverImpl;
import org.apache.sling.resourceresolver.impl.helper.ResourceResolverContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

/**
 * Mock {@link ResourceResolver} implementation.
 * Simulates OSGi environment and intatiates real Sling ResourceResolver and JCR implementation, but
 * with a mocked JCR repository implementation underneath.
 */
class MockJcrResourceResolverFactory implements ResourceResolverFactory {

  private final SlingRepository slingRepository;

  public MockJcrResourceResolverFactory(final SlingRepository repository) {
    this.slingRepository = repository;
  }

  @Override
  public ResourceResolver getResourceResolver(final Map<String, Object> authenticationInfo) throws LoginException {
    // setup mock OSGi environment
    Dictionary<String, Object> resourceProviderFactoryProps = new Hashtable<>();
    resourceProviderFactoryProps.put(Constants.SERVICE_VENDOR, "sling-mock");
    resourceProviderFactoryProps.put(Constants.SERVICE_DESCRIPTION, "sling-mock");
    resourceProviderFactoryProps.put("resource.resolver.manglenamespaces", true);
    ComponentContext componentContext = MockOsgi.newComponentContext(resourceProviderFactoryProps);

    // setup mocked JCR environment
    componentContext.getBundleContext().registerService(SlingRepository.class.getName(), this.slingRepository, null);
    ServiceReference repositoryServiceReference = componentContext.getBundleContext().getServiceReference(SlingRepository.class.getName());

    // setup real sling JCR resource provider implementation for use in mocked context
    MockJcrResourceProviderFactory jcrResourceProviderFactory = new MockJcrResourceProviderFactory();
    jcrResourceProviderFactory.bindRepository(repositoryServiceReference);
    try {
      jcrResourceProviderFactory.activate(componentContext);
    }
    catch (RepositoryException ex) {
      throw new RuntimeException("Activating mocked JCR resource provider factory failed.", ex);
    }
    ResourceProvider resourceProvider = jcrResourceProviderFactory.getAdministrativeResourceProvider(new HashMap<String, Object>());
    Dictionary<Object, Object> resourceProviderProps = new Hashtable<>();
    resourceProviderProps.put(ResourceProvider.ROOTS, new String[] {
        "/"
    });
    componentContext.getBundleContext().registerService(ResourceProvider.class.getName(), resourceProvider, resourceProviderProps);
    ServiceReference resourceProviderServiceReference = componentContext.getBundleContext().getServiceReference(ResourceProvider.class.getName());

    // setup real sling resource resolver implementation for use in mocked context
    MockResourceResolverFactoryActivator activator = new MockResourceResolverFactoryActivator();
    activator.bindResourceProvider(resourceProvider, getServiceReferenceProperties(resourceProviderServiceReference));
    activator.activate(componentContext);
    CommonResourceResolverFactoryImpl commonFactoryImpl = new CommonResourceResolverFactoryImpl(activator);
    ResourceResolverContext context = new ResourceResolverContext(true, null, new ResourceAccessSecurityTracker());
    ResourceResolverImpl resourceResolver = new ResourceResolverImpl(commonFactoryImpl, context);
    return resourceResolver;
  }

  private Map<String, Object> getServiceReferenceProperties(final ServiceReference serviceReference) {
    Map<String, Object> props = new HashMap<>();
    String[] keys = serviceReference.getPropertyKeys();
    for (String key : keys) {
      props.put(key, serviceReference.getProperty(key));
    }
    return props;
  }

  @Override
  public ResourceResolver getAdministrativeResourceResolver(final Map<String, Object> authenticationInfo) throws LoginException {
    return getResourceResolver(authenticationInfo);
  }

  @Override
  public ResourceResolver getServiceResourceResolver(final Map<String, Object> authenticationInfo) throws LoginException {
    return getResourceResolver(authenticationInfo);
  }

}
