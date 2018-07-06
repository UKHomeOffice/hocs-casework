package uk.gov.digital.ho.hocs.casework.search.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SearchRequestTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCreateWithNoEntities() {
        SearchRequest searchRequest = new SearchRequest();

        assertThat(searchRequest.getCaseData()).isEmpty();
        assertThat(searchRequest.getCaseReference()).isEqualTo("");
    }

    @Test
    public void testCreateNull() throws IOException {
        SearchRequest searchRequest = objectMapper.readValue("{}", SearchRequest.class);

        Map<String, String> caseData = new HashMap<>();
        assertThat(searchRequest.getCaseData()).isEqualTo(caseData);
        assertThat(searchRequest.getCaseReference()).isEqualTo("");
    }

    @Test
    public void testCreateWithEntities() throws IOException {
        SearchRequest searchRequest = objectMapper.readValue("{ \"caseReference\" : \"fsfd\", \"caseData\" : { \"key\" : \"value\"} }", SearchRequest.class);

        Map<String, String> caseData = new HashMap<>();
        caseData.put("key", "value");
        assertThat(searchRequest.getCaseData()).isEqualTo(caseData);
        assertThat(searchRequest.getCaseReference()).isEqualTo("fsfd");
    }

    @Test
    public void testCreateToJson() throws JsonProcessingException {
        SearchRequest searchRequest = new SearchRequest();
        String jsonCompare = objectMapper.writeValueAsString(searchRequest);

        assertThat(SearchRequest.toJsonString(objectMapper, searchRequest)).isEqualTo(jsonCompare);
    }

    @Test
    public void testCreateToJsonException() throws JsonProcessingException {
        SearchRequest searchRequest = new SearchRequest();

        ObjectMapper om = spy(new ObjectMapper());
        when(om.writeValueAsString(searchRequest)).thenThrow(new JsonProcessingException("") {
        });

        assertThat(SearchRequest.toJsonString(om, searchRequest)).isEqualTo("");
    }

}
