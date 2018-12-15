package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
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
    private UUID primaryTopic;

    @JsonProperty("primaryCorrespondent")
    private UUID primaryCorrespondent;

    @JsonProperty("caseDeadline")
    private LocalDate caseDeadline;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    public static CaseDataDto from(CaseData caseData) {

        return new CaseDataDto(
                caseData.getUuid(),
                caseData.getCreated(),
                caseData.getType(),
                caseData.getReference(),
                caseData.getData(),
                caseData.getPrimaryTopicUUID(),
                caseData.getPrimaryCorrespondentUUID(),
                caseData.getCaseDeadline(),
                caseData.getDateReceived());
    }
}
