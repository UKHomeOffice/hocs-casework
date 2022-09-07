package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinedStringPropertyPolicyTest {

    private static final String PROPERTY_NAME_1 = "property1";

    private static final String PROPERTY_VALUE_1 = "value1";

    private static final String PROPERTY_NAME_2 = "property2";

    private static final String PROPERTY_VALUE_2 = "value2";

    private static final double POINTS_TO_AWARD = 10d;

    private JoinedStringPropertyPolicy policy;

    private StageWithCaseData stage;

    @Before
    public void before() {
        stage = new StageWithCaseData();
        policy = new JoinedStringPropertyPolicy(PROPERTY_NAME_1, PROPERTY_VALUE_1, PROPERTY_NAME_2, PROPERTY_VALUE_2,
            POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaMatched() {
        stage.putData(PROPERTY_NAME_1, PROPERTY_VALUE_1);
        stage.putData(PROPERTY_NAME_2, PROPERTY_VALUE_2);

        double result = policy.apply(stage);
        assertThat(result).isEqualTo(POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaNotMatched() {
        stage.putData(PROPERTY_NAME_1, "1");
        stage.putData(PROPERTY_NAME_2, "2");

        double result = policy.apply(stage);

        assertThat(result).isZero();
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(stage);
        assertThat(result).isZero();
    }

}
