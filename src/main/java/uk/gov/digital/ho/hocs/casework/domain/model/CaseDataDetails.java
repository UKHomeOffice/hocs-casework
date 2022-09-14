package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.repository.JsonConfigFolderReader.CaseTypeObject;

import java.util.List;
import java.util.Map;

public class CaseDataDetails implements CaseTypeObject<Map<String, List<CaseDataDetails.Fields>>> {

    private final String type;
    private final Map<String, List<Fields>> groups;

    @JsonCreator
    public CaseDataDetails(@JsonProperty("type") String type,
                           @JsonProperty("groups") Map<String, List<Fields>> fields) {
        this.type = type;
        this.groups = fields;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, List<Fields>> getValue() {
        return groups;
    }

    @Getter
    public static class Fields {

        private final String name;
        private final String label;
        private final String component;
        private final Map<String, Object> props;

        @JsonCreator
        public Fields(@JsonProperty("name") String name,
                      @JsonProperty("label") String label,
                      @JsonProperty("component") String component,
                      @JsonProperty("props") Map<String, Object> props) {
            this.name = name;
            this.label = label;
            this.component = component;
            this.props = props;
        }

    }
}
