package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class DeadlinePolicyTest {

    private DeadlinePolicy policy;

    private StageWithCaseData stage;

    @Before
    public void before() {
        stage = new StageWithCaseData();
        policy = new DeadlinePolicy();
        ;
    }

    @Test
    public void apply_withDateNow() {
        stage.setDeadline(LocalDate.now());

        double result = policy.apply(stage);

        assertThat(result).isZero();
    }

    @Test
    public void apply_withDateInFuture() {
        stage.setDeadline(LocalDate.now().plusDays(1));

        double result = policy.apply(stage);

        assertThat(result).isEqualTo(-1);
    }

    @Test
    public void apply_withDateInPast() {
        stage.setDeadline(LocalDate.now().minusDays(1));

        double result = policy.apply(stage);

        assertThat(result).isEqualTo(1);
    }

}
