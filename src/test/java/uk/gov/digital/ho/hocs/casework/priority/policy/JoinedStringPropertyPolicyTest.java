package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinedStringPropertyPolicyTest {

    private JoinedStringPropertyPolicy policy;

    private static final String PROPERTY_NAME_1 = "property1";
    private static final String PROPERTY_VALUE_1 = "value1";
    private static final String PROPERTY_NAME_2 = "property2";
    private static final String PROPERTY_VALUE_2 = "value2";
    private static final double POINTS_TO_AWARD = 10d;

    @Before
    public void before() {
        policy = new JoinedStringPropertyPolicy(PROPERTY_NAME_1, PROPERTY_VALUE_1, PROPERTY_NAME_2, PROPERTY_VALUE_2, POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaMatched() {
        double result = policy.apply(Map.of(PROPERTY_NAME_1, PROPERTY_VALUE_1, PROPERTY_NAME_2, PROPERTY_VALUE_2), Collections.emptySet());
        assertThat(result).isEqualTo(POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaNotMatched() {
        double result = policy.apply(Map.of(PROPERTY_NAME_1, "1", PROPERTY_NAME_2, "2"), Collections.emptySet());
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(Map.of(), Collections.emptySet());
        assertThat(result).isEqualTo(0);
    }
}
