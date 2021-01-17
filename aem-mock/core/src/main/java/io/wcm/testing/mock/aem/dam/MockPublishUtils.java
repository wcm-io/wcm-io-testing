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
package io.wcm.testing.mock.aem.dam;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import com.day.cq.dam.api.s7dam.utils.PublishUtils;

/**
 * Mock implementation of {@link PublishUtils}.
 */
@Component(service = PublishUtils.class)
public final class MockPublishUtils implements PublishUtils {

  /**
   * Hostname that is returned as scene7 image server URL.
   */
  public static final String DUMMY_HOST = "https://dummy.scene7.com";

  @Override
  public String[] externalizeImageDeliveryAsset(Resource assetResource) throws RepositoryException {
    return new String[] {
        DUMMY_HOST,
        assetResource.getPath()
    };
  }

  // --- unsupported operations ---

  @Override
  public String externalizeImageDeliveryAsset(Resource resource, String assetPath) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String externalizeImageDeliveryUrl(Resource arg0, String arg1) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPublishNodeURL(Resource arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getISProperty(String arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

}
