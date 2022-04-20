package uk.gov.digital.ho.hocs.casework.security.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseAuthFilterServiceTest {

    @Mock
    private UserPermissionsService userPermissionsService;

    private GetCaseAuthFilterService getCaseAuthFilterService;

    @Before
    public void setUp() {
        getCaseAuthFilterService = new GetCaseAuthFilterService(userPermissionsService);
    }

    @Test(expected = SecurityExceptions.AuthFilterException.class)
    public void testShouldThrowExceptionIfFilterNotForObjectType() {

        // GIVEN
        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
                GetCaseSummaryResponse.from(
                        new CaseSummary(
                                null, null, null,
                                null, null, null,
                                null, null, null,
                                null, null, null, null)
        ));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        // WHEN
        getCaseAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN - Expect Exception
    }

    @Test
    public void testShouldReturnEmptyCaseDataMapIfNotPermittedToSeeAnyData() {
        // GIVEN
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

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        when(userPermissionsService.getFieldsByCaseTypeAndPermissionLevel(caseType,userAccessLevel)).thenReturn(List.of());
        // WHEN
        Object result = getCaseAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(GetCaseResponse.class); // is actually subclass.

        GetCaseResponse getCaseResponse = (GetCaseResponse) resultResponseEntity.getBody();

        assertThat(getCaseResponse.getData().size()).isEqualTo(0);
    }

    @Test
    public void testShouldReturnCaseDataMapWithPermittedData() {
        // GIVEN
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

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        FieldDto Data1 = new FieldDto(UUID.randomUUID(), "Data1","Data 1", null, null, true, true, AccessLevel.RESTRICTED_OWNER, null);
        FieldDto Data2 = new FieldDto(UUID.randomUUID(), "Data2","Data 2", null, null, true, true, AccessLevel.RESTRICTED_OWNER, null);

        when(userPermissionsService.getFieldsByCaseTypeAndPermissionLevel(caseType,userAccessLevel)).thenReturn(List.of(Data1, Data2));
        // WHEN
        Object result = getCaseAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(GetCaseResponse.class); // is actually protected subclass.

        GetCaseResponse getCaseResponse = (GetCaseResponse) resultResponseEntity.getBody();

        assertThat(getCaseResponse.getData().size()).isEqualTo(2);
        assertThat(getCaseResponse.getData().keySet()).containsAll(Arrays.asList("Data1", "Data2"));
        assertThat(getCaseResponse.getData().keySet()).doesNotContainAnyElementsOf(Arrays.asList("Data3", "Data4"));

    }
}