package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCorrespondentDataResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCorrespondentsResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CorrespondentDataResourceTest {

    @Mock
    private CorrespondentDataService correspondentDataService;

    private CorrespondentDataResource correspondentDataResource;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID CORRESPONDENT_UUID = UUID.randomUUID();

    @Before
    public void setUp() {
        correspondentDataResource = new CorrespondentDataResource(correspondentDataService);
    }

    @Test
    public void shouldGetAllCorrespondentsForIndividualCase() {
        Set<CorrespondentData> Correspondents = new HashSet<>();
        CorrespondentData correspondentData =
                new CorrespondentData(
                        "Bob",
                        "S1 1DJ",
                        "1 somewhere street",
                        "some",
                        "Where",
                        "UK",
                        "01234 567890",
                        "A@A.com");

        when(correspondentDataService.getCorrespondents(CASE_UUID)).thenReturn(Correspondents);
        ResponseEntity<GetCorrespondentsResponse> response = correspondentDataResource.getCorrespondents(CASE_UUID);

        verify(correspondentDataService, times(1)).getCorrespondents(CASE_UUID);
        verifyNoMoreInteractions(correspondentDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCorrespondent() {
        when(correspondentDataService.getCorrespondent(CORRESPONDENT_UUID)).thenReturn(new CorrespondentData());

        ResponseEntity<GetCorrespondentDataResponse> response  = correspondentDataResource.getCorrespondent(CASE_UUID, CORRESPONDENT_UUID);

        verify(correspondentDataService, times(1)).getCorrespondent(CORRESPONDENT_UUID);
        verifyNoMoreInteractions(correspondentDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCorrespondentFromCase() {

        correspondentDataResource.deleteCorrespondentFromCase(CASE_UUID,CORRESPONDENT_UUID);

        verify(correspondentDataService, times(1)).deleteCorrespondent(CASE_UUID,CORRESPONDENT_UUID );

        verifyNoMoreInteractions(correspondentDataService);
    }

}