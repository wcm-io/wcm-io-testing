package io.wcm.testing.mock.aem.models.policy;

import com.day.cq.wcm.api.policies.ContentPolicy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = ContentPolicy.class)
public interface ContentPolicyTestModel {

    @ValueMapValue(via = "properties")
    String getProp1();

    @ValueMapValue(via = "properties")
    boolean getProp2();
}
