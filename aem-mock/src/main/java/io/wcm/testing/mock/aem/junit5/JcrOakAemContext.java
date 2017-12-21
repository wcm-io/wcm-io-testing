package io.wcm.testing.mock.aem.junit5;

import org.apache.sling.testing.mock.sling.ResourceResolverType;

public class JcrOakAemContext extends AemContext {

    public JcrOakAemContext() {
        setResourceResolverType(ResourceResolverType.JCR_OAK);
    }

}
