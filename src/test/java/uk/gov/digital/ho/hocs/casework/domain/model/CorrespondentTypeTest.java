package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentType.*;

public class CorrespondentTypeTest {

    @Test
    public void getDisplayValue() {
        assertThat(CORRESPONDENT.getDisplayValue()).isEqualTo("CORRESPONDENT");
        assertThat(CONSTITUENT.getDisplayValue()).isEqualTo("CONSTITUENT");
        assertThat(THIRD_PARTY.getDisplayValue()).isEqualTo("THIRD_PARTY");
        assertThat(APPLICANT.getDisplayValue()).isEqualTo("APPLICANT");
        assertThat(COMPLAINANT.getDisplayValue()).isEqualTo("COMPLAINANT");
        assertThat(FAMILY.getDisplayValue()).isEqualTo("FAMILY_RELATION");
        assertThat(FRIEND.getDisplayValue()).isEqualTo("FRIEND");
        assertThat(LEGAL_REP.getDisplayValue()).isEqualTo("LEGAL_REP");
        assertThat(MEMBER.getDisplayValue()).isEqualTo("MEMBER");
        assertThat(OTHER.getDisplayValue()).isEqualTo("OTHER");


    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(CORRESPONDENT, 0);
        assertOrderValue(CONSTITUENT, 1);
        assertOrderValue(THIRD_PARTY, 2);
        assertOrderValue(APPLICANT, 3);
        assertOrderValue(COMPLAINANT, 4);
        assertOrderValue(FAMILY, 5);
        assertOrderValue(FRIEND, 6);
        assertOrderValue(LEGAL_REP, 7);
        assertOrderValue(MEMBER, 8);
        assertOrderValue(OTHER, 9);


    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (CorrespondentType correspondentType : CorrespondentType.values()) {
            switch (correspondentType) {
                case CORRESPONDENT:
                case CONSTITUENT:
                case THIRD_PARTY:
                case APPLICANT:
                case COMPLAINANT:
                case FAMILY:
                case FRIEND:
                case LEGAL_REP:
                case MEMBER:
                case OTHER:
                    break;
                default:
                    fail("You've added a CorrespondentType, make sure you've written all the tests!");
            }
        }
    }

    private void assertOrderValue(CorrespondentType correspondentType, int value) {
        assertThat(correspondentType.ordinal()).isEqualTo(value);
    }
}