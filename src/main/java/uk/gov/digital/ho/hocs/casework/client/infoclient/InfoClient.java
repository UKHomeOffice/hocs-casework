package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class InfoClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;


    @Autowired
    public InfoClient(RestHelper restHelper,
                      @Value("${hocs.info-service}") String infoService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;
    }

    public Set<CaseDataType> getCaseTypes() {
        GetCaseTypesResponse response = restHelper.get(serviceBaseURL, "/caseType", GetCaseTypesResponse.class);
        return response.getCaseTypes();
    }

    @Cacheable(value = "getCaseTypeByShortCode")
    public CaseDataType getCaseTypeByShortCode(String shortCode) {
        return restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode), CaseDataType.class);
    }

    public InfoTopic getTopic(UUID topicUUID) {
        return restHelper.get(serviceBaseURL, String.format("/topic/%s", topicUUID), InfoTopic.class);
    }

    public Set<String> getCaseSummaryFields(String type) {
        GetSummaryFieldsResponse response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/summary", type), GetSummaryFieldsResponse.class);
        return response.getFields();
    }

    public Map<String, LocalDate> getDeadlines(String caseType, LocalDate localDate) {
        InfoGetDeadlinesResponse response = restHelper.get(serviceBaseURL, String.format("/casetype/%s/deadlines/%s", caseType, localDate), InfoGetDeadlinesResponse.class);
        return response.getDeadlines();
    }

    public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
        InfoGetNominatedPeopleResponse response = restHelper.get(serviceBaseURL, String.format("/nominatedpeople/%s", teamUUID), InfoGetNominatedPeopleResponse.class);
        return response.getNominatedPeople();
    }

    public UserDto getUser(UUID userUUD) {
        return restHelper.get(serviceBaseURL, String.format("/user/%s", userUUD), UserDto.class);
    }
}