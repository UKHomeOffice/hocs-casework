package uk.gov.digital.ho.hocs.casework.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.io.IOException;
import java.util.HashSet;
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
    @Mock
    private RequestData mockRequestData;

    private SearchService searchService;

    private static Set<CaseData> getValidSet() {
        Set<CaseData> hashSet = new HashSet<>();
        hashSet.add(new CaseData(CaseType.MIN, 0l));
        return hashSet;
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        this.searchService = new SearchService(
                mockAuditService,
                mockCaseDataRepository,
                mockRequestData
        );
    }

    @Test
    public void shouldFindCasesByReference() throws IOException {
        when(mockCaseDataRepository.findByCaseReference(anyString())).thenReturn(getValidSet());

        SearchRequest searchRequest = objectMapper.readValue("{ \"caseReference\" : \"fsfd\" }", SearchRequest.class);
        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isNotEmpty();

        verify(mockCaseDataRepository, times(1)).findByCaseReference(anyString());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldFindCasesByReferenceNoResults() throws IOException {
        when(mockCaseDataRepository.findByCaseReference(anyString())).thenReturn(new HashSet<>());

        SearchRequest searchRequest = objectMapper.readValue("{ \"caseReference\" : \"fsfd\" }", SearchRequest.class);
        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(1)).findByCaseReference(anyString());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldFindCasesByReferenceNullResults() throws IOException {
        when(mockCaseDataRepository.findByCaseReference(anyString())).thenReturn(null);

        SearchRequest searchRequest = objectMapper.readValue("{ \"caseReference\" : \"fsfd\" }", SearchRequest.class);
        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(1)).findByCaseReference(anyString());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldFindCasesByNameOrDob() throws IOException {
        when(mockCaseDataRepository.findByNameOrDob(anyString(), anyString(), anyString())).thenReturn(getValidSet());

        SearchRequest searchRequest = objectMapper.readValue("{\"caseData\" : { \"first-name\" : \"Rick\", \"last-name\" : \"Sanchez\", \"dob\" : \"1960-01-01\"} }", SearchRequest.class);
        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isNotEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(1)).findByNameOrDob(anyString(), anyString(), anyString());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldFindCasesByNameOrDobNoResults() throws IOException {
        when(mockCaseDataRepository.findByNameOrDob(anyString(), anyString(), anyString())).thenReturn(new HashSet<>());

        SearchRequest searchRequest = objectMapper.readValue("{\"caseData\" : { \"first-name\" : \"Rick\", \"last-name\" : \"Sanchez\", \"dob\" : \"1960-01-01\"} }", SearchRequest.class);

        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(1)).findByNameOrDob(anyString(), anyString(), anyString());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldFindCasesByNameOrDobNullResults() throws IOException {
        when(mockCaseDataRepository.findByNameOrDob(anyString(), anyString(), anyString())).thenReturn(null);

        SearchRequest searchRequest = objectMapper.readValue("{\"caseData\" : { \"first-name\" : \"Rick\", \"last-name\" : \"Sanchez\", \"dob\" : \"1960-01-01\"} }", SearchRequest.class);

        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(1)).findByNameOrDob(anyString(), anyString(), anyString());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldReturnEmptyWhenNoParamsPassed() {
        SearchRequest searchRequest = new SearchRequest();
        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldReturnEmptyWhenNull() throws IOException {
        SearchRequest searchRequest = objectMapper.readValue("{}", SearchRequest.class);

        Set<CaseData> cases = searchService.findCases(searchRequest);

        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(0)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(0)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

    @Test
    public void shouldReturnEmptyWhenAllParamsPassed() throws IOException {
        // Return Nothing

        SearchRequest searchRequest = objectMapper.readValue("{ \"caseReference\" : \"fsfd\", \"caseData\" : { \"first-name\" : \"Rick\", \"last-name\" : \"Sanchez\", \"dob\" : \"1960-01-01\"} }", SearchRequest.class);

        Set<CaseData> cases = searchService.findCases(searchRequest);

        // Return empty List (not null)
        assertThat(cases).isNotNull();
        assertThat(cases).isEmpty();

        verify(mockCaseDataRepository, times(1)).findByCaseReference(any());
        verify(mockCaseDataRepository, times(1)).findByNameOrDob(any(), any(), any());

        verify(mockAuditService, times(1)).writeSearchEvent(searchRequest);
    }

}