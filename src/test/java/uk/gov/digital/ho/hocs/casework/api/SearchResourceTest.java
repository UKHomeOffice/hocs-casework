package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStagesResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(SearchResource.class)
@RunWith(SpringRunner.class)
public class SearchResourceTest {

    @MockBean
    private SearchService searchService;

    private SearchResource searchResource;

    @Before
    public void setUp() {
        searchResource = new SearchResource(searchService);
    }

    @Test
    public void shouldSearch() {

        Set<StageWithCaseData> stages = new HashSet<>();
        SearchRequest searchRequest = new SearchRequest();

        when(searchService.search(searchRequest)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = searchResource.search(searchRequest);

        verify(searchService).search(searchRequest);
        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(searchService);
    }

}
