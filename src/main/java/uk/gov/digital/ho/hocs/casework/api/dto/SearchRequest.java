package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SearchRequest {

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("caseType")
    @JsonSetter
    private void caseTypeDeserialize(String caseType) {
        this.caseTypes = Arrays.asList(caseType.split(","));
    }

    private List<String> caseTypes;

    @JsonGetter
    public List<String> getCaseType() { return caseTypes; }

    @JsonProperty("dateReceived")
    private DateRangeDto dateReceived;

    @JsonProperty("correspondentName")
    private String correspondentName;

    @JsonProperty("correspondentNameNotMember")
    private String correspondentNameNotMember;

    @JsonProperty("correspondentReference")
    private String correspondentReference;

    @JsonProperty("correspondentExternalKey")
    private String correspondentExternalKey;

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("poTeamUuid")
    private String poTeamUuid;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("activeOnly")
    private Boolean activeOnly;

}
