package uk.gov.digital.ho.hocs.casework.client.searchclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SEARCH_CLIENT_SEARCH_SUCCESS;

@Slf4j
@Component
public class SearchClient {

    private final RestHelper restHelper;

    private final String serviceBaseURL;

    @Autowired
    public SearchClient(RestHelper restHelper, @Value("${hocs.search-service}") String searchService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = searchService;
    }

    public Set<UUID> search(SearchRequest searchRequest) {
        Set<UUID> response = restHelper.post(serviceBaseURL, "/case", searchRequest,
            new ParameterizedTypeReference<Set<UUID>>() {});
        log.info("Got {} caseUUID results", response.size(), value(EVENT, SEARCH_CLIENT_SEARCH_SUCCESS));
        return response;
    }

}
