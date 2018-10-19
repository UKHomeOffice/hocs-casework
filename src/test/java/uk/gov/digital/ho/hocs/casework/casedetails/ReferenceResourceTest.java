package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetReferenceResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Reference;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceResourceTest {

    @Mock
    private ReferenceDataService referenceDataService;

    private ReferenceDataResource referenceDataResource;

    private UUID caseUUID = UUID.randomUUID();

    @Before
    public void setUp() {
        referenceDataResource = new ReferenceDataResource(referenceDataService);
    }


    @Test
    public void shouldGetAllCorrespondentsForIndividualCase() {
        Reference reference = new Reference(caseUUID, ReferenceType.MEMBER_REFERENCE, "M101");

        when(referenceDataService.getReferenceData(caseUUID)).thenReturn(reference);
        ResponseEntity<GetReferenceResponse> response = referenceDataResource.getReference(caseUUID);

        verify(referenceDataService, times(1)).getReferenceData(caseUUID);

        verifyNoMoreInteractions(referenceDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}