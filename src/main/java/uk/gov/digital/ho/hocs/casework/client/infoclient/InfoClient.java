package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;

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

    @Cacheable(value = "InfoClientGetCaseTypeByShortCode", unless = "#result == null", key = "#shortCode")
    public CaseDataType getCaseTypeByShortCode(String shortCode) {
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode), CaseDataType.class);
        log.info("Got CaseDataType {} for Short code {}", caseDataType.getDisplayCode(), shortCode, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS));
        return caseDataType;
    }

    @Cacheable(value = "InfoClientGetCaseType", unless = "#result == null", key = "#type")
    public CaseDataType getCaseType(String type) {
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/type/%s", type), CaseDataType.class);
        log.info("Got CaseDataType {} for Type {}", caseDataType.getDisplayCode(), type, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
        return caseDataType;
    }

    public ConstituencyDto getConstituencyByMemberExternalKey(String externalReference) {
        ConstituencyDto constituencyDto = restHelper.get(serviceBaseURL, String.format("/constituency/member/%s", externalReference), ConstituencyDto.class);
        log.info("Got Constituency {} for Member externalKey {}", constituencyDto.getConstituencyName(), externalReference, value(EVENT, INFO_CLIENT_GET_CONSTITUENCY_SUCCESS));
        return constituencyDto;
    }

    @Cacheable(value = "InfoClientGetStandardLine", unless = "#result == null", key = "#topicUUID")
    public GetStandardLineResponse getStandardLine(UUID topicUUID) {
        GetStandardLineResponse standardLine = restHelper.get(serviceBaseURL, String.format("/topic/%s/standardLine", topicUUID), GetStandardLineResponse.class);
        log.info("Got StandardLine {} for Topic {}", standardLine.getDisplayName(), topicUUID, value(EVENT, INFO_CLIENT_GET_STANDARD_LINE_SUCCESS));
        return standardLine;
    }

    @Cacheable(value = "InfoClientGetTemplate", unless = "#result == null", key = "#caseType")
    public GetTemplateResponse getTemplate(String caseType) {
        GetTemplateResponse template = restHelper.get(serviceBaseURL, String.format("/caseType/%s/template", caseType), GetTemplateResponse.class);
        log.info("Got Template {} for CaseType {}", template.getDisplayName(), caseType, value(EVENT, INFO_CLIENT_GET_TEMPLATE_SUCCESS));
        return template;
    }

    @Cacheable(value = "InfoClientGetTeams", unless = "#result.size() == 0")
    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    @Cacheable(value = "InfoClientGetCaseSummaryFieldsRequest", unless = "#result.size() == 0", key = "#caseType")
    public Set<FieldDto> getCaseSummaryFields(String caseType) {
        Set<FieldDto> response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s/summary", caseType), new ParameterizedTypeReference<Set<FieldDto>>() {});
        log.info("Got {} case summary fields for CaseType {}", response.size(), caseType, value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetCaseDeadline", unless = "#result == null", key = "{#caseType, #received.toString() }")
    public LocalDate getCaseDeadline(String caseType, LocalDate received) {
        LocalDate response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/deadline?received=%s", caseType, received), LocalDate.class);
        log.info("Got {} as deadline for CaseType {} and Date {}", response.toString(), caseType, received, value(EVENT, INFO_CLIENT_GET_CASE_DEADLINE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetStageDeadlines", unless = "#result.size() == 0", key = "{#caseType, #received.toString() }")
    public Map<String, LocalDate> getStageDeadlines(String caseType, LocalDate received) {
        Map<String, LocalDate> response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/stageType/deadline?received=%s", caseType, received), new ParameterizedTypeReference<Map<String, LocalDate> >() {});
        log.info("Got {} stage deadlines for CaseType {} and Date {}", response.size(), caseType, received, value(EVENT, INFO_CLIENT_GET_DEADLINES_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetStageDeadline", unless = "#result == null", key = "{#stageType, #received.toString() }")
    public LocalDate getStageDeadline(String stageType, LocalDate received) {
        LocalDate response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/deadline?received=%s", stageType, received), LocalDate.class);
        log.info("Got {} as deadline for StageType {} and Date {}", response.toString(), stageType, received, value(EVENT, INFO_CLIENT_GET_STAGE_DEADLINE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTopic", unless = "#result == null", key = "{#topicUUID}")
    public InfoTopic getTopic(UUID topicUUID) {
        InfoTopic infoTopic = restHelper.get(serviceBaseURL, String.format("/topic/%s", topicUUID), InfoTopic.class);
        log.info("Got Topic {} for Topic {}", infoTopic.getLabel(), topicUUID, value(EVENT, INFO_CLIENT_GET_TOPIC_SUCCESS));
        return infoTopic;
    }
    @Cacheable(value = "InfoClientGetUser", unless = "#result == null", key = "{ #userUUID}")
    public UserDto getUser(UUID userUUID) {
        UserDto userDto = restHelper.get(serviceBaseURL, String.format("/user/%s", userUUID), UserDto.class);
        log.info("Got User UserUUID {}", userUUID, value(EVENT, INFO_CLIENT_GET_USER));
        return userDto;
    }
}