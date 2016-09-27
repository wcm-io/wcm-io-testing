package io.wcm.testing.mock.aem;

import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class MockAssetManagerTest {

    @Rule
    public AemContext context = TestAemContext.newAemContext();

    @Test
    public void testCreateAsset() throws IOException {
        InputStream testImage = openTestAsset();
        String assetName = "myasset.gif";
        String mimeType = "image/gif";

        Asset asset = context.assetManager().createAsset(context.uniqueRoot().dam() + '/' +assetName , testImage, "image/gif", true);

        assertNotNull(asset);
        assertNotNull(asset.getOriginal().getStream());
        assertTrue(IOUtils.contentEquals(openTestAsset(), asset.getOriginal().getStream()));
        assertEquals(asset.getName(), assetName);
        assertEquals(asset.getMimeType(), mimeType);
    }

    private InputStream openTestAsset() {
        return getClass().getClassLoader().getResourceAsStream("sample-image.gif");
    }
}
