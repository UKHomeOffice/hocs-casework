package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

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
        ResponseEntity<GetCaseTypesResponse> response = restHelper.get(serviceBaseURL, "/caseType", GetCaseTypesResponse.class);
        return response.getBody().getCaseTypes();
    }


    @Cacheable(value = "getCaseTypeByShortCode")
    public CaseDataType getCaseTypeByShortCode(String shortCode) {
        ResponseEntity<CaseDataType> response = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode), CaseDataType.class);
        return response.getBody();
    }


    public Set<String> getCaseSummaryFields(String type) {
        ResponseEntity<GetSummaryFieldsResponse> response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/summary", type), GetSummaryFieldsResponse.class);
        return response.getBody().getFields();
    }

    public Map<StageType, LocalDate> getDeadlines(String caseType, LocalDate localDate) {
        ResponseEntity<InfoGetDeadlinesResponse> response = restHelper.get(serviceBaseURL, String.format("/casetype/%s/deadlines/%s", caseType, localDate), InfoGetDeadlinesResponse.class);
        return response.getBody().getDeadlines();
    }



}