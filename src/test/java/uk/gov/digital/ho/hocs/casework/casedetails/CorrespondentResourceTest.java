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
import uk.gov.digital.ho.hocs.casework.casedetails.model.Correspondent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CorrespondentResourceTest {

    @Mock
    private CorrespondentService correspondentService;

    private CorrespondentResource correspondentResource;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID CORRESPONDENT_UUID = UUID.randomUUID();

    @Before
    public void setUp() {
        correspondentResource = new CorrespondentResource(correspondentService);
    }

    @Test
    public void shouldGetAllCorrespondentsForIndividualCase() {
        Set<Correspondent> Correspondents = new HashSet<>();
        Correspondent correspondent =
                new Correspondent(
                        "Bob",
                        "S1 1DJ",
                        "1 somewhere street",
                        "some",
                        "Where",
                        "UK",
                        "01234 567890",
                        "A@A.com");

        when(correspondentService.getCorrespondents(CASE_UUID)).thenReturn(Correspondents);
        ResponseEntity<GetCorrespondentsResponse> response = correspondentResource.getCorrespondents(CASE_UUID);

        verify(correspondentService, times(1)).getCorrespondents(CASE_UUID);
        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCorrespondent() {
        when(correspondentService.getCorrespondent(CORRESPONDENT_UUID)).thenReturn(new Correspondent());

        ResponseEntity<GetCorrespondentDataResponse> response = correspondentResource.getCorrespondent(CASE_UUID, CORRESPONDENT_UUID);

        verify(correspondentService, times(1)).getCorrespondent(CORRESPONDENT_UUID);
        verifyNoMoreInteractions(correspondentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCorrespondentFromCase() {

        correspondentResource.deleteCorrespondentFromCase(CASE_UUID, CORRESPONDENT_UUID);

        verify(correspondentService, times(1)).deleteCorrespondent(CASE_UUID, CORRESPONDENT_UUID);

        verifyNoMoreInteractions(correspondentService);
    }

}