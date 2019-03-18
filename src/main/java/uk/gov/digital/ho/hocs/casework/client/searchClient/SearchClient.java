package uk.gov.digital.ho.hocs.casework.client.searchClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Component
public class SearchClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public SearchClient(RestHelper restHelper,
                        @Value("${hocs.search-service}") String infoService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;
    }

    public Set<UUID> search(SearchRequest searchRequest) {
        try {
            Set<UUID> response = restHelper.post(serviceBaseURL, "/case", searchRequest, Set.class);
            log.info("Got {} caseUUIDs", response.size(), value(EVENT, SEARCH_CLIENT_SEARCH_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get search results", value(EVENT, SEARCH_CLIENT_SEARCH_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException("Could not get search results", SEARCH_CLIENT_SEARCH_FAILURE);
        }
    }
}