package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class UpdateTeamByStageAndTextsResponse {

    @JsonProperty("teamMap")
    private Map<String, String> teamMap;

    public static UpdateTeamByStageAndTextsResponse from(Map<String, String> teamMap) {
        return new UpdateTeamByStageAndTextsResponse(teamMap);
    }

}
