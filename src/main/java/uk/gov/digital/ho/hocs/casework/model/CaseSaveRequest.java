package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CaseSaveRequest {

    @JsonProperty("requestUUID")
    private String requestUUID;

    @JsonProperty("requestTimestamp")
    private String requestTimestamp;

    @JsonProperty("caseType")
    private String caseType;

    @JsonProperty("notifyDetails")
    private NotifyDetails notifyDetails;

    @JsonProperty("stageDetails")
    private StageDetails stageDetails;

    public static CaseSaveRequest from(UUID requestUUID, LocalDateTime requestTimestamp, String caseType, String stageName, RshCaseSaveRequest rshCaseSaveRequest){
        NotifyDetails notifyDetails = NotifyDetails.from(rshCaseSaveRequest.getNotifyEmail(), rshCaseSaveRequest.getNotifyTeamName());
        StageDetails stageDetails = StageDetails.from(stageName, rshCaseSaveRequest.getCaseData());
        return new CaseSaveRequest(requestUUID.toString(), requestTimestamp.toString(), caseType, notifyDetails, stageDetails);
    }
}
