package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseSummaryAdditionalFieldProvider;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultCaseSummaryAdditionalFieldProvider implements CaseSummaryAdditionalFieldProvider {
    public Set<AdditionalField> getAdditionalFieldsForSummary(
            Set<FieldDto> summaryFields,
            Map<String, String> caseDataMap) {
        Set<AdditionalField> additionalFields = summaryFields.stream()
                .map(field ->
                        new AdditionalField(field.getLabel(), caseDataMap.getOrDefault(field.getName(), "")
                                , field.getComponent(), extractChoices(field)))
                .collect(Collectors.toSet());
        return additionalFields;
    }

    private Object extractChoices(FieldDto fieldDto) {
        if (fieldDto != null && fieldDto.getProps() != null && fieldDto.getProps() instanceof Map) {
            Map propMap = (Map) fieldDto.getProps();
            return propMap.get("choices");
        }

        return null;
    }
}
