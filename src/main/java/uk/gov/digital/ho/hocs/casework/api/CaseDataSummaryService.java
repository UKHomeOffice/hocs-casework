package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.stereotype.Service;

import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;

import uk.gov.digital.ho.hocs.casework.domain.model.SummaryFields.SummaryField;
import uk.gov.digital.ho.hocs.casework.domain.model.SummaryFields.SummaryField.ConditionChoices;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseSummaryRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CaseDataSummaryService {

    private final CaseSummaryRepository caseSummaryRepository;

    public CaseDataSummaryService(CaseSummaryRepository caseSummaryRepository) {
        this.caseSummaryRepository = caseSummaryRepository;
    }

    public Set<AdditionalField> getAdditionalCaseDataFieldsByCaseType(String caseType,
                                                                      Map<String, String> caseDataItems) {
        var fields = caseSummaryRepository.getSummaryFieldsByCaseType(caseType);

        return fields.stream().filter(field -> caseDataItems.containsKey(field.getName())).map(
            field -> toAdditionalField(field, caseDataItems)).collect(Collectors.toSet());
    }

    private AdditionalField toAdditionalField(SummaryField field, Map<String, String> caseDataItems) {
        return new AdditionalField(field.getLabel(), caseDataItems.get(field.getName()), field.getType(),
            reducesChoices(field.getConditionChoices(), field.getChoices(), caseDataItems), field.getName());
    }

    private Object reducesChoices(List<ConditionChoices> conditionChoices,
                                  Object choices,
                                  Map<String, String> caseDataItems) {
        if (conditionChoices!=null) {
            var foundChoices = conditionChoices.stream().filter(
                choice -> Objects.equals(caseDataItems.get(choice.getConditionPropertyName()),
                    choice.getConditionPropertyValue())).findFirst().orElse(null);

            return foundChoices==null ? null:foundChoices.getChoices();
        }

        return choices;
    }

}
