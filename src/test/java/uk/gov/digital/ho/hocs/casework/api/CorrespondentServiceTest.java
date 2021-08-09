package uk.gov.digital.ho.hocs.casework.api;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import javax.validation.constraints.NotEmpty;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class CorrespondentServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final CaseDataType caseDataType = new CaseDataType("TEST", "1a", "TEST");
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

    @Captor
    private ArgumentCaptor<Correspondent> correspondentRepoCapture = ArgumentCaptor.forClass(Correspondent.class);

    @Captor
    private ArgumentCaptor<Correspondent> correspondentAuditCaptor = ArgumentCaptor.forClass(Correspondent.class);

    @Before
    public void setUp() {
        correspondentService = new CorrespondentService(correspondentRepository, caseDataRepository, auditClient, infoClient, caseDataService);
    }

    @Test
    public void getAllActiveCorrespondentsThenFindAllActive() {
        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";
        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, address, phone, email, reference, externalKey);
        Set<Correspondent> correspondentsExpected = Set.of(correspondent);
        when(correspondentRepository.findAllActive()).thenReturn(correspondentsExpected);

        Set<Correspondent> correspondents = correspondentService.getAllActiveCorrespondents();

        assertThat(correspondents).isNotNull();
        assertThat(correspondents).isSameAs(correspondentsExpected);
        verify(correspondentRepository).findAllActive();
        verifyNoMoreInteractions(correspondentRepository);
    }

    @Test
    public void shouldGetCorrespondentTypes() {

        when(caseDataRepository.getCaseType(caseUUID)).thenReturn("TEST");
        CorrespondentTypeDto correspondentTypeDto = new CorrespondentTypeDto();
        GetCorrespondentTypeResponse getCorrespondentTypeResponse = new GetCorrespondentTypeResponse(new HashSet(Arrays.asList(correspondentTypeDto)));
        when(infoClient.getCorrespondentType("TEST")).thenReturn(getCorrespondentTypeResponse);

        Set<CorrespondentTypeDto> CorrespondentTypeDtos = correspondentService.getCorrespondentTypes(caseUUID);

        verify(caseDataRepository).getCaseType(caseUUID);
        verifyNoMoreInteractions(caseDataRepository);
        verify(infoClient).getCorrespondentType("TEST");
        verifyNoMoreInteractions(infoClient);
        assertThat(CorrespondentTypeDtos.size()).isEqualTo(1);
    }

    @Test
    public void shouldGetCorrespondents() {

        Address address = new Address("postcode", "line1", "line2", "line3", "country");

        CorrespondentWithPrimaryFlag correspondent = new CorrespondentWithPrimaryFlag(
          caseUUID,
          "Type",
          "full name",
                address,
                "01923478393",
                "email@test.com",
                "ref",
                "key",
                true
        );
        Set<CorrespondentWithPrimaryFlag> correspondents = Set.of(correspondent);

        when(correspondentRepository.findAllByCaseUUID(caseUUID)).thenReturn(correspondents);

        Set<CorrespondentWithPrimaryFlag> expectedCorrespondents = correspondentService.getCorrespondents(caseUUID);

        verify(correspondentRepository).findAllByCaseUUID(caseUUID);
        verifyNoMoreInteractions(correspondentRepository);
        Assert.assertEquals(expectedCorrespondents, correspondents);
    }

    @Test
    public void shouldDeleteCorrespondent() {

        Address address = new Address("postcode", "line1", "line2", "line3", "country");
        Correspondent correspondent = new Correspondent(
                caseUUID,
                "Type",
                "full name",
                address,
                "01923478393",
                "email@test.com",
                "ref",
                "key");
        CaseData caseData = new CaseData(caseDataType, 1L, null);
        when(correspondentRepository.findByUUID(caseUUID, correspondent.getUuid())).thenReturn(correspondent);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(caseData);

        correspondentService.deleteCorrespondent(caseUUID, stageUUID, correspondent.getUuid());

        verify(correspondentRepository).findByUUID(caseUUID, correspondent.getUuid());
        verify(correspondentRepository).save(correspondent);
        verify(caseDataRepository).findByUuid(caseUUID);
        verifyNoMoreInteractions(correspondentRepository);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldDeleteCorrespondentAndRemovePrimaryCorrespondent() {

        Address address = new Address("postcode", "line1", "line2", "line3", "country");
        Correspondent correspondent = new Correspondent(
                caseUUID,
                "Type",
                "full name",
                address,
                "01923478393",
                "email@test.com",
                "ref",
                "key");
        CaseData caseData = new CaseData(caseDataType, 1L, null);
        caseData.setPrimaryCorrespondentUUID(correspondent.getUuid());
        when(correspondentRepository.findByUUID(caseUUID, correspondent.getUuid())).thenReturn(correspondent);
        when(caseDataRepository.findByUuid(caseUUID)).thenReturn(caseData);

        correspondentService.deleteCorrespondent(caseUUID, stageUUID, correspondent.getUuid());

        verify(correspondentRepository).findByUUID(caseUUID, correspondent.getUuid());
        verify(correspondentRepository).save(correspondent);
        verify(caseDataRepository).findByUuid(caseUUID);
        verify(caseDataRepository).save(caseData);
        verifyNoMoreInteractions(correspondentRepository);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldUpdateAddressCorrectly() {

        // GIVEN
        UUID testCaseUUID = UUID.randomUUID();
        UUID testCorrespondenceUUID = UUID.randomUUID();
        @NotEmpty String testFullname = "test name";
        String testPostcode = "T3 5ST";
        String testAdd1 = "Test House";
        String testAdd2 = "Test Street";
        String testAdd3 = "Test Village";
        String testCountry = "TestCountry";
        String testTelephone = "07900100100";
        String testReference = "TestRef";
        String testEmail = "test@test.com";

        UpdateCorrespondentRequest  testRequest = new UpdateCorrespondentRequest(
                testFullname,
                testPostcode,
                testAdd1,
                testAdd2,
                testAdd3,
                testCountry,
                testTelephone,
                testEmail,
                testReference
        );


        Correspondent mockDBResponse = new Correspondent(testCaseUUID, "SomeType" ,testFullname, null, null, null, null, null);
        when(correspondentRepository.findByUUID(testCaseUUID, testCorrespondenceUUID)).thenReturn(mockDBResponse);

        // WHEN
        correspondentService.updateCorrespondent(testCaseUUID, testCorrespondenceUUID, testRequest);

        // THEN
        verify(correspondentRepository, times(1)).findByUUID(testCaseUUID, testCorrespondenceUUID);
        verify(correspondentRepository, times(1)).save(correspondentRepoCapture.capture());
        verify(auditClient, times(1)).updateCorrespondentAudit(any());

        Correspondent captureOutput = correspondentRepoCapture.getValue();

        assertThat(captureOutput.getFullName()).isEqualTo(testFullname);
        assertThat(captureOutput.getPostcode()).isEqualTo(testPostcode);
        assertThat(captureOutput.getAddress1()).isEqualTo(testAdd1);
        assertThat(captureOutput.getAddress2()).isEqualTo(testAdd2);
        assertThat(captureOutput.getAddress3()).isEqualTo(testAdd3);
        assertThat(captureOutput.getCountry()).isEqualTo(testCountry);
        assertThat(captureOutput.getEmail()).isEqualTo(testEmail);
        assertThat(captureOutput.getCaseUUID()).isEqualTo(testCaseUUID);

        verifyNoMoreInteractions(correspondentRepository, auditClient);
        verifyNoInteractions(caseDataRepository);

    }
}
