package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTemplateResponse;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Cacheable(value = "InfoClientGetCaseTypesByShortCode", unless = "#result.size() == 0")
    public Map<String, CaseDataType> getCaseTypesByShortCode() {
        GetCaseTypesResponse response = restHelper.get(serviceBaseURL, "/caseType", GetCaseTypesResponse.class);
        log.info("Got {} case types", response.getCaseTypes().size(), value(EVENT, INFO_CLIENT_GET_CASE_TYPES_SUCCESS));
        return response.getCaseTypes().stream().collect(Collectors.toMap(CaseDataType::getShortCode, c -> c));
    }

    public CaseDataType getCaseType(String shortCode) {
        if(getCaseTypesByShortCode().containsKey(shortCode)) {
            return getCaseTypesByShortCode().get(shortCode);
        }
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode), CaseDataType.class);
        log.info("Got CaseDataType {} for Short code {}", caseDataType.getDisplayCode(), shortCode, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
        return caseDataType;
    }

    @Cacheable(value = "InfoClientGetTopic")
    public InfoTopic getTopic(UUID topicUUID) {
        InfoTopic infoTopic = restHelper.get(serviceBaseURL, String.format("/topic/%s", topicUUID), InfoTopic.class);
        log.info("Got Topic {} for Topic {}", infoTopic.getLabel(), topicUUID, value(EVENT, INFO_CLIENT_GET_TOPIC_SUCCESS));
        return infoTopic;
    }

    @Cacheable(value = "InfoClientGetStandardLinesByTopicUUID", unless = "#result.size() == 0")
    public Map<UUID ,GetStandardLineResponse> getStandardLinesByTopicUUID() {
        Set<GetStandardLineResponse> standardLines = restHelper.get(serviceBaseURL, "/standardLine", new ParameterizedTypeReference<HashSet<GetStandardLineResponse>>() {});
        log.info("Got {} StandardLines", standardLines.size(), value(EVENT, INFO_CLIENT_GET_STANDARD_LINES_SUCCESS));
        return standardLines.stream().collect(Collectors.toMap(GetStandardLineResponse::getTopicUUID, s -> s));
    }

    public GetStandardLineResponse getStandardLine(UUID topicUUID) {
        if(getStandardLinesByTopicUUID().containsKey(topicUUID)) {
            return getStandardLinesByTopicUUID().get(topicUUID);
        }
        GetStandardLineResponse standardLine = restHelper.get(serviceBaseURL, String.format("/topic/%s/standardLine", topicUUID), GetStandardLineResponse.class);
        log.info("Got StandardLine {} for Topic {}", standardLine.getDisplayName(), topicUUID, value(EVENT, INFO_CLIENT_GET_STANDARD_LINE_SUCCESS));
        return standardLine;
    }

    @Cacheable(value = "InfoClientGetTemplatesByCaseType", unless = "#result.size() == 0")
    public Map<String,GetTemplateResponse> getTemplatesByCaseType() {
        Set<GetTemplateResponse> response = restHelper.get(serviceBaseURL, "/template", new ParameterizedTypeReference<HashSet<GetTemplateResponse>>() {});
        log.info("Got {} Templates", response.size(), value(EVENT, INFO_CLIENT_GET_TEMPLATES_SUCCESS));
        return response.stream().collect(Collectors.toMap(GetTemplateResponse::getCaseType, t -> t));
    }

    public GetTemplateResponse getTemplate(String caseType) {
        if(getTemplatesByCaseType().containsKey(caseType)) {
            return getTemplatesByCaseType().get(caseType);
        }
        GetTemplateResponse template = restHelper.get(serviceBaseURL, String.format("/caseType/%s/template", caseType), GetTemplateResponse.class);
        log.info("Got Template {} for CaseType {}", template.getDisplayName(), caseType, value(EVENT, INFO_CLIENT_GET_TEMPLATE_SUCCESS));
        return template;
    }

    @Cacheable(value = "InfoClientGetCaseSummaryFields", unless = "#result.size() == 0")
    public Set<FieldDto> getCaseSummaryFields(String caseType) {
        Set<FieldDto> response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s/summary", caseType), new ParameterizedTypeReference<HashSet<FieldDto>>() {});
        log.info("Got {} case summary fields for CaseType {}", response.size(), caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetCaseDeadline")
    public LocalDate getCaseDeadline(String caseType, LocalDate localDate) {
        LocalDate response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/deadline?received=%s", caseType, localDate), LocalDate.class);
        log.info("Got {} as deadline for CaseType {} and Date {}", response.toString(), caseType, localDate, value(EVENT, INFO_CLIENT_GET_CASE_DEADLINE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetStageDeadline")
    public LocalDate getStageDeadline(String stageType, LocalDate localDate) {
        LocalDate response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/deadline?received=%s", stageType, localDate), LocalDate.class);
        log.info("Got {} as deadline for StageType {} and Date {}", response.toString(), stageType, localDate, value(EVENT, INFO_CLIENT_GET_STAGE_DEADLINE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetDeadlines")
    public Map<String, LocalDate> getDeadlines(String caseType, LocalDate localDate) {
        Map<String, LocalDate> response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/stageType/deadline?received=%s", caseType, localDate), new ParameterizedTypeReference<Map<String, LocalDate> >() {});
        log.info("Got {} case deadlines for CaseType {} and Date {}", response.size(), caseType, localDate, value(EVENT, INFO_CLIENT_GET_DEADLINES_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetNominatedPeople", unless = "#result.size() == 0")
    public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
        InfoGetNominatedPeopleResponse response = restHelper.get(serviceBaseURL, String.format("/team/%s/contact", teamUUID), InfoGetNominatedPeopleResponse.class);
        log.info("Got {} contacts for Team {}", teamUUID, value(EVENT, INFO_CLIENT_GET_CONTACTS_SUCCESS));
        return response.getNominatedPeople();
    }

    @Cacheable(value = "InfoClientGetUser")
    public UserDto getUser(UUID userUUID) {
        UserDto userDto = restHelper.get(serviceBaseURL, String.format("/user/%s", userUUID), UserDto.class);
        log.info("Got User UserUUID {}", userUUID, value(EVENT, INFO_CLIENT_GET_USER_SUCCESS));
        return userDto;
    }

    @Cacheable(value = "InfoClientGetTeams", unless = "#result.size() == 0")
    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }
}