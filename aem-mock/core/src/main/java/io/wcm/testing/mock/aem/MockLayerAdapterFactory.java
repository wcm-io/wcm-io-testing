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
package io.wcm.testing.mock.aem;

import java.io.IOException;
import java.io.InputStream;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.AdapterFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;

import com.day.image.Layer;

/**
 * Tries to adapt to an InputStream to get a Layer instance for an image binary.
 */
@Component(service = AdapterFactory.class,
    property = {
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.adapter.Adaptable",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.image.Layer"
    })
@ProviderType
public final class MockLayerAdapterFactory implements AdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public @Nullable <AdapterType> AdapterType getAdapter(@NotNull Object object, @NotNull Class<AdapterType> type) {
    if (type == Layer.class && object instanceof Adaptable) {
      try (InputStream is = ((Adaptable)object).adaptTo(InputStream.class)) {
        if (is != null) {
          return (AdapterType)new Layer(is);
        }
      }
      catch (IOException ex) {
        // ignore
      }
    }
    return null;
  }

}
