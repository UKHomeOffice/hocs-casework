package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetStageResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("active")
    private boolean active;

    public static GetStageResponse from(Stage stage) {
        return new GetStageResponse(stage.getUuid(), stage.getType(), stage.getCaseReference(), stage.isActive());
    }
}