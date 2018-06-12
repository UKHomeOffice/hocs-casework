package uk.gov.digital.ho.hocs.casework.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {

    @Mock
    private AuditService mockAuditService;

    @Mock
    private CaseDataRepository mockCaseDataRepository;

    private SearchService searchService;

    private final String testUser = "Test User";

    private static Set<CaseData> getValidSet() {
        Set<CaseData> hashSet = new HashSet<>();
        hashSet.add(new CaseData("REF", 0L));
        return hashSet;
    }

    @Before
    public void setUp() {
        this.searchService = new SearchService(
                mockAuditService,
                mockCaseDataRepository
        );
    }

    @Test
    public void shouldFindCasesByReference() {
        when(mockCaseDataRepository.findByCaseReference(anyString())).thenReturn(getValidSet());

        SearchRequest searchRequest = new SearchRequest("NotMatter", null);
        Set<CaseData> cases = searchService.findCases(searchRequest, testUser);

        assertThat(cases).isNotEmpty();

        verify(mockCaseDataRepository, times(1)).findByCaseReference(anyString());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService).writeSearchEvent(testUser, searchRequest);
    }

    @Test
    public void shouldFindCasesByReferenceNoResults() {
        when(mockCaseDataRepository.findByCaseReference(anyString())).thenReturn(null);

        SearchRequest searchRequest = new SearchRequest("NotMatter", null);
        Set<CaseData> cases = searchService.findCases(searchRequest, testUser);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(1)).findByCaseReference(anyString());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(testUser, searchRequest);
    }

    @Test
    public void shouldFindCasesByNameOrDob() {
        when(mockCaseDataRepository.findByNameOrDob(anyString(), anyString(), anyString())).thenReturn(getValidSet());

        Map<String, String> caseData = new HashMap<>();
        caseData.put("first-name", "Rick");
        caseData.put("last-name", "Sanchez");
        caseData.put("dob", "1960-01-01");
        SearchRequest searchRequest = new SearchRequest(null, caseData);
        Set<CaseData> cases = searchService.findCases(searchRequest, testUser);

        assertThat(cases).isNotEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(1)).findByNameOrDob(anyString(), anyString(), anyString());

        verify(mockAuditService, times(1)).writeSearchEvent(testUser, searchRequest);
    }

    @Test
    public void shouldFindCasesByNameOrDobNoResults() {
        when(mockCaseDataRepository.findByNameOrDob(anyString(), anyString(), anyString())).thenReturn(null);

        Map<String, String> caseData = new HashMap<>();
        caseData.put("first-name", "Rick");
        caseData.put("last-name", "Sanchez");
        caseData.put("dob", "1960-01-01");
        SearchRequest searchRequest = new SearchRequest(null, caseData);
        Set<CaseData> cases = searchService.findCases(searchRequest, testUser);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(1)).findByNameOrDob(anyString(), anyString(), anyString());

        verify(mockAuditService, times(1)).writeSearchEvent(testUser, searchRequest);
    }

    @Test
    public void shouldReturnEmptyWhenNoParamsPassed() {
        SearchRequest searchRequest = new SearchRequest(null, null);
        Set<CaseData> cases = searchService.findCases(searchRequest, testUser);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(testUser, searchRequest);
    }

    @Test
    public void shouldReturnEmptyWhenAllParamsPassed() {
        Map<String, String> caseData = new HashMap<>();
        caseData.put("first-name", "Rick");
        caseData.put("last-name", "Sanchez");
        caseData.put("dob", "1960-01-01");

        SearchRequest searchRequest = new SearchRequest("NoMatter", caseData);
        Set<CaseData> cases = searchService.findCases(searchRequest, testUser);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(1)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(1)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(testUser, searchRequest);
    }

}