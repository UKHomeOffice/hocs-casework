package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;

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
    public void shouldCreateCase() throws EntityCreationException {
        final CaseType caseType = CaseType.MIN;

        when(caseDataService.createCase(any())).thenReturn(new CaseData(caseType.toString(), 1234L));
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getReference()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CreateCaseResponse.class);
    }

    @Test
    public void shouldCreateCaseException() throws EntityCreationException {
        final CaseType caseType = CaseType.MIN;

        when(caseDataService.createCase(any())).thenThrow(EntityCreationException.class);
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
        doNothing().when(caseDataService).updateCase(any());
        UpdateCaseRequest request = new UpdateCaseRequest();
        ResponseEntity response = caseDataResource.updateCase(uuid, request);

        verify(caseDataService, times(1)).updateCase(uuid);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldUpdateCaseCreateException() throws EntityCreationException, EntityNotFoundException {

        doThrow(EntityCreationException.class).when(caseDataService).updateCase(any());
        UpdateCaseRequest request = new UpdateCaseRequest();
        ResponseEntity response = caseDataResource.updateCase(uuid, request);

        verify(caseDataService, times(1)).updateCase(uuid);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCaseFindException() throws EntityCreationException, EntityNotFoundException {
        doThrow(EntityNotFoundException.class).when(caseDataService).updateCase(any());
        UpdateCaseRequest request = new UpdateCaseRequest();
        ResponseEntity response = caseDataResource.updateCase(uuid, request);

        verify(caseDataService, times(1)).updateCase(uuid);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
