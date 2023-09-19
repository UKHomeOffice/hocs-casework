package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentsResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.api.utils.JsonResponseStreamer;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CorrespondentResourceTest {

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID stageUUID = UUID.randomUUID();

    private final UUID correspondentUUID = UUID.randomUUID();

    @Mock
    private CorrespondentService correspondentService;

    @Mock
    private JsonResponseStreamer jsonResponseStreamer;

    private CorrespondentResource correspondentResource;

    @Before
    public void setUp() {
        correspondentResource = new CorrespondentResource(correspondentService, jsonResponseStreamer);

        // Ensure stream supplier is invoked
        when(jsonResponseStreamer.jsonStringsWrappedTransactionalStreamingResponseBody(eq("correspondents"), any()))
            .thenAnswer(invocation -> {
                invocation.<Supplier<?>>getArgument(1).get();
                return ResponseEntity.ok().build();
            });
    }

    @Test
    public void getAllActiveCorrespondents() {
        when(correspondentService.streamCorrespondentOutlineJson(false)).thenReturn(Stream.of());

        ResponseEntity<StreamingResponseBody> response = correspondentResource.getAllActiveCorrespondents(false);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(correspondentService).streamCorrespondentOutlineJson(false);
        verifyNoMoreInteractions(correspondentService);
    }

    @Test
    public void getAllCorrespondents() {
        when(correspondentService.streamCorrespondentOutlineJson(true)).thenReturn(Stream.of());

        ResponseEntity<StreamingResponseBody> response = correspondentResource.getAllActiveCorrespondents(true);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(correspondentService).streamCorrespondentOutlineJson(true);
        verifyNoMoreInteractions(correspondentService);
    }

    @Test
    public void shouldAddCorrespondentToCase() {
        doNothing().when(correspondentService).createCorrespondent(eq(caseUUID), eq(stageUUID), eq("any"),
            eq("anyFullName"), eq("organisation"), any(Address.class), eq("anyPhone"), eq("anyEmail"),
            eq("anyReference"), eq("external key"));

        CreateCorrespondentRequest createCorrespondentRequest = new CreateCorrespondentRequest("any", "anyFullName",
            "organisation", "anyPostcode", "any1", "any2", "any3", "anyCountry", "anyPhone", "anyEmail", "anyReference",
            "external key");
        ResponseEntity<Void> response = correspondentResource.addCorrespondentToCase(caseUUID, stageUUID,
            createCorrespondentRequest);

        verify(correspondentService, times(1)).createCorrespondent(eq(caseUUID), eq(stageUUID), eq("any"),
            eq("anyFullName"), eq("organisation"), any(Address.class), eq("anyPhone"), eq("anyEmail"),
            eq("anyReference"), eq("external key"));

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
        Correspondent correspondent = new Correspondent(caseUUID, "CORRESPONDENT", "anyFullName", "organisation",
            address, "anyPhone", "anyEmail", "anyReference", "external key");

        when(correspondentService.getCorrespondent(caseUUID, correspondentUUID)).thenReturn(correspondent);

        ResponseEntity<GetCorrespondentResponse> response = correspondentResource.getCorrespondent(caseUUID,
            correspondentUUID);

        verify(correspondentService, times(1)).getCorrespondent(caseUUID, correspondentUUID);

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCorrespondentType() {

        CorrespondentTypeDto correspondentTypeDto = new CorrespondentTypeDto();
        when(correspondentService.getCorrespondentTypes(caseUUID)).thenReturn(
            new HashSet<>(List.of(correspondentTypeDto)));

        ResponseEntity<GetCorrespondentTypeResponse> response = correspondentResource.getCorrespondentType(caseUUID);

        verify(correspondentService).getCorrespondentTypes(caseUUID);
        verifyNoMoreInteractions(correspondentService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCorrespondentFromCase() {

        doNothing().when(correspondentService).deleteCorrespondent(caseUUID, stageUUID, correspondentUUID);

        ResponseEntity<GetCorrespondentResponse> response = correspondentResource.deleteCorrespondent(caseUUID, stageUUID, correspondentUUID);

        verify(correspondentService, times(1)).deleteCorrespondent(caseUUID, stageUUID, correspondentUUID);

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateCorrespondentOnCase() {

        UpdateCorrespondentRequest updateCorrespondentRequest = new UpdateCorrespondentRequest("anyFullName",
            "organisation", "anyPostcode", "any1", "any2", "any3", "anyCountry", "anyPhone", "anyEmail",
            "anyReference");
        doNothing().when(correspondentService).updateCorrespondent(eq(caseUUID), eq(correspondentUUID),
            eq(updateCorrespondentRequest));

        ResponseEntity<Void> response = correspondentResource.updateCorrespondent(caseUUID, stageUUID, correspondentUUID,
            updateCorrespondentRequest);

        verify(correspondentService, times(1)).updateCorrespondent(eq(caseUUID), eq(correspondentUUID),
            eq(updateCorrespondentRequest));

        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
