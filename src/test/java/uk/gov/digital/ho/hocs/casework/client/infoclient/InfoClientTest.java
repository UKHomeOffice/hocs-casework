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
import static org.mockito.Mockito.when;

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
    }

    @Test
    public void getTeamByStageAndText(){

        TeamDto teamDto = new TeamDto();
        when(restHelper.get("infoService", "/team/stage/stageType/text/text", TeamDto.class)).thenReturn(teamDto);

        TeamDto result = infoClient.getTeamByStageAndText("stageType", "text");

        assertThat(result).isEqualTo(teamDto);
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
    }
}
