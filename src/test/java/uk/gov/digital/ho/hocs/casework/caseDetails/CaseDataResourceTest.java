package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataResourceTest {

    @Mock
    private CaseDataService caseDataService;

    private CaseDataResource caseDataResource;

    private final UUID uuid = UUID.randomUUID();
    private final Map<String, String> data = new HashMap<>();
    private final String testUser = "Test User";

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService);
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {
        final String caseType = "Case Type";

        when(caseDataService.createCase(any(), any())).thenReturn(new CaseData(caseType, 1234L));
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.createCase(request, testUser);

        verify(caseDataService).createCase(caseType, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CreateCaseResponse.class);
    }

    // @Test
    // public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
    //     final String stageName = "Stage Name";
    //     final String stageData = "{stage: data}";

    //     when(caseDataService.updateStage(any(UUID.class), any(UUID.class), anyString(), anyMap(), anyString())).thenReturn(
    //            new StageData(
    //                     UUID.randomUUID(), stageName, stageData)
    //     );
    //     UpdateRequest request = new UpdateStageRequest("Create", data);
    //     ResponseEntity response = caseDataResource.updateStage(request, testUser);

    //    verify(caseDataService).updateStage(uuid, uuid, "Create", data, testUser);
    //    assertThat(response).isNotNull();
    //    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    // }

}
