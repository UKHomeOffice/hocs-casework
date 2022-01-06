package uk.gov.digital.ho.hocs.casework.client.infoclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InfoClientTest {

    @Mock
    RestHelper restHelper;

    private InfoClient infoClient;

    @Before
    public void setup() {
        infoClient = new InfoClient(restHelper, "infoService");
    }

    @Test
    public void getAllCorrespondentType() {
        CorrespondentTypeDto correspondentTypeDto = new CorrespondentTypeDto();
        GetCorrespondentTypeResponse getCorrespondentType = new GetCorrespondentTypeResponse(new HashSet<>(Arrays.asList(correspondentTypeDto)));
        when(restHelper.get("infoService", "/correspondentType", GetCorrespondentTypeResponse.class)).thenReturn(getCorrespondentType);

        GetCorrespondentTypeResponse getCorrespondentTypeResponse = infoClient.getAllCorrespondentType();

        assertThat(getCorrespondentTypeResponse).isNotNull();
        assertThat(getCorrespondentTypeResponse.getCorrespondentTypes()).isNotNull();
        assertThat(getCorrespondentTypeResponse.getCorrespondentTypes().size()).isEqualTo(1);

        verify(restHelper).get("infoService", "/correspondentType", GetCorrespondentTypeResponse.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getCorrespondentType() {

        CorrespondentTypeDto correspondentTypeDto = new CorrespondentTypeDto();
        GetCorrespondentTypeResponse getCorrespondentType = new GetCorrespondentTypeResponse(new HashSet<>(Arrays.asList(correspondentTypeDto)));
        when(restHelper.get("infoService", "/correspondentType/CASE_TYPE", GetCorrespondentTypeResponse.class)).thenReturn(getCorrespondentType);

        GetCorrespondentTypeResponse getCorrespondentTypeResponse = infoClient.getCorrespondentType("CASE_TYPE");

        assertThat(getCorrespondentTypeResponse).isNotNull();
        assertThat(getCorrespondentTypeResponse.getCorrespondentTypes()).isNotNull();
        assertThat(getCorrespondentTypeResponse.getCorrespondentTypes().size()).isEqualTo(1);

        verify(restHelper).get("infoService", "/correspondentType/CASE_TYPE", GetCorrespondentTypeResponse.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTeamByStageAndText() {

        TeamDto teamDto = new TeamDto();
        when(restHelper.get("infoService", "/team/stage/stageType/text/text", TeamDto.class)).thenReturn(teamDto);

        TeamDto result = infoClient.getTeamByStageAndText("stageType", "text");

        assertThat(result).isEqualTo(teamDto);

        verify(restHelper).get("infoService", "/team/stage/stageType/text/text", TeamDto.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocumentTags() {
        List<String> tags = new ArrayList(Arrays.asList("tag"));
        when(restHelper.get("infoService", "/caseType/TEST/documentTags", new ParameterizedTypeReference<List<String>>() {
        })).thenReturn(tags);

        List<String> response = infoClient.getDocumentTags("TEST");

        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    public void getEntityListTotalsReturnsEntityList() {
        EntityTotalDto entityTotalDto = new EntityTotalDto(new HashMap(), new HashMap());
        EntityDto<EntityTotalDto> entityDto = new EntityDto<EntityTotalDto>("simpleName", entityTotalDto);
        List<EntityDto<EntityTotalDto>> entityListTotals = new ArrayList();
        entityListTotals.add(entityDto);
        when(restHelper.get("infoService", "/entity/list/listName", new ParameterizedTypeReference<List<EntityDto<EntityTotalDto>>>() {
        })).thenReturn(entityListTotals);

        List<EntityDto<EntityTotalDto>> response = infoClient.getEntityListTotals("listName");

        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        verify(restHelper).get("infoService", "/entity/list/listName", new ParameterizedTypeReference<List<EntityDto<EntityTotalDto>>>() {
        });
        verifyNoMoreInteractions(restHelper);
    }


    @Test
    public void getPriorityPoliciesForCaseType() {
        String policyType = "POLICYB";
        String caseType = "CASE_TYPE_A";
        Map<String, String> config = Map.of("propertyB", "valueB");
        List<PriorityPolicyDto> priorityPolicyDtos = Collections.singletonList(new PriorityPolicyDto(policyType, caseType, config));
        when(restHelper.get("infoService", "/priority/policy/" + caseType, new ParameterizedTypeReference<List<PriorityPolicyDto>>() {
        })).thenReturn(priorityPolicyDtos);


        List<PriorityPolicyDto> results = infoClient.getPriorityPoliciesForCaseType(caseType);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(priorityPolicyDtos.size());
        assertThat(results.get(0).getPolicyType()).isEqualTo(policyType);
        assertThat(results.get(0).getCaseType()).isEqualTo(caseType);
        assertThat(results.get(0).getConfig()).isEqualTo(config);

        verify(restHelper).get("infoService", "/priority/policy/" + caseType, new ParameterizedTypeReference<List<PriorityPolicyDto>>() {
        });
        verifyNoMoreInteractions(restHelper);

    }

    @Test
    public void getProfileByCaseType() {

        ProfileDto profileDto = new ProfileDto("prof1", true, List.of());
        when(restHelper.get("infoService", "/profile/forcasetype/caseTypeA", ProfileDto.class)).thenReturn(profileDto);

        ProfileDto result = infoClient.getProfileByCaseType("caseTypeA");

        assertThat(result).isEqualTo(profileDto);

        verify(restHelper).get("infoService", "/profile/forcasetype/caseTypeA", ProfileDto.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getUsersForTeam() {
        List<UserDto> users = List.of(new UserDto(UUID.randomUUID().toString(), "username", "firstName", "lastName", "email@test.com"));

        UUID teamUUID = UUID.randomUUID();
        when(restHelper.get("infoService", "/teams/" + teamUUID.toString() + "/members", new ParameterizedTypeReference<List<UserDto>>() {
        })).thenReturn(users);

        List<UserDto> response = infoClient.getUsersForTeam(teamUUID);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(users);

        verify(restHelper).get("infoService", "/teams/" + teamUUID.toString() + "/members", new ParameterizedTypeReference<List<UserDto>>() {
        });
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getUsersForTeamByStage() {
        List<UserDto> users = List.of(new UserDto(UUID.randomUUID().toString(), "username", "firstName", "lastName", "email@test.com"));

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        when(restHelper.get("infoService", "/case/" + caseUUID + "/stage/" + stageUUID + "/team/members", new ParameterizedTypeReference<List<UserDto>>() {
        })).thenReturn(users);

        List<UserDto> response = infoClient.getUsersForTeamByStage(caseUUID, stageUUID);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(users);

        verify(restHelper).get("infoService", "/case/" + caseUUID + "/stage/" + stageUUID + "/team/members", new ParameterizedTypeReference<List<UserDto>>() {
        });
        verifyNoMoreInteractions(restHelper);
    }


    @Test
    public void getCaseTypeActionByUuid() {

        // GIVEN
        UUID actionTypeUuid = UUID.randomUUID();
        String caseDataType = "CT1";

        CaseTypeActionDto mockCaseTypeActionDto =
                new CaseTypeActionDto(actionTypeUuid,
                        UUID.randomUUID(),
                        caseDataType,
                        null,
                        null,
                        null,
                        1,
                        10,
                        true,
                        null);
        when(restHelper.get("infoService", "/caseType/" + caseDataType + "/actions/" + actionTypeUuid, CaseTypeActionDto.class)).thenReturn(mockCaseTypeActionDto);

        // WHEN
        CaseTypeActionDto response = infoClient.getCaseTypeActionByUuid(caseDataType, actionTypeUuid);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(mockCaseTypeActionDto);
    }

    @Test
    public void getCaseTypeActionForCaseType_shouldReturnListOfCaseTypeActionDtos() {

        // GIVEN
        String caseType = "CT1";
        ParameterizedTypeReference<List<CaseTypeActionDto>> typeRef = new ParameterizedTypeReference<>() {};

        when(restHelper.get("infoService", "/caseType/" + caseType + "/actions", typeRef)).thenReturn(List.of());

        // WHEN
        List<CaseTypeActionDto> response = infoClient.getCaseTypeActionForCaseType(caseType);

        // THEN
        assertThat(response).isNotNull();
    }
}
