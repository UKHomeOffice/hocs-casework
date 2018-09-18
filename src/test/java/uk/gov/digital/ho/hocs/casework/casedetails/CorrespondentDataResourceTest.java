package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Before
    public void setUp() {
        correspondentDataResource = new CorrespondentDataResource(correspondentDataService);
    }

    @Test
    public void shouldGetAllCorrespondentsForIndividualCase() {
        UUID uuid = UUID.randomUUID();
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

        when(correspondentDataService.getCorrespondents(uuid)).thenReturn(Correspondents);
        ResponseEntity<GetCorrespondentsResponse> response = correspondentDataResource.getCorrespondents(uuid);

        verify(correspondentDataService, times(1)).getCorrespondents(uuid);

        verifyNoMoreInteractions(correspondentDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}