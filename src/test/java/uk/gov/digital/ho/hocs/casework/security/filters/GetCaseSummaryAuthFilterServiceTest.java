package uk.gov.digital.ho.hocs.casework.security.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.AdditionalFieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseSummaryAuthFilterServiceTest {

    @Mock
    private UserPermissionsService userPermissionsService;

    private GetCaseSummaryAuthFilterService getCaseSummaryAuthFilterService;

    @Before
    public void setUp() {
        getCaseSummaryAuthFilterService = new GetCaseSummaryAuthFilterService(userPermissionsService);
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

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(GetCaseResponse.from(
                new CaseData(
                        type, caseNumber, caseDataMap, null
                ),
                true
        ));

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

        Set<AdditionalField> additionalFields = Set.of(additionalField1, additionalField2, additionalField3, additionalField4);

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
                GetCaseSummaryResponse.from(
                        new CaseSummary(
                                caseType, null, null,
                                null, additionalFields, null,
                                null, null, null,
                                null, null, null)
                ));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        when(userPermissionsService.getFieldsByCaseTypeAndPermissionLevel(caseType,userAccessLevel)).thenReturn(List.of());


        // WHEN
        Object result = getCaseSummaryAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(GetCaseSummaryResponse.class); // is actually protected subclass.

        GetCaseSummaryResponse getCaseSummaryResponse = (GetCaseSummaryResponse) resultResponseEntity.getBody();

        assertThat(getCaseSummaryResponse.getAdditionalFields().size()).isEqualTo(0);
        assertThat(getCaseSummaryResponse.getActiveStages().size()).isEqualTo(0);
    }

    @Test
    public void testShouldReturnFilteredAdditionalFieldDtoListWhenHasPermittedFields() {

        // GIVEN
        String caseType = "CASE_TYPE";

        AdditionalField additionalField1 = new AdditionalField("Field 1", "ValField1", null, null, "field1");
        AdditionalField additionalField2 = new AdditionalField("Field 2", "ValField2", null, null, "field2");
        AdditionalField additionalField3 = new AdditionalField("Field 3", "ValField3", null, null, "field3");
        AdditionalField additionalField4 = new AdditionalField("Field 4", "ValField4", null, null, "field4");

        Set<AdditionalField> additionalFields = Set.of(additionalField1, additionalField2, additionalField3, additionalField4);

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
                GetCaseSummaryResponse.from(
                        new CaseSummary(
                                caseType, null, null,
                                null, additionalFields, null,
                                null, null, null,
                                null, null, null)
                ));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        FieldDto field1 = new FieldDto(UUID.randomUUID(), "field1","Field 1", null, null, true, true, AccessLevel.RESTRICTED_OWNER, null);
        FieldDto field2 = new FieldDto(UUID.randomUUID(), "field2","Field 2", null, null, true, true, AccessLevel.RESTRICTED_OWNER, null);

        when(userPermissionsService.getFieldsByCaseTypeAndPermissionLevel(caseType,userAccessLevel)).thenReturn(List.of(field1, field2));


        // WHEN
        Object result = getCaseSummaryAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(GetCaseSummaryResponse.class); // is actually protected subclass.

        GetCaseSummaryResponse getCaseSummaryResponse = (GetCaseSummaryResponse) resultResponseEntity.getBody();

        assertThat(getCaseSummaryResponse.getAdditionalFields().size()).isEqualTo(2);
        assertThat(getCaseSummaryResponse.getActiveStages().size()).isEqualTo(0);
        assertThat(getCaseSummaryResponse.getAdditionalFields().stream().map(AdditionalFieldDto::getName)).contains("field1", "field2");
        assertThat(getCaseSummaryResponse.getAdditionalFields().stream().map(AdditionalFieldDto::getName)).doesNotContain("field3", "field4");
    }

}