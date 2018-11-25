package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CaseDataDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("reference")
    private String reference;

    @JsonRawValue
    private String data;

    @JsonProperty("primaryTopic")
    private String primaryTopic;

    @JsonProperty("primaryCorrespondent")
    private String primaryCorrespondent;

    public static CaseDataDto from(CaseData caseData) {

        String primaryCorrespondent = null;
        if (caseData.getPrimaryCorrespondentUUID() != null) {
            primaryCorrespondent = caseData.getPrimaryCorrespondentUUID().toString();
        }

        String primaryTopic = null;
        if (caseData.getPrimaryTopicUUID() != null) {
            primaryTopic = caseData.getPrimaryTopicUUID().toString();
        }

        return new CaseDataDto(
                caseData.getUuid(),
                caseData.getCreated(),
                caseData.getCaseDataType().toString(),
                caseData.getReference(),
                caseData.getData(),
                primaryTopic,
                primaryCorrespondent);
    }
}
