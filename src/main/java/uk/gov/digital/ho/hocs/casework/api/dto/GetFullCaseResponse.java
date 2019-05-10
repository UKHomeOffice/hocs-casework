package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetFullCaseResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss Z", timezone = "UTC")
    private ZonedDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("reference")
    private String reference;

    @JsonRawValue
    private String data;

    @JsonProperty("caseDeadline")
    private LocalDate caseDeadline;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("primaryTopic")
    private GetTopicResponse primaryTopic;

    @JsonProperty("primaryCorrespondent")
    private GetCorrespondentResponse primaryCorrespondent;



    public static GetFullCaseResponse from(CaseData caseData) {
        GetCorrespondentResponse getCorrespondentResponse = null;
        if (caseData.getPrimaryCorrespondent() != null) {

            getCorrespondentResponse = GetCorrespondentResponse.from(caseData.getPrimaryCorrespondent());
        }

        GetTopicResponse getTopicsResponse = null;
        if (caseData.getPrimaryTopic() != null) {
            getTopicsResponse = GetTopicResponse.from(caseData.getPrimaryTopic());
        }

        return new GetFullCaseResponse(
                caseData.getUuid(),
                ZonedDateTime.of(caseData.getCreated(), ZoneOffset.UTC),
                caseData.getType(),
                caseData.getReference(),
                populateFields(caseData),
                caseData.getCaseDeadline(),
                caseData.getDateReceived(),
                getTopicsResponse,
                getCorrespondentResponse);
    }

    private static String populateFields(CaseData caseData) {
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();

        if(caseData.getPrimaryTopic() != null) {
            keys.add(caseData.getPrimaryTopic().getUuid().toString());
            values.add(caseData.getPrimaryTopic().getText());
        }
        if(caseData.getPrimaryCorrespondent() != null) {
            keys.add(caseData.getPrimaryCorrespondent().getUuid().toString());
            values.add(caseData.getPrimaryCorrespondent().getFullName());
        }

        return StringUtils.replaceEach(caseData.getData(),
                keys.toArray(new String[0]),
                values.toArray(new String[0]));
    }


}
