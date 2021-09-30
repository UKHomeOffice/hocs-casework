package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.api.utils.CorrespondentTypeNameDecorator;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CorrespondentServiceTest {

    private static final UUID PREV_CASE_UUID = UUID.randomUUID();
    private final UUID caseUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final CaseDataType caseDataType = new CaseDataType("TEST", "1a", "TEST", "Testfield");
    @Mock
    private CorrespondentRepository correspondentRepository;
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private AuditClient auditClient;
    @Mock
    private InfoClient infoClient;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private CorrespondentTypeNameDecorator correspondentTypeNameDecorator;
    @Mock
    private CorrespondentService correspondentService;


    @Captor
    private ArgumentCaptor<Correspondent> correspondentRepoCapture = ArgumentCaptor.forClass(Correspondent.class);

    @Captor
    private ArgumentCaptor<Correspondent> correspondentAuditCaptor = ArgumentCaptor.forClass(Correspondent.class);

    @Before
    public void setUp() {
        correspondentService = Mockito.spy(new CorrespondentService(correspondentRepository, caseDataRepository, auditClient, infoClient, caseDataService, correspondentTypeNameDecorator));
    }

    @Test
    public void shouldCreateCorrespondent() {

        // given
        Set<CorrespondentWithPrimaryFlag> primaryFlagSet = Set.of(getCorrespondentWithPrimaryFlag());
        CorrespondentWithPrimaryFlag correspondent = getCorrespondentWithPrimaryFlag();
        when(correspondentRepository.findAllByCaseUUID(caseUUID)).thenReturn(primaryFlagSet);

        //when
        correspondentService.createCorrespondent(caseUUID, correspondent);

        // then
        ArgumentCaptor<Correspondent> repoCaptureCorrespondent = ArgumentCaptor.forClass(Correspondent.class);
        verify(correspondentRepository).save(repoCaptureCorrespondent.capture());
        Correspondent paramCorrespondent = repoCaptureCorrespondent.getValue();
        assertThat(paramCorrespondent.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(paramCorrespondent.getCorrespondentType()).isEqualTo(correspondent.getCorrespondentType());
        assertThat(paramCorrespondent.getAddress1()).isEqualTo(correspondent.getAddress1());
        assertThat(paramCorrespondent.getAddress2()).isEqualTo(correspondent.getAddress2());
        assertThat(paramCorrespondent.getAddress3()).isEqualTo(correspondent.getAddress3());
        assertThat(paramCorrespondent.getCountry()).isEqualTo(correspondent.getCountry());
        assertThat(paramCorrespondent.getPostcode()).isEqualTo(correspondent.getPostcode());
        assertThat(paramCorrespondent.getFullName()).isEqualTo(correspondent.getFullName());
        assertThat(paramCorrespondent.getTelephone()).isEqualTo(correspondent.getTelephone());
        assertThat(paramCorrespondent.getEmail()).isEqualTo(correspondent.getEmail());
        assertThat(paramCorrespondent.getExternalKey()).isEqualTo(correspondent.getExternalKey());

        ArgumentCaptor<Correspondent> auditCaptureCorrespondent = ArgumentCaptor.forClass(Correspondent.class);
        verify(auditClient).createCorrespondentAudit(auditCaptureCorrespondent.capture());
        assertThat(auditCaptureCorrespondent.getValue().getCaseUUID()).isEqualTo(caseUUID);

        verify(correspondentRepository).findAllByCaseUUID(caseUUID);
        verify(caseDataService).updatePrimaryCorrespondent( eq(caseUUID),any(), any());
    }

    @Test
    public void getAllActiveCorrespondentsThenFindAllActive() {
        Correspondent correspondent = getCorrespondent();
        Set<Correspondent> correspondentsExpected = Set.of(correspondent);
        GetCorrespondentTypeResponse emptyCorrespondentSet = new GetCorrespondentTypeResponse(Collections.emptySet());

        when(correspondentRepository.findAllActive()).thenReturn(correspondentsExpected);
        when(infoClient.getAllCorrespondentType()).thenReturn(emptyCorrespondentSet);
        when(correspondentTypeNameDecorator.addCorrespondentTypeName(emptyCorrespondentSet.getCorrespondentTypes(), correspondentsExpected)).thenReturn(correspondentsExpected);

        Set<Correspondent> correspondents = correspondentService.getAllActiveCorrespondents();

        assertThat(correspondents).isNotNull();
        assertThat(correspondents).isSameAs(correspondentsExpected);
        verify(correspondentRepository).findAllActive();
        verifyNoMoreInteractions(correspondentRepository);
    }

    private CorrespondentWithPrimaryFlag getCorrespondentWithPrimaryFlag() {
        Correspondent correspondent = getCorrespondent();
        return new CorrespondentWithPrimaryFlag(correspondent.getCaseUUID(),
                correspondent.getCorrespondentType(),
                correspondent.getFullName(),
                Address.builder()
                        .postcode(correspondent.getPostcode())
                        .address1(correspondent.getAddress1())
                        .address2(correspondent.getAddress1())
                        .address3(correspondent.getAddress3())
                        .country(correspondent.getCountry())
                        .build(),
                correspondent.getTelephone(),
                correspondent.getEmail(),
                correspondent.getReference(),
                correspondent.getExternalKey(),
                Boolean.TRUE);
    }

    private Correspondent getCorrespondent() {
        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";
        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, address, phone, email, reference, externalKey);
        return correspondent;
    }


    @Test
    public void shouldGetCorrespondentTypes() {

        when(caseDataRepository.getCaseType(caseUUID)).thenReturn("TEST");
        CorrespondentTypeDto correspondentTypeDto = new CorrespondentTypeDto();
        GetCorrespondentTypeResponse getCorrespondentTypeResponse = new GetCorrespondentTypeResponse(Set.of(correspondentTypeDto));
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
        Set<CorrespondentTypeDto> emptyCorrespondentSet = Collections.emptySet();

        when(correspondentRepository.findAllByCaseUUID(caseUUID)).thenReturn(correspondents);
        doReturn(emptyCorrespondentSet).when(correspondentService).getCorrespondentTypes(caseUUID);
        when(correspondentTypeNameDecorator.addCorrespondentTypeName(emptyCorrespondentSet, correspondents)).thenReturn(correspondents);

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
        Set<CorrespondentTypeDto> emptyCorrespondentSet = Collections.emptySet();

        when(correspondentRepository.findByUUID(caseUUID, correspondent.getUuid())).thenReturn(correspondent);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        doReturn(emptyCorrespondentSet).when(correspondentService).getCorrespondentTypes(caseUUID);
        when(correspondentTypeNameDecorator.addCorrespondentTypeName(emptyCorrespondentSet, correspondent)).thenReturn(correspondent);

        correspondentService.deleteCorrespondent(caseUUID, stageUUID, correspondent.getUuid());

        verify(correspondentRepository).findByUUID(caseUUID, correspondent.getUuid());
        verify(correspondentRepository).save(correspondent);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
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
        Set<CorrespondentTypeDto> emptyCorrespondentSet = Collections.emptySet();

        when(correspondentRepository.findByUUID(caseUUID, correspondent.getUuid())).thenReturn(correspondent);
        when(caseDataRepository.findActiveByUuid(caseUUID)).thenReturn(caseData);
        doReturn(emptyCorrespondentSet).when(correspondentService).getCorrespondentTypes(caseUUID);
        when(correspondentTypeNameDecorator.addCorrespondentTypeName(emptyCorrespondentSet, correspondent)).thenReturn(correspondent);

        correspondentService.deleteCorrespondent(caseUUID, stageUUID, correspondent.getUuid());

        verify(correspondentRepository).findByUUID(caseUUID, correspondent.getUuid());
        verify(correspondentRepository).save(correspondent);
        verify(caseDataRepository).findActiveByUuid(caseUUID);
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
        when(caseDataRepository.getCaseType(testCaseUUID)).thenReturn("TEST");
        CorrespondentTypeDto correspondentTypeDto = new CorrespondentTypeDto();
        GetCorrespondentTypeResponse getCorrespondentTypeResponse = new GetCorrespondentTypeResponse(Set.of(correspondentTypeDto));
        when(infoClient.getCorrespondentType("TEST")).thenReturn(getCorrespondentTypeResponse);

        // WHEN
        correspondentService.updateCorrespondent(testCaseUUID, testCorrespondenceUUID, testRequest);

        // THEN

        verify(correspondentRepository, times(1)).findByUUID(testCaseUUID, testCorrespondenceUUID);
        verify(correspondentRepository, times(1)).save(correspondentRepoCapture.capture());
        verify(caseDataRepository, times(1)).getCaseType(any());
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
        verifyNoMoreInteractions(caseDataRepository);

    }
}
