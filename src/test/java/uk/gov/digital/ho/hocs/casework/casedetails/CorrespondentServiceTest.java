package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.CreateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CorrespondentDataRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CorrespondentServiceTest {

    @Mock
    private CorrespondentDataRepository correspondentDataRepository;

    @Mock
    private CaseCorrespondentRepository caseCorrespondentRepository;

    private CorrespondentService correspondentService;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID CORRESPODENT_UUID = UUID.randomUUID();

    @Before
    public void setUp() {
        this.correspondentService = new CorrespondentService(
                correspondentDataRepository,
                caseCorrespondentRepository
        );
    }

    @Test
    public void shouldSaveCorrespondentData() {

        CreateCorrespondentRequest createCorrespondentRequest =
                new CreateCorrespondentRequest(null,
                        "Bob",
                        "S1 1DJ",
                        "1 somewhere street",
                        "some",
                        "Where",
                        "UK",
                        "01234 567890",
                        "A@A.com",
                        CorrespondentType.COMPLAINANT);

        correspondentService.assignCorrespondentToCase(CASE_UUID, UUID.randomUUID(), createCorrespondentRequest.getType());
        //verify(correspondentDataRepository, times(1)).save(any(Correspondent.class));
        //verify(caseCorrespondentRepository, times(1)).save(any(CaseCorrespondent.class));

        //verifyNoMoreInteractions(correspondentDataRepository);
        //verifyNoMoreInteractions(caseCorrespondentRepository);
    }


    @Test
    public void shouldGetAllCorrespondenceForIndividualCase() {
        UUID uuid = UUID.randomUUID();
        Set<Correspondent> correspondents = new HashSet<>();
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
        correspondents.add(correspondent);
        when(correspondentDataRepository.findByCaseUUID(uuid)).thenReturn(correspondents);
        Set<Correspondent> response = correspondentService.getCorrespondents(uuid);

        verify(correspondentDataRepository, times(1)).findByCaseUUID(uuid);
        verifyNoMoreInteractions(correspondentDataRepository);
        verifyNoMoreInteractions(caseCorrespondentRepository);

        assertThat(response.size()).isEqualTo(1);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCorrespondentMissingtNameException() throws EntityCreationException {
        CreateCorrespondentRequest createCorrespondentRequest =
                new CreateCorrespondentRequest(null,
                        null,
                        "S1 1DJ",
                        "1 somewhere street",
                        "some",
                        "Where",
                        "UK",
                        "01234 567890",
                        "A@A.com",
                        CorrespondentType.COMPLAINANT);
        correspondentService.createCorrespondent(CASE_UUID,
                createCorrespondentRequest.getFullname(),
                createCorrespondentRequest.getPostcode(),
                createCorrespondentRequest.getAddress1(),
                createCorrespondentRequest.getAddress2(),
                createCorrespondentRequest.getAddress3(),
                createCorrespondentRequest.getCountry(),
                createCorrespondentRequest.getTelephone(),
                createCorrespondentRequest.getEmail(),
                createCorrespondentRequest.getType());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateCorrespondentMissingTypeException() throws EntityCreationException {
        CreateCorrespondentRequest createCorrespondentRequest =
                new CreateCorrespondentRequest(null,
                        "Bob",
                        "S1 1DJ",
                        "1 somewhere street",
                        "some",
                        "Where",
                        "UK",
                        "01234 567890",
                        "A@A.com",
                        null);
        correspondentService.createCorrespondent(CASE_UUID,
                createCorrespondentRequest.getFullname(),
                createCorrespondentRequest.getPostcode(),
                createCorrespondentRequest.getAddress1(),
                createCorrespondentRequest.getAddress2(),
                createCorrespondentRequest.getAddress3(),
                createCorrespondentRequest.getCountry(),
                createCorrespondentRequest.getTelephone(),
                createCorrespondentRequest.getEmail(),
                createCorrespondentRequest.getType());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetCorrespondentsEntityNotFoundException() {
        when(correspondentDataRepository.findByCaseUUID(CASE_UUID)).thenReturn(null);
        correspondentService.getCorrespondents(CASE_UUID);

    }

    @Test
    public void shouldDeleteCorrespondentFromCase() {

        CaseCorrespondent caseCorrespondent = new CaseCorrespondent(1,CASE_UUID,CORRESPODENT_UUID, CorrespondentType.APPLICANT.getDisplayValue(),Boolean.FALSE);

        when(caseCorrespondentRepository.findByCaseUUIDAndCorrespondentUUID(CASE_UUID, CORRESPODENT_UUID)).thenReturn(caseCorrespondent);

        correspondentService.deleteCorrespondent(CASE_UUID, CORRESPODENT_UUID);

        verify(caseCorrespondentRepository, times(1)).findByCaseUUIDAndCorrespondentUUID(CASE_UUID, CORRESPODENT_UUID);
        verify(caseCorrespondentRepository, times(1)).save(any());

        verifyNoMoreInteractions(caseCorrespondentRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThroughExceptionWhenDeleteCorrespondentFromCaseButNullResponseFromDB() {

        when(caseCorrespondentRepository.findByCaseUUIDAndCorrespondentUUID(CASE_UUID, CORRESPODENT_UUID)).thenReturn(null);

        correspondentService.deleteCorrespondent(CASE_UUID, CORRESPODENT_UUID);
    }
}