package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.SomuTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.TemplateDto;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EXCEPTION;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_ALL_STAGE_TYPES;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_ALL_USERS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_CASE_TYPES_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_CASE_TYPE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_CONTRIBUTIONS_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_DEADLINES_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_DEFAULT_USERS_FOR_STAGE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_ENTITY_BY_SIMPLE_NAME;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_ENTITY_LIST;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_PROFILE_BY_CASE_TYPE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_STAGE_TYPE_BY_TYPE_STRING;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_STANDARD_LINE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_TEAMS_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_TEAM_BY_UUID_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_TEMPLATE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_TOPIC_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_USER;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_USERS_FOR_TEAM_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.INFO_CLIENT_GET_USER_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MISSING_TEAM_FOR_STAGE;

@Slf4j
@Component
public class InfoClient {

    private final RestHelper restHelper;

    private final String serviceBaseURL;

    @Autowired
    public InfoClient(RestHelper restHelper, @Value("${hocs.info-service}") String infoService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;
    }

    @Cacheable(value = "InfoClientGetCaseTypeByShortCode", unless = "#result == null", key = "#shortCode")
    public CaseDataType getCaseTypeByShortCode(String shortCode) {
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode),
            CaseDataType.class);
        log.info("Got CaseDataType {} for Short code {}", caseDataType.getDisplayCode(), shortCode,
            value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS));
        return caseDataType;
    }

    @Cacheable(value = "InfoClientGetCaseType", unless = "#result == null", key = "#type")
    public CaseDataType getCaseType(String type) {
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/type/%s", type),
            CaseDataType.class);
        log.info("Got CaseDataType {} for Type {}", caseDataType.getDisplayCode(), type,
            value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
        return caseDataType;
    }

    public List<CaseDataType> getAllCaseTypes() {
        List<CaseDataType> caseDataTypes = restHelper.get(serviceBaseURL, "/caseType?initialCaseType=false",
            new ParameterizedTypeReference<>() {});
        log.info("Got all case types", value(EVENT, INFO_CLIENT_GET_CASE_TYPES_SUCCESS));
        return caseDataTypes;
    }

    @Cacheable(value = "InfoClientGetCorrespondentType", unless = "#result == null")
    public GetCorrespondentTypeResponse getAllCorrespondentType() {
        GetCorrespondentTypeResponse correspondentType = restHelper.get(serviceBaseURL, "/correspondentType",
            GetCorrespondentTypeResponse.class);
        log.info("Got CorrespondentTypes {}", correspondentType.getCorrespondentTypes().size(),
            value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
        return correspondentType;
    }

    @Cacheable(value = "InfoClientGetCorrespondentType", unless = "#result == null", key = "#caseType")
    public GetCorrespondentTypeResponse getCorrespondentType(String caseType) {
        GetCorrespondentTypeResponse correspondentType = restHelper.get(serviceBaseURL,
            String.format("/correspondentType/%s", caseType), GetCorrespondentTypeResponse.class);
        log.info("Got CorrespondentTypes {}", correspondentType.getCorrespondentTypes().size(),
            value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
        return correspondentType;
    }

    @Cacheable(value = "InfoClientGetCorrespondentType", unless = "#result == null", key = "#caseType")
    public GetCorrespondentTypeResponse getSelectableCorrespondentType(String caseType) {
        GetCorrespondentTypeResponse correspondentType = restHelper.get(serviceBaseURL,
            String.format("/correspondentType/%s/selectable", caseType), GetCorrespondentTypeResponse.class);
        log.info("Got CorrespondentTypes {}", correspondentType.getCorrespondentTypes().size(),
            value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SUCCESS));
        return correspondentType;
    }

    public GetStandardLineResponse getStandardLine(UUID topicUUID) {
        GetStandardLineResponse standardLine = restHelper.get(serviceBaseURL,
            String.format("/topic/%s/standardLine", topicUUID), GetStandardLineResponse.class);
        log.info("Got StandardLine {} for Topic {}", standardLine.getDisplayName(), topicUUID,
            value(EVENT, INFO_CLIENT_GET_STANDARD_LINE_SUCCESS));
        return standardLine;
    }

    public List<TemplateDto> getTemplates(String caseType) {
        List<TemplateDto> templates = restHelper.get(serviceBaseURL, String.format("/caseType/%s/templates", caseType),
            new ParameterizedTypeReference<>() {});
        log.info("Got Templates {} for CaseType {}, event {}", templates.size(), caseType,
            value(EVENT, INFO_CLIENT_GET_TEMPLATE_SUCCESS));
        return templates;
    }

    @Cacheable(value = "InfoClientGetTeams", unless = "#result.size() == 0")
    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<>() {});
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    @Cacheable(value = "InfoClientGetTeamForUUID", unless = "#result == null", key = "#teamUUID")
    public TeamDto getTeamByUUID(UUID teamUUID) {
        TeamDto team = restHelper.get(serviceBaseURL, String.format("/team/%s", teamUUID), TeamDto.class);
        log.info("Got team TeamUUID {}", teamUUID, value(EVENT, INFO_CLIENT_GET_TEAM_BY_UUID_SUCCESS));
        return team;
    }

    @Cacheable(value = "InfoClientGetTeamForStageType", unless = "#result == null", key = "#stageType")
    public TeamDto getTeamForStageType(String stageType) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/team", stageType),
            TeamDto.class);
        if (response == null) {
            String msg = String.format("There is no team defined for stage type: %s", stageType);
            log.error(msg, value(EXCEPTION, MISSING_TEAM_FOR_STAGE));
            throw new ApplicationExceptions.TeamAllocationException(msg, LogEvent.MISSING_TEAM_FOR_STAGE);
        }
        log.info("Got Team teamUUID {} for Stage {}, event: {}", response.getUuid(), stageType,
            value(EVENT, INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeamForStageAndText", unless = "#result == null", key = "{ #stageType, #text }")
    public TeamDto getTeamByStageAndText(String stageType, String text) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/stage/%s/text/%s", stageType, text),
            TeamDto.class);
        log.info("Got Team teamUUID {} for Stage {} and Text {}", response.getUuid(), stageType, text,
            value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoAllSomuTypesForCaseTypeRequest", unless = "#result.size() == 0", key = "#caseType")
    public Set<SomuTypeDto> getAllSomuTypesForCaseType(String caseType) {
        Set<SomuTypeDto> response = restHelper.get(serviceBaseURL, String.format("/somuType/%s", caseType),
            new ParameterizedTypeReference<>() {});
        log.info("Got {} case summary fields for CaseType {}", response.size(), caseType,
            value(EVENT, INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetAllStages", unless = "#result.size() == 0")
    public Set<StageTypeDto> getAllStageTypes() {
        Set<StageTypeDto> response = restHelper.get(serviceBaseURL, "/stageType",
            new ParameterizedTypeReference<>() {});
        log.info("Got {} stage types", response.size(), value(EVENT, INFO_CLIENT_GET_ALL_STAGE_TYPES));
        return response;
    }

    @Cacheable(value = "InfoClientGetStageTypeByTypeString", unless = "#result == null", key = "#typeString")
    public StageTypeDto getStageTypeByTypeString(String typeString) {
        StageTypeDto response = restHelper.get(serviceBaseURL, String.format("/stageType/type/%s", typeString), StageTypeDto.class);

        log.info("Got stage type {}", typeString, value(EVENT, INFO_CLIENT_GET_STAGE_TYPE_BY_TYPE_STRING));
        return response;
    }

    @Cacheable(value = "InfoClientGetAllStagesForCaseType", unless = "#result.size() == 0", key = "{#caseType}")
    public Set<StageTypeDto> getAllStagesForCaseType(String caseType) {
        Set<StageTypeDto> response = restHelper.get(serviceBaseURL, String.format("/stages/caseType/%s", caseType),
            new ParameterizedTypeReference<>() {});

        log.info("Got {} stage types for CaseType {}", response.size(), caseType,
            value(EVENT, INFO_CLIENT_GET_DEADLINES_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetStageContributions", unless = "#result == null", key = "{#stageType}")
    public Boolean getStageContributions(String stageType) {
        Boolean response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/contributions", stageType),
            new ParameterizedTypeReference<>() {});
        log.info("Got {} as contributions for StageType {}", response.toString(), stageType,
            value(EVENT, INFO_CLIENT_GET_CONTRIBUTIONS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTopic", unless = "#result == null", key = "{#topicUUID}")
    public InfoTopic getTopic(UUID topicUUID) {
        InfoTopic infoTopic = restHelper.get(serviceBaseURL, String.format("/topic/%s", topicUUID), InfoTopic.class);
        log.info("Got Topic {} for Topic {}", infoTopic.getLabel(), topicUUID,
            value(EVENT, INFO_CLIENT_GET_TOPIC_SUCCESS));
        return infoTopic;
    }

    @Cacheable(value = "InfoClientGetUser", unless = "#result == null", key = "{ #userUUID}")
    public UserDto getUser(UUID userUUID) {
        UserDto userDto = restHelper.get(serviceBaseURL, String.format("/user/%s", userUUID), UserDto.class);
        log.info("Got User UserUUID {}", userUUID, value(EVENT, INFO_CLIENT_GET_USER));
        return userDto;
    }

    @Cacheable(value = "InfoClientGetUsers", unless = "#result == null || #result.size() == 0")
    public Set<UserDto> getAllUsers() {
        Set<UserDto> userDtos = restHelper.get(serviceBaseURL, "/users", new ParameterizedTypeReference<>() {});
        log.info("Got {} users", userDtos.size(), value(EVENT, INFO_CLIENT_GET_ALL_USERS));
        return userDtos;
    }

    @Cacheable(value = "InfoClientGetEntityListDtos", unless = "#result == null", key = "#listName")
    public List<EntityDto<EntityTotalDto>> getEntityListTotals(String listName) {
        List<EntityDto<EntityTotalDto>> entityListDtos = restHelper.get(serviceBaseURL,
            String.format("/entity/list/%s", listName), new ParameterizedTypeReference<>() {});
        log.info("Got Entity List By List Name {} ", value(EVENT, INFO_CLIENT_GET_ENTITY_LIST));
        return entityListDtos;
    }

    public EntityDto<Map<String, String>> getEntityBySimpleName(String simpleName) {
        EntityDto<Map<String, String>> entityDto = restHelper.get(serviceBaseURL,
            String.format("/entity/simpleName/%s", simpleName), EntityDto.class);
        log.info("Got Entity By Simple Name {} ", value(EVENT, INFO_CLIENT_GET_ENTITY_BY_SIMPLE_NAME));
        return entityDto;
    }

    @Cacheable(value = "InfoGetProfileByCaseType", unless = "#result == null")
    public ProfileDto getProfileByCaseType(String caseType) {
        ProfileDto response = restHelper.get(serviceBaseURL, String.format("/profile/forcasetype/%s", caseType),
            ProfileDto.class);
        log.info("Got profile {} for case type {}, event {}", response.getProfileName(), caseType,
            value(EVENT, INFO_CLIENT_GET_PROFILE_BY_CASE_TYPE_SUCCESS));
        return response;
    }

    public UserDto getUserForTeam(UUID teamUUID, UUID userUUID) {
        UserDto response = restHelper.get(serviceBaseURL, String.format("/teams/%s/member/%s", teamUUID, userUUID),
            UserDto.class);
        log.info("Got User for Team {} for User {}, event: {}", teamUUID, userUUID,
            value(EVENT, INFO_CLIENT_GET_USER_SUCCESS));
        return response;
    }

    public List<UserDto> getUsersForTeam(UUID teamUUID) {
        List<UserDto> response = restHelper.get(serviceBaseURL, String.format("/teams/%s/members", teamUUID),
            new ParameterizedTypeReference<>() {});
        log.info("Got {} users for team {}", response.size(), teamUUID,
            value(EVENT, INFO_CLIENT_GET_USERS_FOR_TEAM_SUCCESS));
        return response;
    }

    public List<UserDto> getUsersForTeamByStage(UUID caseUUID, UUID stageUUID) {
        List<UserDto> response = restHelper.get(serviceBaseURL,
            String.format("/case/%s/stage/%s/team/members", caseUUID, stageUUID), new ParameterizedTypeReference<>() {});
        log.info("Got {} default users by stage {}", response.size(), stageUUID,
            value(EVENT, INFO_CLIENT_GET_DEFAULT_USERS_FOR_STAGE_SUCCESS));
        return response;
    }

    public CaseTypeActionDto getCaseTypeActionByUuid(String caseDataType, UUID caseTypeActionUuid) {
        log.debug("Requesting action type with id: {}", caseTypeActionUuid);

        CaseTypeActionDto response = restHelper.get(serviceBaseURL,
            String.format("/caseType/%s/actions/%s", caseDataType, caseTypeActionUuid), CaseTypeActionDto.class);

        log.info("Received Action: {} for action id: {}", response.toString(), caseTypeActionUuid);
        return response;
    }

    public List<CaseTypeActionDto> getCaseTypeActionForCaseType(String caseType) {
        log.debug("Requesting case type actions for case type: {}", caseType);

        ParameterizedTypeReference<List<CaseTypeActionDto>> typeRef = new ParameterizedTypeReference<>() {};
        List<CaseTypeActionDto> response = restHelper.get(serviceBaseURL,
            String.format("/caseType/%s/actions", caseType), typeRef);

        log.info("Received {} CaseTypeActions for caseType {}", response.size(), caseType);
        return response;
    }

    @Cacheable(value = "InfoClientGetExemptionDatesForType", unless = "#result == null", key = "#caseType")
    public Set<LocalDate> getExemptionDatesForType(String caseType) {
        log.debug("Requesting exemption dates for case type: {}", caseType);

        ParameterizedTypeReference<Set<LocalDate>> typeRef = new ParameterizedTypeReference<>() {};
        Set<LocalDate> exemptionDates = restHelper.get(serviceBaseURL,
            String.format("/caseType/%s/exemptionDates", caseType), typeRef);

        log.info("Received {} exemption dates", exemptionDates.size(), caseType);
        return exemptionDates;
    }

}
