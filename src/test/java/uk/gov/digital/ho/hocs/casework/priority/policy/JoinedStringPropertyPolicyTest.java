package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinedStringPropertyPolicyTest {

    private JoinedStringPropertyPolicy policy;

    private CaseData caseData;

    private ActiveStage activeStage;

    private static final String PROPERTY_NAME_1 = "property1";

    private static final String PROPERTY_VALUE_1 = "value1";

    private static final String PROPERTY_NAME_2 = "property2";

    private static final String PROPERTY_VALUE_2 = "value2";

    private static final double POINTS_TO_AWARD = 10d;

    @Before
    public void before() {
        var caseUUID = UUID.randomUUID();
        caseData = new CaseData(caseUUID, LocalDateTime.now(), "MIN", "MIN/123456/22", false, new HashMap<>(), null,
            null, null, null, Collections.emptySet(), null, null, LocalDate.now(), false, null, Collections.emptySet(),
            Set.of());

        activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null, null, null,
            caseUUID, null, null, caseData, null, null, null);
        caseData.setActiveStages(Set.of(activeStage));
        policy = new JoinedStringPropertyPolicy(PROPERTY_NAME_1, PROPERTY_VALUE_1, PROPERTY_NAME_2, PROPERTY_VALUE_2,
            POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaMatched() {
        caseData.update(PROPERTY_NAME_1, PROPERTY_VALUE_1);
        caseData.update(PROPERTY_NAME_2, PROPERTY_VALUE_2);

        double result = policy.apply(caseData, activeStage);
        assertThat(result).isEqualTo(POINTS_TO_AWARD);
    }

    @Test
    public void apply_criteriaNotMatched() {
        caseData.update(PROPERTY_NAME_1, "1");
        caseData.update(PROPERTY_NAME_2, "2");

        double result = policy.apply(caseData, activeStage);

        assertThat(result).isZero();
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(caseData, activeStage);
        assertThat(result).isZero();
    }

}
