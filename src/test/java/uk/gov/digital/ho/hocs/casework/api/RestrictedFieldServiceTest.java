package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.AdditionalFieldDto;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class RestrictedFieldServiceTest {

    @Autowired
    RestrictedFieldService restrictedFieldService;

    @Test
    public void caseData_shouldNotRemoveCaseDataIfCaseTypeNotFound() {
        Map<String, String> caseData = Map.of("Field1", "Test");

        restrictedFieldService
                .removeRestrictedFieldsFromCaseData("UNKNOWN", AccessLevel.RESTRICTED_OWNER, caseData);

        assertThat(caseData).hasSize(1);
    }

    @Test
    public void caseData_shouldNotRemoveCaseDataIfAccessLevelHigherThanRestricted() {
        Map<String, String> caseData = Map.of("Field1", "Test");

        restrictedFieldService
                .removeRestrictedFieldsFromCaseData("TEST", AccessLevel.OWNER, caseData);

        assertThat(caseData).hasSize(1);
    }

    @Test
    public void caseData_shouldOnlyRemoveSingleValueWithAccessLevelSummary() {
        Map<String, String> caseData = new HashMap<>(
                Map.of("Field1", "Test1", "Field2", "Test2")
        );

        restrictedFieldService
                .removeRestrictedFieldsFromCaseData("TEST", AccessLevel.SUMMARY, caseData);

        assertThat(caseData)
                .hasSize(1)
                .containsEntry("Field2", "Test2");
    }

    @Test
    public void caseData_shouldOnlyRemoveValuesAcrossAccessLevels() {
        Map<String, String> caseData = new HashMap<>(
                Map.of("Field1", "Test1", "Field2", "Test2")
        );

        restrictedFieldService
                .removeRestrictedFieldsFromCaseData("TEST", AccessLevel.RESTRICTED_OWNER, caseData);

        assertThat(caseData)
                .isEmpty();
    }

    @Test
    public void additionalFields_shouldNotRemoveCaseDataIfCaseTypeNotFound() {
        List<AdditionalFieldDto> additionalFields = List.of(
                AdditionalFieldDto.from(new AdditionalField("", "Test1", "", "", "Field1"))
        );

        restrictedFieldService
                .removeRestrictedFieldsFromAdditionalFields("UNKNOWN", AccessLevel.RESTRICTED_OWNER, additionalFields);

        assertThat(additionalFields).hasSize(1);
    }

    @Test
    public void additionalFields_shouldNotRemoveCaseDataIfAccessLevelHigherThanRestricted() {
        List<AdditionalFieldDto> additionalFields = List.of(
                AdditionalFieldDto.from(new AdditionalField("", "Test1", "", "", "Field1"))
        );

        restrictedFieldService
                .removeRestrictedFieldsFromAdditionalFields("TEST", AccessLevel.OWNER, additionalFields);

        assertThat(additionalFields).hasSize(1);
    }

    @Test
    public void additionalFields_shouldOnlyRemoveSingleValueWithAccessLevelSummary() {
        var field2 = AdditionalFieldDto.from(new AdditionalField("", "Test2", "", "", "Field2"));

        List<AdditionalFieldDto> additionalFields = new ArrayList<>(
                List.of(
                        AdditionalFieldDto.from(new AdditionalField("", "Test1", "", "", "Field1")),
                        field2
                )
        );

        restrictedFieldService
                .removeRestrictedFieldsFromAdditionalFields("TEST", AccessLevel.SUMMARY, additionalFields);

        assertThat(additionalFields)
                .hasSize(1)
                .contains(field2);
    }


    @Test
    public void additionalFields_shouldOnlyRemoveValuesAcrossAccessLevels() {
        List<AdditionalFieldDto> additionalFields = new ArrayList<>(
                List.of(
                        AdditionalFieldDto.from(new AdditionalField("", "Test1", "", "", "Field1")),
                        AdditionalFieldDto.from(new AdditionalField("", "Test2", "", "", "Field2"))
                )
        );

        restrictedFieldService
                .removeRestrictedFieldsFromAdditionalFields("TEST", AccessLevel.RESTRICTED_OWNER, additionalFields);

        assertThat(additionalFields)
                .isEmpty();
    }

}
