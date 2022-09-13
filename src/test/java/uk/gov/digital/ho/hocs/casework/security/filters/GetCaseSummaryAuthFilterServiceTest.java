package uk.gov.digital.ho.hocs.casework.security.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.RestrictedFieldService;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.repository.RestrictedFieldRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseSummaryAuthFilterServiceTest {

    @Mock
    private RestrictedFieldRepository restrictedFieldRepository;

    private GetCaseSummaryAuthFilterService getCaseSummaryAuthFilterService;

    @Before
    public void setUp() {
        when(restrictedFieldRepository.getByCaseTypeAndPermissionLevelGreaterThanEqual(any(),
            eq(AccessLevel.RESTRICTED_OWNER))).thenReturn(Set.of("Data3", "Data4"));

        var restrictedFieldService = new RestrictedFieldService(restrictedFieldRepository);

        getCaseSummaryAuthFilterService = new GetCaseSummaryAuthFilterService(restrictedFieldService);
    }

    @Test(expected = SecurityExceptions.AuthFilterException.class)
    public void testShouldThrowExceptionIfFilterNotForObjectType() {

        // GIVEN

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        String caseType = "CASE_TYPE";
        CaseDataType type = new CaseDataType(null, "A", caseType, null, 1, 1);
        long caseNumber = 1L;
        Map<String, String> caseDataMap = new HashMap<>();
        caseDataMap.put("Data1", "Data 1");
        caseDataMap.put("Data2", "Data 2");
        caseDataMap.put("Data3", "Data 3");
        caseDataMap.put("Data4", "Data 4");

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
            GetCaseResponse.from(new CaseData(type, caseNumber, caseDataMap, null), true));

        // WHEN
        getCaseSummaryAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN - Expect Exception
    }

    @Test
    public void testShouldReturnEmptyAdditionalFieldDtoListWhenNoPermittedFields() {

        // GIVEN
        String caseType = "CASE_TYPE";

        AdditionalField additionalField1 = new AdditionalField("Field 1", "ValField1", null, null, "field1");
        AdditionalField additionalField2 = new AdditionalField("Field 2", "ValField2", null, null, "field2");
        AdditionalField additionalField3 = new AdditionalField("Field 3", "ValField3", null, null, "field3");
        AdditionalField additionalField4 = new AdditionalField("Field 4", "ValField4", null, null, "field4");

        Set<AdditionalField> additionalFields = Set.of(additionalField1, additionalField2, additionalField3,
            additionalField4);

        Map<String, List<ActionDataDto>> caseActionDataMap = new HashMap<>();

        ActionDataAppealDto appealDto = new ActionDataAppealDto(null, null, "TEST_APPEAL", "ACTION_LABEL", null,
            LocalDate.MAX, null, null, "TEST NOTE - This appeal should be removed by case summary auth filter", "{}",
            null);

        caseActionDataMap.put("appeals", new ArrayList(List.of(appealDto)));

        CaseActionDataResponseDto caseActionData = CaseActionDataResponseDto.from(caseActionDataMap, List.of(),
            LocalDate.now(), 20);

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(GetCaseSummaryResponse.from(
            new CaseSummary(caseType, null, null, null, additionalFields, null, null, null, null, null, null,
                caseActionData, null)));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        // WHEN
        Object result = getCaseSummaryAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull().isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(
            GetCaseSummaryResponse.class); // is actually protected subclass.

        GetCaseSummaryResponse getCaseSummaryResponse = (GetCaseSummaryResponse) resultResponseEntity.getBody();

        assertThat(getCaseSummaryResponse.getAdditionalFields()).hasSize(additionalFields.size());
        assertThat(getCaseSummaryResponse.getActiveStages()).isEmpty();
        assertThat(getCaseSummaryResponse.getActions().getCaseActionData()).isEmpty();
    }

    @Test
    public void testShouldReturnFilteredAdditionalFieldDtoListWhenHasPermittedFields() {
        // GIVEN
        String caseType = "CASE_TYPE";

        AdditionalField additionalField1 = new AdditionalField("Field 1", "ValField1", null, null, "Data1");
        AdditionalField additionalField2 = new AdditionalField("Field 2", "ValField2", null, null, "Data2");
        AdditionalField additionalField3 = new AdditionalField("Field 3", "ValField3", null, null, "Data3");
        AdditionalField additionalField4 = new AdditionalField("Field 4", "ValField4", null, null, "Data4");

        Set<AdditionalField> additionalFields = Set.of(additionalField1, additionalField2, additionalField3,
            additionalField4);

        Map<String, List<ActionDataDto>> caseActionDataMap = new HashMap<>();

        ActionDataAppealDto appealDto = new ActionDataAppealDto(null, null, "TEST_APPEAL", "ACTION_LABEL", null,
            LocalDate.MAX, null, null, "TEST NOTE - This appeal should be removed by case summary auth filter", "{}",
            null);

        caseActionDataMap.put("appeals", new ArrayList(List.of(appealDto)));

        CaseActionDataResponseDto caseActionData = CaseActionDataResponseDto.from(caseActionDataMap, List.of(),
            LocalDate.now(), 20);

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(GetCaseSummaryResponse.from(
            new CaseSummary(caseType, null, null, null, additionalFields, null, null, null, null, null, null,
                caseActionData, null)));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        // WHEN
        Object result = getCaseSummaryAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull().isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(
            GetCaseSummaryResponse.class); // is actually protected subclass.

        GetCaseSummaryResponse getCaseSummaryResponse = (GetCaseSummaryResponse) resultResponseEntity.getBody();

        assertThat(getCaseSummaryResponse.getAdditionalFields().size()).isEqualTo(2);
        assertThat(getCaseSummaryResponse.getActiveStages().size()).isEqualTo(0);
        assertThat(getCaseSummaryResponse.getAdditionalFields().stream().map(AdditionalFieldDto::getName)).contains(
            "Data1", "Data2");
        assertThat(
            getCaseSummaryResponse.getAdditionalFields().stream().map(AdditionalFieldDto::getName)).doesNotContain(
            "Data3", "Data4");
        assertThat(getCaseSummaryResponse.getActions().getCaseActionData().size()).isEqualTo(0);
    }

}
