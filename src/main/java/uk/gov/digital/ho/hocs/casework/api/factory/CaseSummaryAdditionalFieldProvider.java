package uk.gov.digital.ho.hocs.casework.api.factory;

import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;

import java.util.Map;
import java.util.Set;

public interface CaseSummaryAdditionalFieldProvider {
    Set<AdditionalField> getAdditionalFieldsForSummary(Set<FieldDto> summaryFields, Map<String, String> caseDataMap);
}
