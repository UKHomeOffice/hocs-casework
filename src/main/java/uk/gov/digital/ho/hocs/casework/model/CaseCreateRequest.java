package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.rsh.StageDetails;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CaseCreateRequest {

    @JsonProperty("requestUUID")
    private String requestUUID;

    @JsonProperty("requestTimestamp")
    private String requestTimestamp;

    @JsonProperty("caseType")
    private String caseType;
}
