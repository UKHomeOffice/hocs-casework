package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.digital.ho.hocs.casework.domain.model.StageStatusType.*;

public class StageStatusTypeTest {

    @Test
    public void getDisplayValue() {
        assertThat(CREATED.getDisplayValue()).isEqualTo("CREATED");
        assertThat(UPDATED.getDisplayValue()).isEqualTo("UPDATED");
        assertThat(REJECTED.getDisplayValue()).isEqualTo("REJECTED");
        assertThat(COMPLETED.getDisplayValue()).isEqualTo("COMPLETED");

    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(CREATED, 0);
        assertOrderValue(UPDATED, 1);
        assertOrderValue(REJECTED, 2);
        assertOrderValue(COMPLETED, 3);

    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (StageStatusType stageStatusType : StageStatusType.values()) {
            switch (stageStatusType) {
                case CREATED:
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