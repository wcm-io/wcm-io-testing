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

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.adapter.AdapterManager;

/**
 * Mock {@link AdapterManager} implementation.
 */
class MockAdapterManager implements AdapterManager {

  /**
   * Simple list of all adapter factories, they are processed in thier order of registration.
   * The official sling implementation does not have a "controllable" order as well, it calls the
   * factories in alphabetical order of their bundle IDs.
   */
  private final List<AdapterFactory> adapterFactories = new ArrayList<AdapterFactory>();

  /**
   * Returns the adapted <code>adaptable</code> or <code>null</code> if the object cannot be adapted.
   */
  @Override
  public <AdapterType> AdapterType getAdapter(final Object adaptable, final Class<AdapterType> type) {

    // iterate over all adapter factories and try to adapt the object
    for (AdapterFactory adapterFactory : this.adapterFactories) {
      AdapterType instance = adapterFactory.getAdapter(adaptable, type);
      if (instance != null) {
        return instance;
      }
    }

    // no matching adapter factory found
    return null;
  }

  /**
   * Register a adapter factory
   * @param adapterFactory Adapter factory
   */
  public void register(final AdapterFactory adapterFactory) {
    if (!this.adapterFactories.contains(adapterFactory)) {
      this.adapterFactories.add(adapterFactory);
    }
  }

  /**
   * Removes all registrations from adapter factory.
   */
  public void clearRegistrations() {
    this.adapterFactories.clear();
  }

}
