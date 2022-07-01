package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.AdditionalFieldDto;
import uk.gov.digital.ho.hocs.casework.domain.repository.RestrictedFieldRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.util.List;
import java.util.Map;

@Service
public class RestrictedFieldService {

    private final RestrictedFieldRepository restrictedFieldRepository;

    public RestrictedFieldService(RestrictedFieldRepository restrictedFieldRepository) {
        this.restrictedFieldRepository = restrictedFieldRepository;
    }

    public void removeRestrictedFieldsFromCaseData(String type, AccessLevel userAccessLevel, Map<String, String> caseData) {
        restrictedFieldRepository.getByCaseTypeAndPermissionLevelGreaterThanEqual(type, userAccessLevel)
                        .forEach(caseData.keySet()::remove);
    }

    public void removeRestrictedFieldsFromAdditionalFields
            (String type, AccessLevel userAccessLevel, List<AdditionalFieldDto> additionalFields) {
        restrictedFieldRepository.getByCaseTypeAndPermissionLevelGreaterThanEqual(type, userAccessLevel)
                .forEach(field ->
                        additionalFields.removeIf(additionalFieldDto -> field.equals(additionalFieldDto.getName())));
    }
}
