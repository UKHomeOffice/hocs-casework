package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class DcuMinCaseSummaryAdditionalFieldProviderTest {

    public DcuMinCaseSummaryAdditionalFieldProvider dcuMinCaseSummaryAdditionalFieldProvider;
    Set<FieldDto> summaryFields;
    Map<String, String> dropdownChoices;
    Map<String, String> teamChoices;
    UUID overrideTeamUuid = UUID.randomUUID();
    UUID privateOfficeOverridePOTeamUUID = UUID.randomUUID();

    @Before
    public void setup() {
        dcuMinCaseSummaryAdditionalFieldProvider = new DcuMinCaseSummaryAdditionalFieldProvider();
        dropdownChoices = Map.of("Y", "Yes", "N", "No");
        teamChoices = Map.of(overrideTeamUuid.toString(), "test team");

        summaryFields= new HashSet<>();

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

        summaryFields.add(new FieldDto(
                        UUID.randomUUID(),
                        "OverridePOTeamUUID",
                        "PO Team Test",
                        "text-area",
                        null,
                        true,
                        true,
                        Map.of("choices", teamChoices)
                )
        );

    }

    @Test
    public void dcuMinFieldProviderProvidesCorrectFields() {
        // Given
        final Map<String, String> caseDataMap = Map.of(
                "DATE", "2021-11-26", // should be excluded from result
                "RADIO", "Y",
                "TEXT_AREA", "some test text"
        );

        // When
        final Set<AdditionalField> additionalFieldsForSummary = dcuMinCaseSummaryAdditionalFieldProvider
                .getAdditionalFieldsForSummary(summaryFields, caseDataMap);

        // Then
        assertThat(additionalFieldsForSummary).isNotNull();
        assertThat(additionalFieldsForSummary).isNotEmpty();
        assertThat(additionalFieldsForSummary.size()).isEqualTo(3);

        // choices are extracted
        final Optional<AdditionalField> radioField =
                additionalFieldsForSummary.stream().filter(field -> field.getType().equals("radio")).findFirst();

        assertThat(radioField.get().getChoices()).isNotNull();

        // override po team field should be empty
        final Optional<AdditionalField> overrideField =
                additionalFieldsForSummary.stream().filter(field -> field.getLabel().equals("PO Team Test"))
                        .findFirst();

        assertThat(overrideField.get().getValue()).isEqualTo("");
    }

    @Test
    public void dcuMinFieldProviderProvidesCorrectFields_override_at_draft() {
        // Given
        final Map<String, String> caseDataMap = Map.of(
                "DATE", "2021-11-26", // should be excluded from result
                "RADIO", "Y",
                "OverridePOTeamUUID", overrideTeamUuid.toString(),
                "TEXT_AREA", "some test text"
        );

        // When
        final Set<AdditionalField> additionalFieldsForSummary = dcuMinCaseSummaryAdditionalFieldProvider
                .getAdditionalFieldsForSummary(summaryFields, caseDataMap);

        // Then
        assertThat(additionalFieldsForSummary).isNotNull();
        assertThat(additionalFieldsForSummary).isNotEmpty();
        assertThat(additionalFieldsForSummary.size()).isEqualTo(3);

        // choices are extracted
        final Optional<AdditionalField> radioField =
                additionalFieldsForSummary.stream().filter(field -> field.getType().equals("radio")).findFirst();

        assertThat(radioField.get().getChoices()).isNotNull();
        assertThat(radioField.get().getChoices()).isEqualTo(dropdownChoices);

        // override po team field should have team uuid
        final Optional<AdditionalField> overrideField =
                additionalFieldsForSummary.stream().filter(field -> field.getLabel().equals("PO Team Test"))
                        .findFirst();

        assertThat(overrideField.get().getValue()).isEqualTo(overrideTeamUuid.toString());
    }

    @Test
    public void dcuMinFieldProviderProvidesCorrectFields_override_at_po() {
        // Given
        final Map<String, String> caseDataMap = Map.of(
                "DATE", "2021-11-26", // should be excluded from result
                "RADIO", "Y",
                "PrivateOfficeOverridePOTeamUUID", privateOfficeOverridePOTeamUUID.toString(),
                "TEXT_AREA", "some test text"
        );

        // When
        final Set<AdditionalField> additionalFieldsForSummary = dcuMinCaseSummaryAdditionalFieldProvider
                .getAdditionalFieldsForSummary(summaryFields, caseDataMap);

        // Then
        assertThat(additionalFieldsForSummary).isNotNull();
        assertThat(additionalFieldsForSummary).isNotEmpty();
        assertThat(additionalFieldsForSummary.size()).isEqualTo(3);

        // choices are extracted
        final Optional<AdditionalField> radioField =
                additionalFieldsForSummary.stream().filter(field -> field.getType().equals("radio")).findFirst();

        assertThat(radioField.get().getChoices()).isNotNull();
        assertThat(radioField.get().getChoices()).isEqualTo(dropdownChoices);

        // override po team field should have team uuid
        final Optional<AdditionalField> overrideField =
                additionalFieldsForSummary.stream().filter(field -> field.getLabel().equals("PO Team Test"))
                        .findFirst();

        assertThat(overrideField.get().getValue()).isEqualTo(privateOfficeOverridePOTeamUUID.toString());
    }

    @Test
    public void dcuMinFieldProviderProvidesCorrectFields_override_at_draft_and_po() {
        // Given
        final Map<String, String> caseDataMap = Map.of(
                "DATE", "2021-11-26", // should be excluded from result
                "RADIO", "Y",
                "OverridePOTeamUUID", overrideTeamUuid.toString(),
                "PrivateOfficeOverridePOTeamUUID", privateOfficeOverridePOTeamUUID.toString(),
                "TEXT_AREA", "some test text"
        );

        // When
        final Set<AdditionalField> additionalFieldsForSummary = dcuMinCaseSummaryAdditionalFieldProvider
                .getAdditionalFieldsForSummary(summaryFields, caseDataMap);

        // Then
        assertThat(additionalFieldsForSummary).isNotNull();
        assertThat(additionalFieldsForSummary).isNotEmpty();
        assertThat(additionalFieldsForSummary.size()).isEqualTo(3);

        // choices are extracted
        final Optional<AdditionalField> radioField =
                additionalFieldsForSummary.stream().filter(field -> field.getType().equals("radio")).findFirst();

        assertThat(radioField.get().getChoices()).isNotNull();
        assertThat(radioField.get().getChoices()).isEqualTo(dropdownChoices);

        // override po team field should have team uuid
        final Optional<AdditionalField> overrideField =
                additionalFieldsForSummary.stream().filter(field -> field.getLabel().equals("PO Team Test"))
                        .findFirst();

        assertThat(overrideField.get().getValue()).isEqualTo(privateOfficeOverridePOTeamUUID.toString());
    }
}