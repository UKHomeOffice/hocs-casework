package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CorrespondentResourceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final UUID correspondentUUID = UUID.randomUUID();
    @Mock
    private CorrespondentService correspondentService;
    private CorrespondentResource correspondentResource;

    @Before
    public void setUp() {
        correspondentResource = new CorrespondentResource(correspondentService);
    }

    @Test
    public void getAllActiveCorrespondents(){
        when(correspondentService.getAllActiveCorrespondents()).thenReturn(new HashSet<>());

        ResponseEntity<GetCorrespondentOutlinesResponse> response = correspondentResource.getAllActiveCorrespondents();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(correspondentService).getAllActiveCorrespondents();
        verifyNoMoreInteractions(correspondentService);
    }

    @Test
    public void shouldAddCorrespondentToCase() throws JsonProcessingException {

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");

        doNothing().when(correspondentService).createCorrespondent(eq(caseUUID), eq(stageUUID), eq("any"), eq("anyFullName"), eq("organisation"), any(Address.class), eq("anyPhone"), eq("anyEmail"), eq("anyReference"), eq("external key"));

        CreateCorrespondentRequest createCorrespondentRequest = new CreateCorrespondentRequest("any", "anyFullName", "organisation", "anyPostcode", "any1", "any2", "any3", "anyCountry", "anyPhone", "anyEmail", "anyReference", "external key");
        ResponseEntity response = correspondentResource.addCorrespondentToCase(caseUUID, stageUUID, createCorrespondentRequest);

        verify(correspondentService, times(1)).createCorrespondent(eq(caseUUID), eq(stageUUID), eq("any"), eq("anyFullName"), eq("organisation"),  any(Address.class), eq("anyPhone"), eq("anyEmail"), eq("anyReference"), eq("external key"));

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCorrespondents() {

        when(correspondentService.getCorrespondents(caseUUID)).thenReturn(new HashSet<>());

        ResponseEntity<GetCorrespondentsResponse> response = correspondentResource.getCorrespondents(caseUUID);

        verify(correspondentService, times(1)).getCorrespondents(caseUUID);

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCorrespondent() {

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        Correspondent correspondent = new Correspondent(caseUUID, "CORRESPONDENT", "anyFullName", "organisation", address, "anyPhone", "anyEmail", "anyReference", "external key");

        when(correspondentService.getCorrespondent(caseUUID, correspondentUUID)).thenReturn(correspondent);

        ResponseEntity<GetCorrespondentResponse> response = correspondentResource.getCorrespondent(caseUUID, correspondentUUID);

        verify(correspondentService, times(1)).getCorrespondent(caseUUID, correspondentUUID);

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCorrespondentType() {

        CorrespondentTypeDto correspondentTypeDto = new CorrespondentTypeDto();
        when(correspondentService.getCorrespondentTypes(caseUUID)).thenReturn(new HashSet(Arrays.asList(correspondentTypeDto)));

        ResponseEntity<GetCorrespondentTypeResponse> response = correspondentResource.getCorrespondentType(caseUUID);

        verify(correspondentService).getCorrespondentTypes(caseUUID);
        verifyNoMoreInteractions(correspondentService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCorrespondentFromCase() {

        doNothing().when(correspondentService).deleteCorrespondent(caseUUID, stageUUID, correspondentUUID);

        ResponseEntity response = correspondentResource.deleteCorrespondent(caseUUID, stageUUID, correspondentUUID);

        verify(correspondentService, times(1)).deleteCorrespondent(caseUUID, stageUUID, correspondentUUID);

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateCorrespondentOnCase() {

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");

        UpdateCorrespondentRequest updateCorrespondentRequest = new UpdateCorrespondentRequest("anyFullName", "organisation","anyPostcode", "any1", "any2", "any3", "anyCountry", "anyPhone", "anyEmail", "anyReference");
        doNothing().when(correspondentService).updateCorrespondent(eq(caseUUID), eq(correspondentUUID), eq(updateCorrespondentRequest));

        ResponseEntity response = correspondentResource.updateCorrespondent(caseUUID, stageUUID, correspondentUUID, updateCorrespondentRequest);

        verify(correspondentService, times(1)).updateCorrespondent(eq(caseUUID), eq(correspondentUUID), eq(updateCorrespondentRequest));

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}