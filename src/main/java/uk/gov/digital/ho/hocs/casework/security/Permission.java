package uk.gov.digital.ho.hocs.casework.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Permission {

    @JsonCreator
    public Permission(@JsonProperty("caseTypeCode") String caseTypeCode, @JsonProperty("accessLevel") AccessLevel accessLevel) {
        this.caseTypeCode = caseTypeCode;
        this.accessLevel = accessLevel;
    }
    @JsonProperty("caseTypeCode")
    private String caseTypeCode;

    @JsonProperty("accessLevel")
    private AccessLevel accessLevel;

}
