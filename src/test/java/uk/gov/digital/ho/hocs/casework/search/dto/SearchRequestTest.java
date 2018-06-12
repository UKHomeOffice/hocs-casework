package uk.gov.digital.ho.hocs.casework.search.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SearchRequestTest {

    @Test
    public void testCreateWithNoEntities() {
        SearchRequest searchRequest = new SearchRequest();

        assertThat(searchRequest.getCaseData()).isEmpty();
        assertThat(searchRequest.getCaseReference()).isEmpty();
    }

    @Test
    public void testCreateWithEntities() {
        Map<String, String> caseData = new HashMap<>();
        caseData.put("key", "value");
        String reference = "reference";
        SearchRequest searchRequest = new SearchRequest(reference, caseData);

        assertThat(searchRequest.getCaseData()).isEqualTo(caseData);
        assertThat(searchRequest.getCaseReference()).isEqualTo(reference);
    }

    @Test
    public void testCreateWithNoEntitiesToJson() throws JsonProcessingException {
        SearchRequest searchRequest = new SearchRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonCompare = objectMapper.writeValueAsString(searchRequest);

        assertThat(SearchRequest.toJsonString(objectMapper, searchRequest)).isEqualTo(jsonCompare);
    }

    @Test
    public void testCreateWithEntitiesToJson() throws JsonProcessingException {
        Map<String, String> caseData = new HashMap<>();
        caseData.put("key", "value");
        String reference = "reference";
        SearchRequest searchRequest = new SearchRequest(reference, caseData);

        ObjectMapper om = spy(new ObjectMapper());
        when(om.writeValueAsString(searchRequest)).thenThrow(new JsonProcessingException("") {
        });

        assertThat(SearchRequest.toJsonString(om, searchRequest)).isEqualTo("");
    }

}
