package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CaseCreateRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CaseSaveResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.UpdateStageRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseDetails;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseResourceTest {

    @Mock
    private CaseService caseService;

    private CaseResource caseResource;

    private final UUID uuid = UUID.randomUUID();
    private final Map<String, Object> data = new HashMap<>();
    private final String testUser = "Test User";

    @Before
    public void setUp() {
        caseResource = new CaseResource(caseService);
    }

    @Test
    public void shouldCreateCase() {
        final String caseType = "Case Type";

        when(caseService.createCase(any(), any())).thenReturn(new CaseDetails(caseType, 1234L));
        CaseCreateRequest request = new CaseCreateRequest(caseType);
        ResponseEntity response = caseResource.createCase(request, testUser);

        verify(caseService).createCase(caseType, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CaseSaveResponse.class);
    }

    @Test
    public void shouldUpdateCase() {
        final String stageName = "Stage Name";
        final String stageData = "{stage: data}";

        when(caseService.updateStage(any(UUID.class), anyInt(), anyMap(), anyString())).thenReturn(
                new StageDetails(
                        UUID.randomUUID(), stageName, 1, stageData)
        );
        UpdateStageRequest request = new UpdateStageRequest(uuid, 1, data);
        ResponseEntity response = caseResource.updateCase(UUID.randomUUID(), request, testUser);

        verify(caseService).updateStage(uuid, 1, data, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
