package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ReferenceDataRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

    @Mock
    private ReferenceDataRepository referenceDataRepository;

    private ReferenceDataService referenceDataService;

    private UUID caseUUID = UUID.randomUUID();

    @Before
    public void setUp() {
        this.referenceDataService = new ReferenceDataService(referenceDataRepository);
    }

    @Test
    public void ShouldCreateReferenceData() {

        referenceDataService.createReference(caseUUID, ReferenceType.MEMBER_REFERENCE, "M101");

        verify(referenceDataRepository, times(1)).save(any(ReferenceData.class));

        verifyNoMoreInteractions(referenceDataRepository);

    }

    @Test(expected = EntityCreationException.class)
    public void ShouldNotCreateReferenceMissingCaseUUIDException() {

        referenceDataService.createReference(null, ReferenceType.MEMBER_REFERENCE, "M101");
    }

    @Test(expected = EntityCreationException.class)
    public void ShouldNotCreateReferenceMissingReferenceTypeException() {

        referenceDataService.createReference(caseUUID, null, "M101");
    }

    @Test(expected = EntityCreationException.class)
    public void ShouldNotCreateReferenceMissingReferenceException() {

        referenceDataService.createReference(caseUUID, ReferenceType.MEMBER_REFERENCE, null);
    }

    @Test
    public void shouldReturnReferenceForCase() {

        when(referenceDataRepository.findByCaseUUID(caseUUID)).thenReturn(new ReferenceData(caseUUID, ReferenceType.MEMBER_REFERENCE, "M101"));

        ReferenceData response = referenceDataService.getReferenceData(caseUUID);
        verify(referenceDataRepository, times(1)).findByCaseUUID(caseUUID);
        verifyNoMoreInteractions(referenceDataRepository);

        assertThat(response.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(response.getReferenceType()).isEqualTo(ReferenceType.MEMBER_REFERENCE);
        assertThat(response.getReference()).isEqualTo("M101");
    }
}