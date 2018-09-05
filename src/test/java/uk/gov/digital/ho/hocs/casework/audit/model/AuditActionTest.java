package uk.gov.digital.ho.hocs.casework.audit.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static uk.gov.digital.ho.hocs.casework.audit.model.AuditAction.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditActionTest {

    @Test
    public void shouldNotAccidentallyChangeTheOrderValuesOfAuditAction() {
        assertOrderValue(CREATE_CASE, 0);
        assertOrderValue(CREATE_STAGE, 1);
        assertOrderValue(UPDATE_CASE, 2);
        assertOrderValue(UPDATE_STAGE, 3);
        assertOrderValue(GET_CASE, 4);
        assertOrderValue(SEARCH, 5);
        assertOrderValue(SEND_EMAIL, 6);
        assertOrderValue(CSV_EXTRACT, 7);
        assertOrderValue(ADD_DOCUMENT, 8);
        assertOrderValue(UPDATE_DOCUMENT, 9);
        assertOrderValue(SET_INACTIVE_STAGE, 10);
        assertOrderValue(SET_ACTIVE_STAGE, 11);
    }

    @Test
    public void shouldNotAccidentallyAddValuesOfAuditAction() {
        for (AuditAction auditAction : AuditAction.values()) {
            switch (auditAction) {
                case CREATE_CASE:
                case CREATE_STAGE:
                case UPDATE_CASE:
                case UPDATE_STAGE:
                case GET_CASE:
                case SEARCH:
                case SEND_EMAIL:
                case CSV_EXTRACT:
                case ADD_DOCUMENT:
                case UPDATE_DOCUMENT:
                case SET_INACTIVE_STAGE:
                case SET_ACTIVE_STAGE:
                    break;
                default:
                    fail("You've added an audit action, make sure you've written all the tests!");
            }
        }
    }

    private void assertOrderValue(AuditAction auditAction, int value) {
        assertThat(auditAction.ordinal()).isEqualTo(value);
    }

}
