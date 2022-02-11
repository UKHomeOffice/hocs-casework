package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static uk.gov.digital.ho.hocs.casework.api.CaseDataService.CASE_UUID_PATTERN;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GetCaseResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss Z", timezone = "UTC")
    private ZonedDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("primaryTopicUUID")
    private UUID primaryTopicUUID;

    @JsonProperty("primaryTopic")
    private GetTopicResponse primaryTopic;

    @JsonProperty("primaryCorrespondentUUID")
    private UUID primaryCorrespondentUUID;

    @JsonProperty("primaryCorrespondent")
    private GetCorrespondentResponse primaryCorrespondent;

    @JsonProperty("caseDeadline")
    private LocalDate caseDeadline;

    @JsonProperty("caseDeadlineWarning")
    private LocalDate caseDeadlineWarning;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("stages")
    private List<SimpleStageDto> stages;

    @JsonProperty("completed")
    private Boolean completed;

    public static GetCaseResponse from(CaseData caseData, boolean full) {

        List<SimpleStageDto> stages = new ArrayList<>();
        if (full && !CollectionUtils.isEmpty(caseData.getActiveStages())) {
            for (ActiveStage activeStage : caseData.getActiveStages()) {
                stages.add(SimpleStageDto.from(activeStage));
            }
        }

        return new GetCaseResponse(
                caseData.getUuid(),
                ZonedDateTime.of(caseData.getCreated(), ZoneOffset.UTC),
                caseData.getType(),
                caseData.getReference(),
                populateFields(caseData, full),
                caseData.getPrimaryTopicUUID(),
                populateTopic(caseData.getPrimaryTopic(), full),
                caseData.getPrimaryCorrespondentUUID(),
                populateCorrespondent(caseData.getPrimaryCorrespondent(), full),
                caseData.getCaseDeadline(),
                caseData.getCaseDeadlineWarning(),
                caseData.getDateReceived(),
                stages,
                caseData.isCompleted());
    }

    private static GetTopicResponse populateTopic(Topic topic, boolean full) {
        if (topic != null && full) {
            return GetTopicResponse.from(topic);
        }
        return null;
    }

    private static GetCorrespondentResponse populateCorrespondent(Correspondent correspondent, boolean full) {
        if (correspondent != null && full) {
            return GetCorrespondentResponse.from(correspondent);
        }
        return null;
    }

    private static Map<String,String> populateFields(CaseData caseData, boolean full) {
        if (full) {
            Map<String, String> replacementValues = new HashMap<>();

            if (caseData.getPrimaryTopic() != null) {
                replacementValues.put(caseData.getPrimaryTopic().getUuid().toString(), caseData.getPrimaryTopic().getText());
            }
            if (caseData.getPrimaryCorrespondent() != null) {
                replacementValues.put(caseData.getPrimaryCorrespondent().getUuid().toString(), caseData.getPrimaryCorrespondent().getFullName());
            }

            final Map<String, String> dataMap = caseData.getDataMap();
            for (String s : dataMap.keySet()) {
                if (CASE_UUID_PATTERN.matcher(s).matches()) {
                    replacementValues.put(s, dataMap.get(s));
                }
            }
            for (Map.Entry<String, String> entry : replacementValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                dataMap.entrySet().stream().filter(it -> it.getValue().equals(key)).forEach(it -> it.setValue(value));
            }

            return dataMap;
        }
        return caseData.getDataMap();
    }

    protected void replaceDataMap(Map<String, String> dataMap) {
        this.data = dataMap;
    }
}
