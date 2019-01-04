package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.HocsFormData;

import java.time.LocalDate;
import java.util.*;

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
            log.info("Got CaseDataType {} for Short code {}", caseDataType.getType(), shortCode, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
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
    public HocsFormData[] getCaseSummaryFields(String caseType) {
        try {
            HocsFormData[] response = restHelper.get(serviceBaseURL, String.format("/form/caseType/%s/summary", caseType), HocsFormData[].class);
            log.info("Got {} case summary fields for CaseType {}", response.length, caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get case summary fields for CaseType {}", caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get case summary fields for CaseType %s", caseType), INFO_CLIENT_GET_SUMMARY_FIELDS_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetDeadlines")
    public Map<String, String> getDeadlines(String caseType, LocalDate localDate) {
        try {
            Map<String,String> response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/stageType/deadline?received=%s", caseType, localDate), Map.class);
            log.info("Got {} case deadlines for CaseType {} and Date {}", response.size(), caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINES_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get deadlines for CaseType {} and Date {}", caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINES_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get deadlines for CaseType %s and Date %s", caseType, localDate), INFO_CLIENT_GET_DEADLINES_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetCaseNoteStageTypes")
    public Set<String> getCaseNoteStageTypes(String stageType) {
        //try {
        //    InfoGetCaseNoteStageTypeResponse response = restHelper.get(serviceBaseURL, String.format("/stagetype/%s/caseNoteStageType", stageType), InfoGetCaseNoteStageTypeResponse.class);
        //    log.info("Got {} case note stage types for stageType {}", response.getCaseNoteStageTypes().size(), stageType, value(EVENT, INFO_CLIENT_GET_CASE_NOTE_STAGE_TYPE_SUCCESS));
        //    return response.getCaseNoteStageTypes();
        //} catch (ApplicationExceptions.ResourceException e) {
        //    log.error("Could not get case note stage types for stageType {}", stageType, value(EVENT, INFO_CLIENT_GET_CASE_NOTE_STAGE_TYPE_FAILURE));
        //    throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get case note stage types for stageType %s", stageType), INFO_CLIENT_GET_CASE_NOTE_STAGE_TYPE_FAILURE);
        //}
        return new HashSet<>(Arrays.asList("DCU_MIN_DATA_INPUT","DCU_MIN_MARKUP"));
    }

    @Cacheable(value = "InfoClientGetNominatedPeople")
    public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
        try {
            InfoGetNominatedPeopleResponse response = restHelper.get(serviceBaseURL, String.format("/team/%s/contact", teamUUID), InfoGetNominatedPeopleResponse.class);
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