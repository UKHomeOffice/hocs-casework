package uk.gov.digital.ho.hocs.casework.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {

    @Mock
    private SearchService mockSearchService;

    private SearchResource searchResource;

    private static Set<CaseData> getValidSet() {
        Set<CaseData> set = new HashSet<>();
        set.add(new CaseData(UUID.randomUUID(), CaseType.MIN.toString(), 0L));
        return set;
    }

    @Before
    public void setUp() {
        this.searchResource = new SearchResource(mockSearchService);
    }

    @Test
    public void shouldRetrieveAllEntities() {
        when(mockSearchService.findCases(any(SearchRequest.class))).thenReturn(getValidSet());

        SearchRequest searchRequest = new SearchRequest();
        ResponseEntity<Set<CaseData>> responseEntity = searchResource.search(searchRequest);

        verify(mockSearchService, times(1)).findCases(searchRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(1);

    }

    @Test
    public void shouldRetrieveAllEntitiesEmpty() {
        when(mockSearchService.findCases(any(SearchRequest.class))).thenReturn(new HashSet<>());

        SearchRequest searchRequest = new SearchRequest();
        ResponseEntity<Set<CaseData>> responseEntity = searchResource.search(searchRequest);

        verify(mockSearchService, times(1)).findCases(searchRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(0);
    }

    @Test
    public void shouldRetrieveAllEntitiesNull() {
        when(mockSearchService.findCases(null)).thenReturn(new HashSet<>());

        ResponseEntity<Set<CaseData>> responseEntity = searchResource.search(null);

        verify(mockSearchService, times(1)).findCases(null);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(0);
    }
}