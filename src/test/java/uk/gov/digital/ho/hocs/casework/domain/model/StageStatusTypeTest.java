package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.digital.ho.hocs.casework.domain.model.StageStatusType.*;

public class StageStatusTypeTest {

    @Test
    public void getDisplayValue() {
        assertThat(UNASSIGNED.getDisplayValue()).isEqualTo("UNASSIGNED");
        assertThat(TEAM_ASSIGNED.getDisplayValue()).isEqualTo("TEAM_ASSIGNED");
        assertThat(USER_ASSIGNED.getDisplayValue()).isEqualTo("USER_ASSIGNED");
        assertThat(UPDATED.getDisplayValue()).isEqualTo("UPDATED");
        assertThat(REJECTED.getDisplayValue()).isEqualTo("REJECTED");
        assertThat(COMPLETED.getDisplayValue()).isEqualTo("COMPLETED");

    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(UNASSIGNED, 0);
        assertOrderValue(TEAM_ASSIGNED, 1);
        assertOrderValue(USER_ASSIGNED, 2);
        assertOrderValue(UPDATED, 3);
        assertOrderValue(REJECTED, 4);
        assertOrderValue(COMPLETED, 5);

    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (StageStatusType stageStatusType : StageStatusType.values()) {
            switch (stageStatusType) {
                case UNASSIGNED:
                case TEAM_ASSIGNED:
                case USER_ASSIGNED:
                case UPDATED:
                case REJECTED:
                case COMPLETED:
                    break;
                default:
                    fail("You've added a StageStatusType, make sure you've written all the tests!");
            }
        }
    }

    private void assertOrderValue(StageStatusType stageStatusType, int value) {
        assertThat(stageStatusType.ordinal()).isEqualTo(value);
    }
}