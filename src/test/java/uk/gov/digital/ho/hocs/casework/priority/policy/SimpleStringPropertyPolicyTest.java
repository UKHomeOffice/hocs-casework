package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleStringPropertyPolicyTest {

    private SimpleStringPropertyPolicy policy;

    private static final String PROPERTY_NAME = "property1";
    private static final String PROPERTY_VALUE = "value1";
    private static final double POINTS_TO_AWARD = 10d;

    @Before
    public void before() {
        policy = new SimpleStringPropertyPolicy(PROPERTY_NAME, PROPERTY_VALUE, POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaMatched() {
        double result = policy.apply(Map.of(PROPERTY_NAME, PROPERTY_VALUE), Collections.emptySet());
        assertThat(result).isEqualTo(POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaNotMatched() {
        double result = policy.apply(Map.of(PROPERTY_NAME, "C"), Collections.emptySet());
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(Map.of(), Collections.emptySet());
        assertThat(result).isEqualTo(0);
    }
}
