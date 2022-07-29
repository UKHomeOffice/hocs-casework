package uk.gov.digital.ho.hocs.casework.domain.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.RestrictedFieldPermissions;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RestrictedFieldRepository extends JsonConfigFileReader {

    private final RestrictedFieldPermissions restrictedFieldPermissions;

    public RestrictedFieldRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        restrictedFieldPermissions = readValueFromFile(new TypeReference<>() {});
    }

    @Override
    String getFileName() {
        return "restricted-fields";
    }

    public Set<String> getByCaseTypeAndPermissionLevelGreaterThanEqual(String type, AccessLevel userAccessLevel) {
        var fieldsForAccessLevel = restrictedFieldPermissions.getFieldsForAccessLevel(type);
        if (fieldsForAccessLevel == null) {
            return Set.of();
        }

        return Arrays.stream(AccessLevel.values())
                .filter(accessLevel -> accessLevel.getLevel() >= userAccessLevel.getLevel())
                .flatMap(accessLevel -> fieldsForAccessLevel.getFieldsForAccessLevel(accessLevel).stream())
                .collect(Collectors.toUnmodifiableSet());
    }

}
