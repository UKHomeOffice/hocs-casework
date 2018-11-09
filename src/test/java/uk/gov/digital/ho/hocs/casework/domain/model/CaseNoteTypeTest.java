package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.digital.ho.hocs.casework.domain.model.CaseNoteType.MANUAL;
import static uk.gov.digital.ho.hocs.casework.domain.model.CaseNoteType.SYSTEM;

public class CaseNoteTypeTest {

    @Test
    public void getDisplayValue() {
        assertThat(MANUAL.getDisplayValue()).isEqualTo("MANUAL");
        assertThat(SYSTEM.getDisplayValue()).isEqualTo("SYSTEM");
    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(MANUAL, 0);
        assertOrderValue(SYSTEM, 1);
    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (CaseNoteType caseNoteType : CaseNoteType.values()) {
            switch (caseNoteType) {
                case MANUAL:
                case SYSTEM:
                    break;
                default:
                    fail("You've added a CaseNoteType, make sure you've written all the tests!");
            }
        }
    }

    private void assertOrderValue(CaseNoteType caseNoteType, int value) {
        assertThat(caseNoteType.ordinal()).isEqualTo(value);
    }
}