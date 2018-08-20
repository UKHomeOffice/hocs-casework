package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.*;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataResourceTest {

    @Mock
    private CaseDataService caseDataService;

    private CaseDataResource caseDataResource;

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService);
    }

    @Test
    public void shouldCreateCase() {

        CaseType caseType = CaseType.MIN;
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        InputData inputData = new InputData(uuid);
        caseData.setInputData(inputData);
        CreateCaseRequest request = new CreateCaseRequest(caseType);

        when(caseDataService.createCase(any())).thenReturn(caseData);

        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseType);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCase() {

        CaseType caseType = CaseType.MIN;
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        InputData inputData = new InputData(uuid);
        caseData.setInputData(inputData);
        CreateCaseRequest request = new CreateCaseRequest(caseType);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid);

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldStoreDeadlines() {

        UpdateDeadlineRequest updateDeadlineRequest = new UpdateDeadlineRequest();
        Set<UpdateDeadlineRequest> deadlines = new HashSet<>();
        deadlines.add(updateDeadlineRequest);
        UpdateDeadlinesRequest updateDeadlinesRequest = new UpdateDeadlinesRequest(deadlines);

        ResponseEntity response = caseDataResource.storeDeadlines(updateDeadlinesRequest,uuid);

        verify(caseDataService, times(1)).updateDeadlines(uuid, deadlines);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
