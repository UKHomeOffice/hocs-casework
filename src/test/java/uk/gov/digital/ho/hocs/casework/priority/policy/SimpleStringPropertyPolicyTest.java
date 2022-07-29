package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleStringPropertyPolicyTest {

    private SimpleStringPropertyPolicy policy;
    private StageWithCaseData stage;

    private static final String PROPERTY_NAME = "property1";
    private static final String PROPERTY_VALUE = "value1";
    private static final double POINTS_TO_AWARD = 10d;

    @Before
    public void before() {
        stage = new StageWithCaseData();
        policy = new SimpleStringPropertyPolicy(PROPERTY_NAME, PROPERTY_VALUE, POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaMatched() {
        stage.putData(PROPERTY_NAME, PROPERTY_VALUE);

        double result = policy.apply(stage);
        assertThat(result).isEqualTo(POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaNotMatched() {
        stage.putData(PROPERTY_NAME, "C");

        double result = policy.apply(stage);
        assertThat(result).isZero();
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(stage);
        assertThat(result).isZero();
    }
}
