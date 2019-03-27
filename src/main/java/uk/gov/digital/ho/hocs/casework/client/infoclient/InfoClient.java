package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTemplateResponse;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.HashMap;
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

    @Cacheable(value = "InfoClientGetStandardLine")
    public GetStandardLineResponse getStandardLine(UUID topicUUID) {
        try {
            GetStandardLineResponse standardLine = restHelper.get(serviceBaseURL, String.format("/topic/%s/standardLine", topicUUID), GetStandardLineResponse.class);
            log.info("Got StandardLine {} for Topic {}", standardLine.getDisplayName(), topicUUID, value(EVENT, INFO_CLIENT_GET_STANDARD_LINE_SUCCESS));
            return standardLine;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get StandardLine for Topic {}", topicUUID, value(EVENT, INFO_CLIENT_GET_STANDARD_LINE_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get StandardLine for Topic %s", topicUUID), INFO_CLIENT_GET_STANDARD_LINE_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetTemplate")
    public GetTemplateResponse getTemplate(String caseType) {
        try {
            GetTemplateResponse template = restHelper.get(serviceBaseURL, String.format("/caseType/%s/template", caseType), GetTemplateResponse.class);
            log.info("Got Template {} for CaseType {}", template.getDisplayName(), caseType, value(EVENT, INFO_CLIENT_GET_TEMPLATE_SUCCESS));
            return template;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get Template for CaseType {}", caseType, value(EVENT, INFO_CLIENT_GET_TEMPLATE_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Template for CaseType %s", caseType), INFO_CLIENT_GET_TEMPLATE_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetCaseSummaryFields")
    public FieldDto[] getCaseSummaryFields(String caseType) {
        try {
            FieldDto[] response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s/summary", caseType), FieldDto[].class);
            log.info("Got {} case summary fields for CaseType {}", response.length, caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get case summary fields for CaseType {}", caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get case summary fields for CaseType %s", caseType), INFO_CLIENT_GET_SUMMARY_FIELDS_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetDeadline")
    public LocalDate getCaseDeadline(String caseType, LocalDate localDate) {
        try {
            LocalDate response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/deadline?received=%s", caseType, localDate), LocalDate.class);
            log.info("Got {} as deadline for CaseType {} and Date {}", response.toString(), caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINE_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get deadlines for CaseType {} and Date {}", caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINE_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get deadlines for CaseType %s and Date %s", caseType, localDate), INFO_CLIENT_GET_DEADLINE_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetDeadlines")
    public Map<String, LocalDate> getDeadlines(String caseType, LocalDate localDate) {
        try {
            Map<String,LocalDate> response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/stageType/deadline?received=%s", caseType, localDate), new ParameterizedTypeReference<HashMap<String,LocalDate>>() {});
            log.info("Got {} case deadlines for CaseType {} and Date {}", response.size(), caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINES_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get deadlines for CaseType {} and Date {}", caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINES_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get deadlines for CaseType %s and Date %s", caseType, localDate), INFO_CLIENT_GET_DEADLINES_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetNominatedPeople")
    public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
        try {
            InfoGetNominatedPeopleResponse response = restHelper.get(serviceBaseURL, String.format("/team/%s/contact", teamUUID), InfoGetNominatedPeopleResponse.class);
            log.info("Got {} contacts for Team {}", teamUUID, value(EVENT, INFO_CLIENT_GET_CONTACTS_SUCCESS));
            return response.getNominatedPeople();
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get contacts for Team {}", teamUUID, value(EVENT, INFO_CLIENT_GET_CONTACTS_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get contacts for TeamDto %s", teamUUID), INFO_CLIENT_GET_CONTACTS_FAILURE);
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

    @Cacheable(value = "InfoClientGetTeams")
    public Set<TeamDto> getTeams() {
        try {
            Set<TeamDto> teamDtos = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
            log.info("Got teamDtos", value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
            return teamDtos;
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get teams", value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
            throw new ApplicationExceptions.EntityNotFoundException("Could not get teams", INFO_CLIENT_GET_TEAMS_FAILURE);
        }
    }
}