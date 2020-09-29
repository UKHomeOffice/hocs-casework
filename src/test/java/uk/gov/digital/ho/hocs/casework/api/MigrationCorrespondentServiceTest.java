package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCorrespondentServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final UUID correspondentUUID = UUID.randomUUID();
    @Mock
    private CorrespondentRepository correspondentRepository;
    @Mock
    private CaseDataRepository caseDataRepository;
    private CorrespondentService correspondentService;
    @Mock
    private AuditClient auditClient;
    @Mock
    private InfoClient infoClient;
    @Mock
    private CaseDataService caseDataService;

    @Before
    public void setUp() {
        correspondentService = new CorrespondentService(correspondentRepository, caseDataRepository, auditClient, infoClient, caseDataService);
    }

    @Test
    public void shouldGetCorrespondents() throws ApplicationExceptions.EntityNotFoundException {
        HashSet<CorrespondentWithPrimaryFlag> correspondentData = new HashSet<>();

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        CorrespondentWithPrimaryFlag correspondent = new CorrespondentWithPrimaryFlag(
                caseUUID,
                "CORRESPONDENT",
                "anyFullName",
                address,
                "anyPhone",
                "anyEmail",
                "anyReference",
                "external key",
                true
        );

        correspondentData.add(correspondent);

        when(correspondentRepository.findAllByCaseUUID(caseUUID)).thenReturn(correspondentData);

        correspondentService.getCorrespondents(caseUUID);

        verify(correspondentRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(correspondentRepository);
    }

    @Test
    public void shouldNotGetCorrespondentsMissingUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        correspondentService.getCorrespondents(null);

        verify(correspondentRepository, times(1)).findAllByCaseUUID(null);

        verifyNoMoreInteractions(correspondentRepository);
    }

    @Test
    public void shouldCreateCorrespondent_firstCorrespondent() throws ApplicationExceptions.EntityCreationException, JsonProcessingException {

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        CorrespondentWithPrimaryFlag correspondent = new CorrespondentWithPrimaryFlag(
                caseUUID,
                "CORRESPONDENT",
                "anyFullName",
                address,
                "anyPhone",
                "anyEmail",
                "anyReference",
                "external key",
                true
        );

        when(correspondentRepository.findAllByCaseUUID(caseUUID)).thenReturn(Set.of(correspondent));
        correspondentService.createCorrespondent(caseUUID, stageUUID,"CORRESPONDENT", "anyFullName", address, "anyPhone", "anyEmail", "anyReference", "external key");

        verify(correspondentRepository).save(any(Correspondent.class));
        verify(correspondentRepository).findAllByCaseUUID(caseUUID);
        verify(caseDataService).updatePrimaryCorrespondent(caseUUID, stageUUID, correspondent.getUuid());

        verifyNoMoreInteractions(correspondentRepository, caseDataService);
    }


    @Test
    public void shouldCreateCorrespondent_multipleCorrespondents() throws ApplicationExceptions.EntityCreationException, JsonProcessingException {

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        CorrespondentWithPrimaryFlag correspondent1 = new CorrespondentWithPrimaryFlag(
                caseUUID,
                "CORRESPONDENT",
                "anyFullName1",
                address,
                "anyPhone1",
                "anyEmail1",
                "anyReference1",
                "external key1",
                true
        );
        CorrespondentWithPrimaryFlag correspondent2 = new CorrespondentWithPrimaryFlag(
                caseUUID,
                "CORRESPONDENT",
                "anyFullName2",
                address,
                "anyPhone2",
                "anyEmail2",
                "anyReference2",
                "external key2",
                true
        );

        when(correspondentRepository.findAllByCaseUUID(caseUUID)).thenReturn(Set.of(correspondent1, correspondent2));
        correspondentService.createCorrespondent(caseUUID, stageUUID,"CORRESPONDENT", "anyFullName2", address, "anyPhone2", "anyEmail2", "anyReference2", "external key2");

        verify(correspondentRepository).save(any(Correspondent.class));
        verify(correspondentRepository).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(correspondentRepository, caseDataService);
    }

    @Test
    public void shouldAuditCreateCorrespondent() throws ApplicationExceptions.EntityCreationException, JsonProcessingException {

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        correspondentService.createCorrespondent(caseUUID, stageUUID, "CORRESPONDENT", "anyFullName", address, "anyPhone", "anyEmail", "anyReference", "external key");

        verify(auditClient, times(1)).createCorrespondentAudit(any(Correspondent.class));

        verifyNoMoreInteractions(auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCorrespondentMissingCaseUUIDException() throws ApplicationExceptions.EntityCreationException, JsonProcessingException {
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        correspondentService.createCorrespondent(null, stageUUID, "CORRESPONDENT", "anyFullName", address, "anyPhone", "anyEmail", "anyReference", "external key");
    }

    @Test
    public void shouldNotCreateCorrespondentMissingCaseUUID() throws JsonProcessingException {

        try {
            Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
            correspondentService.createCorrespondent(null, stageUUID, "CORRESPONDENT", "anyFullName", address, "anyPhone", "anyEmail", "anyReference", "external key");
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(correspondentRepository);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCorrespondentMissingCorrespondentTypeException() throws ApplicationExceptions.EntityCreationException, JsonProcessingException {
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        correspondentService.createCorrespondent(caseUUID, stageUUID, null, "anyFullName", address, "anyPhone", "anyEmail", "anyReference", "external key");
    }

    @Test
    public void shouldNotCreateCorrespondentMissingCorrespondentType() throws JsonProcessingException {

        try {
            Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
            correspondentService.createCorrespondent(caseUUID, stageUUID, null, "anyFullName", address, "anyPhone", "anyEmail", "anyReference", "external key");
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(correspondentRepository);

    }

    @Test
    public void shouldGetCorrespondent() throws ApplicationExceptions.EntityNotFoundException {

        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        Correspondent correspondent = new Correspondent(caseUUID, "CORRESPONDENT", "anyFullName", address, "anyPhone", "anyEmail", "anyReference", "external key");

        when(correspondentRepository.findByUUID(correspondent.getCaseUUID(), correspondent.getUuid())).thenReturn(correspondent);

        correspondentService.getCorrespondent(correspondent.getCaseUUID(), correspondent.getUuid());

        verify(correspondentRepository, times(1)).findByUUID(correspondent.getCaseUUID(), correspondent.getUuid());

        verifyNoMoreInteractions(correspondentRepository);

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetCorrespondentNotFoundException() {

        when(correspondentRepository.findByUUID(caseUUID, correspondentUUID)).thenReturn(null);

        correspondentService.getCorrespondent(caseUUID, correspondentUUID);
    }

    @Test
    public void shouldNotGetCorrespondenteNotFound() {

        when(correspondentRepository.findByUUID(caseUUID, correspondentUUID)).thenReturn(null);

        try {
            correspondentService.getCorrespondent(caseUUID, correspondentUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(correspondentRepository, times(1)).findByUUID(caseUUID, correspondentUUID);

        verifyNoMoreInteractions(correspondentRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetCorrespondentMissingCaseUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        correspondentService.getCorrespondent(null, correspondentUUID);

    }

    @Test
    public void shouldNotGetCorrespondentMissingCaseUUID() {

        try {
            correspondentService.getCorrespondent(null, correspondentUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(correspondentRepository, times(1)).findByUUID(null, correspondentUUID);

        verifyNoMoreInteractions(correspondentRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetCorrespondentMissingCorrespondentUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        correspondentService.getCorrespondent(caseUUID, null);

    }

    @Test
    public void shouldDeleteCorrespondent() {

        Correspondent correspondent = new Correspondent(caseUUID, "any", null, null, null, null, null, null);

        when(correspondentRepository.findByUUID(correspondent.getCaseUUID(), correspondent.getUuid())).thenReturn(correspondent);

        correspondentService.deleteCorrespondent(correspondent.getCaseUUID(), correspondent.getUuid());

        verify(correspondentRepository, times(1)).findByUUID(correspondent.getCaseUUID(), correspondent.getUuid());
        verify(correspondentRepository, times(1)).save(correspondent);


        verifyNoMoreInteractions(correspondentRepository);
    }

    @Test
    public void shouldAuditDeleteCorrespondent() {

        Correspondent correspondent = new Correspondent(caseUUID, "any", null, null, null, null, null, null);

        when(correspondentRepository.findByUUID(correspondent.getCaseUUID(), correspondent.getUuid())).thenReturn(correspondent);

        correspondentService.deleteCorrespondent(correspondent.getCaseUUID(), correspondent.getUuid());

        verify(auditClient, times(1)).deleteCorrespondentAudit(correspondent);


        verifyNoMoreInteractions(auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotDeleteCorrespondentMissingCaseUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        correspondentService.deleteCorrespondent(null, correspondentUUID);

    }

    @Test
    public void shouldNotDeleteCorrespondentMissingCaseUUID() {

        try {
            correspondentService.deleteCorrespondent(null, correspondentUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(correspondentRepository, times(1)).findByUUID(null, correspondentUUID);

        verifyNoMoreInteractions(correspondentRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotDeleteCorrespondentMissingCorrespondentUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        correspondentService.deleteCorrespondent(caseUUID, null);

    }

    @Test
    public void shouldNotDeleteCorrespondentMissingCorrespondentUUID() {

        try {
            correspondentService.deleteCorrespondent(caseUUID, null);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(correspondentRepository, times(1)).findByUUID(caseUUID, null);

        verifyNoMoreInteractions(correspondentRepository);
    }
}
