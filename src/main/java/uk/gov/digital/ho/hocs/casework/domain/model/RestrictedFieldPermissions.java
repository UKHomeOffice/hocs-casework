package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.util.Map;
import java.util.Set;

public class RestrictedFieldPermissions {

    @JsonValue
    public Map<String, FieldAccessLevels> caseFieldAccessLevels;

    @JsonCreator
    public RestrictedFieldPermissions(Map<String, FieldAccessLevels> caseFieldAccessLevels) {
        this.caseFieldAccessLevels = caseFieldAccessLevels;
    }

    public FieldAccessLevels getFieldsForAccessLevel(String caseType) {
        return caseFieldAccessLevels.get(caseType);
    }

    public static class FieldAccessLevels {

        @JsonValue
        private final Map<AccessLevel, Set<String>> fieldLevels;

        @JsonCreator
        public FieldAccessLevels(Map<AccessLevel, Set<String>> fieldLevels) {
            this.fieldLevels = fieldLevels;
        }

        public Set<String> getFieldsForAccessLevel(AccessLevel accessLevel) {
            return fieldLevels.getOrDefault(accessLevel, Set.of());
        }

    }

}
