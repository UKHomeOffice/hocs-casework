package uk.gov.digital.ho.hocs.casework.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetailsRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseDetails;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {

    @Mock
    private AuditService auditService;
    @Mock
    private CaseDetailsRepository caseDetailsRepository;

    private SearchService searchService;

    private final String testUser = "Test User";

    //@Captor
    //private ArgumentCaptor<AuditEntry> auditEntryArgumentCaptor;


    @Before
    public void setUp() {
        this.searchService = new SearchService(
                auditService,
                caseDetailsRepository
        );
    }

    @Test
    public void shouldFindCasesByReference() {
        SearchRequest searchRequest = new SearchRequest("CaseRef", null);
        List<CaseDetails> cases = searchService.findCases(searchRequest, testUser);

        assertThat(cases).isNotNull();
        verify(caseDetailsRepository).findByCaseReference(isA(String.class));
        verify(caseDetailsRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(auditService).writeSearchEvent(testUser, searchRequest);
/*        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNotNull();
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNull();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEARCH.toString());*/
    }

    @Test
    public void shouldFindCasesByNameOrDob() {
        Map<String, String> caseData = new HashMap<>();
        caseData.put("first-name", "Rick");
        caseData.put("last-name", "Sanchez");
        caseData.put("dob", "1960-01-01");
        SearchRequest searchRequest = new SearchRequest(null, caseData);
        List<CaseDetails> cases = searchService.findCases(
                searchRequest,
                testUser
        );

        assertThat(cases).isNotNull();
        verify(caseDetailsRepository, times(0)).findByCaseReference(any());
        verify(caseDetailsRepository).findByNameOrDob(isA(String.class), isA(String.class), isA(String.class));
        verify(auditService).writeSearchEvent(testUser, searchRequest);
    }

    @Test
    public void shouldReturnEmptyWhenNoParamsPassed() {
        SearchRequest searchRequest = new SearchRequest(null, null);
        List<CaseDetails> cases = searchService.findCases(
                searchRequest,
                testUser
        );

        assertThat(cases).isNotNull();
        assertThat(cases).isEmpty();
        verify(caseDetailsRepository, times(0)).findByCaseReference(any());
        verify(caseDetailsRepository, times(0)).findByNameOrDob(any(), any(), any());
        verify(auditService).writeSearchEvent(testUser, searchRequest);
    }

}