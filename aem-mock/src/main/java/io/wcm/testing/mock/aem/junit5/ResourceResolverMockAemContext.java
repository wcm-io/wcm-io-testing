package io.wcm.testing.mock.aem.junit5;

import org.apache.sling.testing.mock.sling.ResourceResolverType;

public class ResourceResolverMockAemContext extends AemContext {

    public ResourceResolverMockAemContext() {
        setResourceResolverType(ResourceResolverType.RESOURCERESOLVER_MOCK);
    }

}
