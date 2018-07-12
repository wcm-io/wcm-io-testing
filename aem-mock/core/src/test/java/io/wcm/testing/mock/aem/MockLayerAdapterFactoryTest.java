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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.AdapterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.image.Layer;

@RunWith(MockitoJUnitRunner.class)
public class MockLayerAdapterFactoryTest {

  @Mock
  Adaptable adaptable;

  private AdapterFactory underTest;

  @Before
  public void setUp() {
    underTest = new MockLayerAdapterFactory();
  }

  @Test
  public void testImage() throws IOException {
    InputStream is = spy(getClass().getResourceAsStream("/sample-image.gif"));
    when(adaptable.adaptTo(InputStream.class)).thenReturn(is);
    Layer layer = underTest.getAdapter(adaptable, Layer.class);
    assertNotNull(layer);
    assertEquals(2, layer.getWidth());
    assertEquals(2, layer.getHeight());
    verify(is).close();
  }

  @Test
  public void testInvalidInputStream() throws IOException {
    InputStream is = spy(new ByteArrayInputStream(new byte[] {
        0x01, 0x02, 0x03
    }));
    when(adaptable.adaptTo(InputStream.class)).thenReturn(is);
    Layer layer = underTest.getAdapter(adaptable, Layer.class);
    assertNull(layer);
    verify(is).close();
  }

  @Test
  public void testNoInputStream() {
    when(adaptable.adaptTo(InputStream.class)).thenReturn(null);
    Layer layer = underTest.getAdapter(adaptable, Layer.class);
    assertNull(layer);
  }

  @Test
  public void testNoAdaptable() {
    Layer layer = underTest.getAdapter(new Object(), Layer.class);
    assertNull(layer);
  }

}
