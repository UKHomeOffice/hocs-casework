package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @JsonRawValue
    private String data;

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

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    public static GetCaseResponse from(CaseData caseData, boolean full) {

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
                caseData.getDateReceived());
    }

    private static GetTopicResponse populateTopic(Topic topic, boolean full) {
        if(topic != null && full) {
            return GetTopicResponse.from(topic);
        }
        return null;
    }

    private static GetCorrespondentResponse populateCorrespondent(Correspondent correspondent, boolean full){
        if (correspondent != null && full) {
            return GetCorrespondentResponse.from(correspondent);
        }
        return null;
    }

    private static Pattern uuidPattern = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b", Pattern.CASE_INSENSITIVE);

    private static String populateFields(CaseData caseData, boolean full) {
        if(full) {
            List<String> keys = new ArrayList<>(2);
            List<String> values = new ArrayList<>(2);

            if (caseData.getPrimaryTopic() != null) {
                keys.add(caseData.getPrimaryTopic().getUuid().toString());
                values.add(caseData.getPrimaryTopic().getText());
            }
            if (caseData.getPrimaryCorrespondent() != null) {
                keys.add(caseData.getPrimaryCorrespondent().getUuid().toString());
                values.add(caseData.getPrimaryCorrespondent().getFullName());
            }
            // substitute the UUID key values into the value UUIDs
            final Map<String, String> dataMap = caseData.getDataMap(new ObjectMapper());
            final Set<String> dataKeys = new LinkedHashSet<>(dataMap.keySet());
            final Collection<String> dataValues = new LinkedList<>(dataMap.values());
            dataKeys.retainAll(dataValues);
            for (String uuid : dataKeys) {
                Matcher uuidMatcher = uuidPattern.matcher(uuid);
                if (uuidMatcher.matches()) {
                    keys.add(uuid);
                    values.add(dataMap.get(uuid));
                }
            }
            return StringUtils.replaceEach(caseData.getData(),
                    keys.toArray(new String[0]),
                    values.toArray(new String[0]));
        }
        return caseData.getData();
    }


}
