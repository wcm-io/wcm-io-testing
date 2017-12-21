package io.wcm.testing.mock.aem.junit5;

import org.apache.sling.testing.mock.sling.ResourceResolverType;

public class JcrMockAemContext extends AemContext {

    public JcrMockAemContext() {
        setResourceResolverType(ResourceResolverType.JCR_MOCK);
    }

}
