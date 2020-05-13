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
    public void getCorrespondentType(){

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
    public void getTeamByStageAndText(){

        TeamDto teamDto = new TeamDto();
        when(restHelper.get("infoService", "/team/stage/stageType/text/text", TeamDto.class)).thenReturn(teamDto);

        TeamDto result = infoClient.getTeamByStageAndText("stageType", "text");

        assertThat(result).isEqualTo(teamDto);

        verify(restHelper).get("infoService", "/team/stage/stageType/text/text", TeamDto.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getEntityListTotalsReturnsEntityList(){
        EntityTotalDto entityTotalDto = new EntityTotalDto(new HashMap(), new HashMap());
        EntityDto<EntityTotalDto> entityDto = new EntityDto<EntityTotalDto>("simpleName", entityTotalDto);
        List<EntityDto<EntityTotalDto>> entityListTotals = new ArrayList();
        entityListTotals.add(entityDto);
        when(restHelper.get("infoService", "/entity/list/listName", new ParameterizedTypeReference<List<EntityDto<EntityTotalDto>>>() {})).thenReturn(entityListTotals);

        List<EntityDto<EntityTotalDto>> response = infoClient.getEntityListTotals("listName");

        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        verify(restHelper).get("infoService", "/entity/list/listName", new ParameterizedTypeReference<List<EntityDto<EntityTotalDto>>>() {});
        verifyNoMoreInteractions(restHelper);
    }


    @Test
    public void getPriorityPoliciesForCaseType(){
        String policyType = "POLICYB";
        String caseType = "CASE_TYPE_A";
        Map<String, String> config = Map.of("propertyB", "valueB");
        List<PriorityPolicyDto> priorityPolicyDtos = Collections.singletonList(new PriorityPolicyDto(policyType, caseType, config));
        when(restHelper.get("infoService", "/priority/policy/" + caseType, new ParameterizedTypeReference<List<PriorityPolicyDto>>() {})).thenReturn(priorityPolicyDtos);


        List<PriorityPolicyDto> results = infoClient.getPriorityPoliciesForCaseType(caseType);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(priorityPolicyDtos.size());
        assertThat(results.get(0).getPolicyType()).isEqualTo(policyType);
        assertThat(results.get(0).getCaseType()).isEqualTo(caseType);
        assertThat(results.get(0).getConfig()).isEqualTo(config);

        verify(restHelper).get("infoService", "/priority/policy/" + caseType, new ParameterizedTypeReference<List<PriorityPolicyDto>>() {});
        verifyNoMoreInteractions(restHelper);

    }
}
