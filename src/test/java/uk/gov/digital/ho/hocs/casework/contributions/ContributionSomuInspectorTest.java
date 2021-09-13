package uk.gov.digital.ho.hocs.casework.contributions;

import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.util.UUID;

import static org.junit.Assert.*;

public class ContributionSomuInspectorTest {
    private final UUID caseUUID = UUID.randomUUID();
    private final UUID somuUUID = UUID.randomUUID();
    private final UUID somuTypeUuid = UUID.randomUUID();
    
    @Test
    public void shouldReturnContributionDueDateIfPresent() {
        String jsonString = "{ \"contributionDueDate\" : \"2020-10-10\"}";
        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, jsonString);
        ContributionSomuInspector somuInspector = new ContributionSomuInspector(somuItem);
        assertEquals("2020-10-10", somuInspector.getContributionDueDate());
    }
    
    @Test(expected = PathNotFoundException.class)
    public void shouldThrowExceptionIfContributionDueDateNotPresent() {
        String jsonString = "{}";
        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, jsonString);
        ContributionSomuInspector somuInspector = new ContributionSomuInspector(somuItem);
        somuInspector.getContributionDueDate();
    }
    
    @Test
    public void shouldReturnContributionStatusIfPresent() {
        String jsonString = "{ \"contributionStatus\" : \"contributionReceived\"}";
        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, jsonString);
        ContributionSomuInspector somuInspector = new ContributionSomuInspector(somuItem);
        assertEquals(ContributionStatus.CONTRIBUTION_RECEIVED, somuInspector.getContributionStatus());
    }
    
    @Test
    public void shouldReturnHasContributionStatusIfPresent() {
        String jsonString = "{ \"contributionStatus\" : \"contributionReceived\"}";
        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, jsonString);
        ContributionSomuInspector somuInspector = new ContributionSomuInspector(somuItem);
        assertTrue(somuInspector.hasContributionStatus());
    }
    
    @Test
    public void shouldReturnEmptyStringIfContributionStatusNotPresent() {
        String jsonString = "{}";
        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, jsonString);
        ContributionSomuInspector somuInspector = new ContributionSomuInspector(somuItem);
        assertEquals(ContributionStatus.NONE, somuInspector.getContributionStatus());
    }
    
}
