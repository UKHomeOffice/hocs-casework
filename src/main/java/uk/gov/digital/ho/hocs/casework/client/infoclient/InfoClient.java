package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

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
        try {
            GetCaseTypesResponse response = restHelper.get(serviceBaseURL, "/caseType", GetCaseTypesResponse.class);
            log.info("Got {} case types", response.getCaseTypes().size(), value(EVENT, INFO_CLIENT_GET_CASE_TYPES_SUCCESS));
            return response.getCaseTypes();
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get case types", value(EVENT, INFO_CLIENT_GET_CASE_TYPES_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException("Could not get case types", INFO_CLIENT_GET_CASE_TYPES_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetCaseType")
    public CaseDataType getCaseType(String shortCode) {
        try {
            CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode), CaseDataType.class);
            log.info("Got CaseDataType {} for Short code {}", caseDataType.getDisplayCode(), shortCode, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
            return caseDataType;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get caseType for Short code {}", shortCode, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get CaseType for Short code %s", shortCode), INFO_CLIENT_GET_CASE_TYPE_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetTopic")
    public InfoTopic getTopic(UUID topicUUID) {
        try {
            InfoTopic infoTopic = restHelper.get(serviceBaseURL, String.format("/topic/%s", topicUUID), InfoTopic.class);
            log.info("Got Topic {} for Topic {}", infoTopic.getLabel(), topicUUID, value(EVENT, INFO_CLIENT_GET_TOPIC_SUCCESS));
            return infoTopic;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get Topic {}", topicUUID, value(EVENT, INFO_CLIENT_GET_TOPIC_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Topic %s", topicUUID), INFO_CLIENT_GET_TOPIC_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetCaseSummaryFields")
    public Set<String> getCaseSummaryFields(String caseType) {
        try {
            GetSummaryFieldsResponse response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/summary", caseType), GetSummaryFieldsResponse.class);
            log.info("Got {} case summary fields for CaseType {}", response.getFields().size(), caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS));
            return response.getFields();
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get case summary fields for CaseType {}", caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get case summary fields for CaseType %s", caseType), INFO_CLIENT_GET_SUMMARY_FIELDS_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetDeadlines")
    public Map<String, LocalDate> getDeadlines(String caseType, LocalDate localDate) {
        try {
            InfoGetDeadlinesResponse response = restHelper.get(serviceBaseURL, String.format("/casetype/%s/deadlines/%s", caseType, localDate), InfoGetDeadlinesResponse.class);
            log.info("Got {} case deadlines for CaseType {} and Date {}", response.getDeadlines().size(), caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINES_SUCCESS));
            return response.getDeadlines();
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get deadlines for CaseType {} and Date {}", caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINES_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get deadlines for CaseType %s and Date %s", caseType, localDate), INFO_CLIENT_GET_DEADLINES_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetNominatedPeople")
    public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
        try {
            InfoGetNominatedPeopleResponse response = restHelper.get(serviceBaseURL, String.format("/nominatedpeople/%s", teamUUID), InfoGetNominatedPeopleResponse.class);
            log.info("Got {} contacts for Team {}", teamUUID, value(EVENT, INFO_CLIENT_GET_CONTACTS_SUCCESS));
            return response.getNominatedPeople();
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get contacts for Team {}", teamUUID, value(EVENT, INFO_CLIENT_GET_CONTACTS_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get contacts for Team %s", teamUUID), INFO_CLIENT_GET_CONTACTS_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetUser")
    public UserDto getUser(UUID userUUID) {
        try {
            UserDto userDto = restHelper.get(serviceBaseURL, String.format("/user/%s", userUUID), UserDto.class);
            log.info("Got User UserUUID {}", userUUID, value(EVENT, INFO_CLIENT_GET_USER_SUCCESS));
            return userDto;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get User for UserUUID {}", userUUID, value(EVENT, INFO_CLIENT_GET_USER_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get User for UserUUID %s", userUUID), INFO_CLIENT_GET_USER_FAILURE);
        }
    }
}