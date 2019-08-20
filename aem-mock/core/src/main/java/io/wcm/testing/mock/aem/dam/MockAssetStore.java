/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.api.handler.store.AssetStore;

/**
 * Mock implementation of {@link AssetStore}
 */
@Component(service = AssetStore.class)
public final class MockAssetStore implements AssetStore {

  @Reference(cardinality = ReferenceCardinality.MULTIPLE,
      policy = ReferencePolicy.STATIC,
      policyOption = ReferencePolicyOption.GREEDY)
  private Collection<AssetHandler> assetHandlers;

  @Override
  public AssetHandler getAssetHandler(String mimeType) {
    for (AssetHandler assetHandler : assetHandlers) {
      if (ArrayUtils.contains(assetHandler.getMimeTypes(), mimeType)) {
        return assetHandler;
      }
    }
    return null;
  }

  @Override
  public AssetHandler[] getAllAssetHandler() {
    return assetHandlers.toArray(new AssetHandler[assetHandlers.size()]);
  }


  // --- unsupported operations ---

  @Override
  public String getFileNameSuffix(String arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getIconPath(String arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getMimeType(String arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
  public Map<String, com.day.cq.dam.api.handler.store.AssetHandlerInfo> getAssetHandlerInfos() {
    throw new UnsupportedOperationException();
  }

}
