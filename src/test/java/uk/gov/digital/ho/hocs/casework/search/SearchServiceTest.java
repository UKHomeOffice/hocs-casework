package uk.gov.digital.ho.hocs.casework.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.*;
import uk.gov.digital.ho.hocs.casework.notify.NotifyRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {

    @Mock
    private AuditRepository auditRepository;
    @Mock
    private CaseDetailsRepository caseDetailsRepository;

    private SearchService searchService;

    private final String testUser = "Test User";

    @Captor
    private ArgumentCaptor<AuditEntry> auditEntryArgumentCaptor;


    @Before
    public void setUp() {
        this.searchService = new SearchService(
                caseDetailsRepository,
                auditRepository
        );
    }

    @Test
    public void shouldFindCasesByReference() {
        List<CaseDetails> cases = searchService.findCases(
                new SearchRequest("CaseRef", null),
                testUser
        );

        assertThat(cases).isNotNull();
        verify(caseDetailsRepository).findByCaseReference(isA(String.class));
        verify(caseDetailsRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(auditRepository).save(isA(AuditEntry.class));
        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNotNull();
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNull();
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.SEARCH.toString());
    }

    @Test
    public void shouldFindCasesByNameOrDob() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("first-name", "Rick");
        caseData.put("last-name", "Sanchez");
        caseData.put("first-name", "1960-01-01");
        List<CaseDetails> cases = searchService.findCases(
                new SearchRequest(null, caseData),
                testUser
        );

        assertThat(cases).isNotNull();
        verify(caseDetailsRepository, times(0)).findByCaseReference(any());
        verify(caseDetailsRepository).findByNameOrDob(isA(String.class), isA(String.class), isA(String.class));
        verify(auditRepository).save(isA(AuditEntry.class));
    }

    @Test
    public void shouldReturnEmptyWhenNoParamsPassed() {
        List<CaseDetails> cases = searchService.findCases(
                new SearchRequest(null, null),
                testUser
        );

        assertThat(cases).isNotNull();
        assertThat(cases).isEmpty();
        verify(caseDetailsRepository, times(0)).findByCaseReference(any());
        verify(caseDetailsRepository, times(0)).findByNameOrDob(any(), any(), any());
        verify(auditRepository).save(isA(AuditEntry.class));
    }

}