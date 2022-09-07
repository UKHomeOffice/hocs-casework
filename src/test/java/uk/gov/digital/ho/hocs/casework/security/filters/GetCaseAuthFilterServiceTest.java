package uk.gov.digital.ho.hocs.casework.security.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.RestrictedFieldService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.repository.RestrictedFieldRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseAuthFilterServiceTest {

    @Mock
    private RestrictedFieldRepository restrictedFieldRepository;

    private GetCaseAuthFilterService getCaseAuthFilterService;

    @Before
    public void setUp() {
        when(restrictedFieldRepository.getByCaseTypeAndPermissionLevelGreaterThanEqual(any(),
            eq(AccessLevel.RESTRICTED_OWNER))).thenReturn(Set.of("Data3", "Data4"));

        var restrictedFieldService = new RestrictedFieldService(restrictedFieldRepository);

        getCaseAuthFilterService = new GetCaseAuthFilterService(restrictedFieldService);
    }

    @Test(expected = SecurityExceptions.AuthFilterException.class)
    public void testShouldThrowExceptionIfFilterNotForObjectType() {

        // GIVEN
        ResponseEntity<?> responseToFilter = ResponseEntity.ok(GetCaseSummaryResponse.from(
            new CaseSummary(null, null, null, null, null, null, null, null, null, null, null, null, null)));

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
        caseDataMap.put("Data3", "Data 3");
        caseDataMap.put("Data4", "Data 4");

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
            GetCaseResponse.from(new CaseData(type, caseNumber, caseDataMap, null), true));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        // WHEN
        Object result = getCaseAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull().isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        GetCaseResponse getCaseResponse = (GetCaseResponse) resultResponseEntity.getBody();

        assertThat(getCaseResponse).isNotNull();
        assertThat(getCaseResponse.getData()).isEmpty();
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

        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
            GetCaseResponse.from(new CaseData(type, caseNumber, caseDataMap, null), true));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        // WHEN
        Object result = getCaseAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result).isNotNull().isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(
            GetCaseResponse.class); // is actually protected subclass.

        GetCaseResponse getCaseResponse = (GetCaseResponse) resultResponseEntity.getBody();

        assertThat(getCaseResponse.getData()).hasSize(2);
        assertThat(getCaseResponse.getData().keySet()).containsAll(Arrays.asList("Data1", "Data2"));
        assertThat(getCaseResponse.getData().keySet()).doesNotContainAnyElementsOf(Arrays.asList("Data3", "Data4"));

    }

}
