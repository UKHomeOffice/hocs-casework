package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

@RunWith(SpringRunner.class)
public class DefaultCaseSummaryAdditionalFieldProviderTest {

    public DefaultCaseSummaryAdditionalFieldProvider defaultCaseSummaryAdditionalFieldProvider;

    @Before
    public void setup() {
        defaultCaseSummaryAdditionalFieldProvider = new DefaultCaseSummaryAdditionalFieldProvider();
    }

    @Test
    public void defaultFieldProviderProvidesCorrectFields() {
        // Given
        Set<FieldDto> summaryFields = new HashSet<>();

        final Map<String, String> dropdownChoices = Map.of("Y", "Yes", "N", "No");
        summaryFields.add(new FieldDto(
                        UUID.randomUUID(),
                        "RADIO",
                        "Additional radio",
                        "radio",
                        null,
                        true,
                        true,
                        Map.of("choices", dropdownChoices)
                )
        );

        summaryFields.add(new FieldDto(
                        UUID.randomUUID(),
                        "TEXT_AREA",
                        "Additional test",
                        "text-area",
                        null,
                        true,
                        true,
                        Map.of()
                )
        );

        Map<String, String> caseDataMap = Map.of(
                "DATE", "2021-11-26", // should be excluded from result
                "RADIO", "Y",
                "TEXT_AREA", "some test text"
        );

        // When
        final Set<AdditionalField> additionalFieldsForSummary = defaultCaseSummaryAdditionalFieldProvider
                .getAdditionalFieldsForSummary(summaryFields, caseDataMap);

        // Then
        assertThat(additionalFieldsForSummary).isNotNull();
        assertThat(additionalFieldsForSummary).isNotEmpty();
        assertThat(additionalFieldsForSummary.size()).isEqualTo(2);

        //choices are extracted
        final Optional<AdditionalField> radioField =
                additionalFieldsForSummary.stream().filter(field -> field.getType().equals("radio")).findFirst();

        assertThat(radioField.get().getChoices()).isNotNull();
        assertThat(radioField.get().getChoices()).isEqualTo(dropdownChoices);

    }
}
