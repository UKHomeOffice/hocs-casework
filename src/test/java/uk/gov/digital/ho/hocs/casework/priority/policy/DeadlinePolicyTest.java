package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DeadlinePolicyTest {

    private DeadlinePolicy policy;

    private CaseData caseData;

    private ActiveStage activeStage;

    @Before
    public void before() {
        var caseUUID = UUID.randomUUID();
        caseData = new CaseData(caseUUID, LocalDateTime.now(), "MIN", "MIN/123456/22", false, Map.of(), null, null,
            null, null, Collections.emptySet(), null, null, LocalDate.now(), false, null, Collections.emptySet(),
            Set.of());

        activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null, null, null,
            caseUUID, null, null, caseData, null, null, null);
        caseData.setActiveStages(Set.of(activeStage));
        policy = new DeadlinePolicy();
    }

    @Test
    public void apply_withDateNow() {
        activeStage.setDeadline(LocalDate.now());

        double result = policy.apply(caseData, activeStage);

        assertThat(result).isZero();
    }

    @Test
    public void apply_withDateInFuture() {
        activeStage.setDeadline(LocalDate.now().plusDays(1));

        double result = policy.apply(caseData, activeStage);

        assertThat(result).isEqualTo(-1);
    }

    @Test
    public void apply_withDateInPast() {
        activeStage.setDeadline(LocalDate.now().minusDays(1));

        double result = policy.apply(caseData, activeStage);

        assertThat(result).isEqualTo(1);
    }

}
