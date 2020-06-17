package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class CorrespondentServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final CaseDataType caseDataType = new CaseDataType("TEST", "1a", "TEST");
    @Mock
    private CorrespondentRepository correspondentRepository;
    @Mock
    private CaseDataRepository caseDataRepository;
    private CorrespondentService correspondentService;
    @Mock
    AuditClient auditClient;
    @Mock
    InfoClient infoClient;

    @Before
    public void setUp() {
        correspondentService = new CorrespondentService(correspondentRepository, caseDataRepository, auditClient, infoClient);
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
}