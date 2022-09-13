package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.api.utils.DateUtils;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:action/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:action/afterTest.sql",
     config = @SqlConfig(transactionMode = ISOLATED),
     executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles({ "local", "integration" })
public class CaseActionServiceIntegrationTest {

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @LocalServerPort
    int port;

    private MockRestServiceServer mockInfoService = null;

    private static final UUID CASE_ID = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");

    private static final UUID CASE_ID_NON_EXISTING = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a2");

    private static final UUID ACTION_ENTITY_ID = UUID.fromString("d159c936-b727-464e-a0ed-b63134fe0b37");

    private static final UUID ACTION_ENTITY_EXTERNAL_INTEREST_ID = UUID.fromString(
        "7a4ce582-e698-462f-9024-d33de6b85983");

    private static final UUID ACTION_ENTITY_ID_NON_EXISTING = UUID.fromString("2a533f69-a70e-4954-9a2f-af674e05be0d");

    private static final UUID EXTENSION_CASE_TYPE_ACTION_ID = UUID.fromString("a68b0ff2-a9fc-4312-8b28-504523d04026");

    private static final UUID EXTERNAL_INTEREST_CASE_TYPE_ACTION_ID = UUID.fromString(
        "1e549055-9115-438a-9c21-29c191bcc58b");

    private static final UUID APPEAL_CASE_TYPE_ACTION_ID = UUID.fromString("326eddb3-ba64-4253-ad39-916ccbb59f4e");

    private static final UUID APPEAL_CASE_TYPE_ACTION_ID_ALT_ID = UUID.fromString(
        "426eddb3-ba64-4253-ad39-916ccbb59f4e");

    private static final UUID NON_EXISTENT_CASE_TYPE_ACTION_ID = UUID.fromString(
        "c3d53309-3be8-4bad-8d9b-b2f7107f6923");

    public static final String EXTENSION_REASON_1_SIMPLE_NAME = "EXTENSION_REASON_1_SIMPLE_NAME";

    public static final String EXTENSION_REASON_2_SIMPLE_NAME = "EXTENSION_REASON_2_SIMPLE_NAME";

    public static final UUID SUSPEND_CASE_TYPE_ACTION_ID = UUID.fromString("7d3c643e-7ccc-4404-8ec0-271ab803a233");

    public static final UUID SUSPEND_CASE_TYPE_ACTION_ID_ALT_ID = UUID.fromString(
        "6f011f21-6b8c-40a9-aff1-56e97029c445");

    private static final UUID SUSPENSION_ENTITY_ID = UUID.fromString("745a149d-b9fb-47b5-bfcd-a192f1bce48e");

    private static final UUID SUSPENSION_ENTITY_ID_NON_EXISTING = UUID.fromString(
        "d9f8359c-797e-4a44-940b-0d5b821ae842");

    private static final UUID MOCK_STAGE_UUID = UUID.fromString("ed648084-5ed8-4e3f-866c-ff3c5d9790c8");

    public static final uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto EXTENSION_REASON_1 = new uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto(
        EXTENSION_REASON_1_SIMPLE_NAME, Map.of("title", "Extension Reason 1"));

    public static final uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto EXTENSION_REASON_2 = new uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto(
        EXTENSION_REASON_1_SIMPLE_NAME, Map.of("title", "Extension Reason 2"));

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_EXTENSION_DTO = new CaseTypeActionDto(
        EXTENSION_CASE_TYPE_ACTION_ID, UUID.randomUUID(), "CASE_TYPE", "EXTENSION", "PIT_EXTENSION", "PIT Extension", 1,
        10, true, "{}");

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_EXTERNAL_INTEREST_DTO = new CaseTypeActionDto(
        EXTENSION_CASE_TYPE_ACTION_ID, UUID.randomUUID(), "CASE_TYPE", "EXTERNAL_INTEREST", "PIT_EXTENSION",
        "External Interest", 1, 10, true, "{}");

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_APPEAL_DTO = new CaseTypeActionDto(
        APPEAL_CASE_TYPE_ACTION_ID, UUID.randomUUID(), "CASE_TYPE", "APPEAL", "PIT_EXTENSION", "APPEAL 1", 1, 10, true,
        "{}");

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_APPEAL_DTO_ALT_ID = new CaseTypeActionDto(
        APPEAL_CASE_TYPE_ACTION_ID_ALT_ID, UUID.randomUUID(), "CASE_TYPE", "APPEAL", "PIT_EXTENSION", "APPEAL 1", 1, 10,
        true, "{}");

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_SUSPEND_DTO = new CaseTypeActionDto(
        SUSPEND_CASE_TYPE_ACTION_ID, UUID.randomUUID(), "CASE_TYPE", "SUSPEND", "SUSPEND_TYPE_1", "SUSPEND 1", 1, 10,
        true, "{}");

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_SUSPEND_DTO_ALT_ID = new CaseTypeActionDto(
        SUSPEND_CASE_TYPE_ACTION_ID_ALT_ID, UUID.randomUUID(), "CASE_TYPE", "SUSPEND", "SUSPEND_TYPE_2", "SUSPEND 1", 1,
        10, true, "{}");

    private final Set<LocalDate> exemptionDates = Set.of(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-04-10"),
        LocalDate.parse("2020-04-13"), LocalDate.parse("2020-05-08"), LocalDate.parse("2020-05-25"),
        LocalDate.parse("2020-08-31"), LocalDate.parse("2020-12-25"), LocalDate.parse("2020-12-28"));

    private static final StageTypeDto STAGE_TYPE = new StageTypeDto("Some Stage", "9999", "SOME_STAGE", 20, 18, 1);

    private static final CaseDataType CASE_DATA_TYPE = CaseDataTypeFactory.from("TEST", "a1");

    private static final CaseDataType CASE_DATA_TYPE2 = CaseDataTypeFactory.from("TEST", "a2");

    @Before
    public void setUp() throws JsonProcessingException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService.expect(manyTimes(), requestTo("http://localhost:8085/caseType/shortCode/a1")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo("http://localhost:8085/caseType/shortCode/a2")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE2), MediaType.APPLICATION_JSON));
        mockInfoService.expect(
            requestTo("http://localhost:8085/caseType/FOI/actions/" + EXTENSION_CASE_TYPE_ACTION_ID)).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_EXTENSION_DTO), MediaType.APPLICATION_JSON));
        mockInfoService.expect(
            requestTo("http://localhost:8085/caseType/FOI/actions/" + EXTERNAL_INTEREST_CASE_TYPE_ACTION_ID)).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_EXTERNAL_INTEREST_DTO),
            MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/FOI/actions/" + NON_EXISTENT_CASE_TYPE_ACTION_ID)).andExpect(
            method(GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/FOI/actions/" + APPEAL_CASE_TYPE_ACTION_ID)).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_APPEAL_DTO), MediaType.APPLICATION_JSON));
        mockInfoService.expect(
            requestTo("http://localhost:8085/caseType/FOI/deadline?received=" + LocalDate.now() + "&days=8")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(8).toString()), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo(
            "http://localhost:8085/caseType/FOI/deadlineWarning?received=" + LocalDate.now() + "&days=8")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(6).toString()), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo(
            "http://localhost:8085/caseType/TEST/deadline?received=" + LocalDate.now() + "&days=8")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(8).toString()), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo(
            "http://localhost:8085/caseType/TEST/deadlineWarning?received=" + LocalDate.now() + "&days=8")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(6).toString()), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo(matchesPattern(
            "http://localhost:8085/stageType/INITIAL_DRAFT/deadline\\?received=2018-01-01&caseDeadline=\\d{4}-\\d{2}-\\d{2}&overrideSla=true"))).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(8).toString()), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo(
            "http://localhost:8085/stageType/INITIAL_DRAFT/deadlineWarning?received=2018-01-01&caseDeadlineWarning=" + DateUtils.addWorkingDays(
                LocalDate.now(), 3, Set.of()) + "&overrideSla=true")).andExpect(method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(6).toString()), MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(), requestTo("http://localhost:8085/caseType/TEST/actions")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(
            List.of(MOCK_CASE_TYPE_ACTION_EXTENSION_DTO, MOCK_CASE_TYPE_ACTION_APPEAL_DTO,
                MOCK_CASE_TYPE_ACTION_EXTERNAL_INTEREST_DTO, MOCK_CASE_TYPE_ACTION_SUSPEND_DTO,
                MOCK_CASE_TYPE_ACTION_SUSPEND_DTO_ALT_ID)), MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/326eddb3-ba64-4253-ad39-916ccbb59f4e")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_APPEAL_DTO), MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/426eddb3-ba64-4253-ad39-916ccbb59f4e")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_APPEAL_DTO_ALT_ID),
            MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/a68b0ff2-a9fc-4312-8b28-504523d04026")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_EXTENSION_DTO), MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/1e549055-9115-438a-9c21-29c191bcc58b")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_EXTERNAL_INTEREST_DTO),
            MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/c3d53309-3be8-4bad-8d9b-b2f7107f6923")).andExpect(
            method(GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/" + SUSPEND_CASE_TYPE_ACTION_ID_ALT_ID)).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_SUSPEND_DTO_ALT_ID),
            MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/" + NON_EXISTENT_CASE_TYPE_ACTION_ID)).andExpect(
            method(GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/actions/" + SUSPEND_CASE_TYPE_ACTION_ID)).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_SUSPEND_DTO), MediaType.APPLICATION_JSON));
        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/caseType/TEST/deadline/2018-01-29/remainingDays")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(10), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));
        mockInfoService.expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));

        final EntityDto test_interested_party = new EntityDto("TEST_INTERESTED_PARTY", UUID.randomUUID().toString(),
            "{}");

        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/entity/simpleName/TEST_INTERESTED_PARTY")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(test_interested_party), MediaType.APPLICATION_JSON));

        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/entity/simpleName/EXTENSION_REASON_1_SIMPLE_NAME")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(EXTENSION_REASON_1), MediaType.APPLICATION_JSON));

        mockInfoService.expect(manyTimes(),
            requestTo("http://localhost:8085/entity/simpleName/EXTENSION_REASON_2_SIMPLE_NAME")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(EXTENSION_REASON_2), MediaType.APPLICATION_JSON));

        mockInfoService.expect(manyTimes(), requestTo("http://localhost:8085/caseType/type/TEST")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(new CaseDataType(null, null, null, null, 20, 15)),
                MediaType.APPLICATION_JSON));

        mockInfoService.expect(manyTimes(), requestTo("http://localhost:8085/stages/caseType/TEST")).andExpect(
            method(GET)).andRespond(
            withSuccess(mapper.writeValueAsString(Set.of(STAGE_TYPE)), MediaType.APPLICATION_JSON));
    }

    // EXTENSIONS - CREATE

    @Test
    public void extensionCreate_shouldCreateDeadlineExtension() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "PIT Extension";

        ActionDataDto actionDataDto = new ActionDataDeadlineExtensionInboundDto(null, EXTENSION_CASE_TYPE_ACTION_ID,
            "PIT_EXTENSION", caseTypeActionLabel, "TODAY", 8, "NOTE",
            EXTENSION_REASON_1_SIMPLE_NAME + "," + EXTENSION_REASON_2_SIMPLE_NAME);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<GetCaseReferenceResponse> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/extension", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), GetCaseReferenceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void extensionCreate_shouldReturn404whenNoActionWithID() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "PIT Extension";

        ActionDataDto actionDataDto = new ActionDataDeadlineExtensionInboundDto(null, NON_EXISTENT_CASE_TYPE_ACTION_ID,
            "PIT_EXTENSION", caseTypeActionLabel, "TODAY", 8, "NOTE",
            EXTENSION_REASON_1_SIMPLE_NAME + "," + EXTENSION_REASON_2_SIMPLE_NAME);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/extension", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void extensionCreate_shouldReturn404whenNoCaseData() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "PIT Extension";

        ActionDataDto actionDataDto = new ActionDataDeadlineExtensionInboundDto(null, EXTENSION_CASE_TYPE_ACTION_ID,
            "PIT_EXTENSION", caseTypeActionLabel, "TODAY", 8, "NOTE",
            EXTENSION_REASON_1_SIMPLE_NAME + "," + EXTENSION_REASON_2_SIMPLE_NAME);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID_NON_EXISTING + "/stage/" + stageUUID + "/actions/extension", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // EXTERNAL INTEREST - CREATE
    @Test
    public void createExternalInterest_shouldCreateExternalInterest() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 2);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "External Interest";

        ActionDataDto actionDataDto = new ActionDataExternalInterestInboundDto(null,
            EXTERNAL_INTEREST_CASE_TYPE_ACTION_ID, "TEST_EXTERNAL_INTEREST", caseTypeActionLabel,
            "TEST_INTERESTED_PARTY", "interest details");

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<GetCaseReferenceResponse> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/interest", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), GetCaseReferenceResponse.class);

        ResponseEntity<CaseActionDataResponseDto> allActionsResponse = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/actions", GET, new HttpEntity<>(createValidAuthHeaders()),
            CaseActionDataResponseDto.class);

        assertThat(Objects.requireNonNull(allActionsResponse.getBody()).getCaseActionData().get(
            "recordInterest").size()).isEqualTo(2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void createExternalInterest_shouldReturn404whenNoCaseData() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "External Interest";

        ActionDataDto actionDataDto = new ActionDataExternalInterestInboundDto(ACTION_ENTITY_EXTERNAL_INTEREST_ID,
            EXTERNAL_INTEREST_CASE_TYPE_ACTION_ID, "TEST_EXTERNAL_INTEREST", caseTypeActionLabel,
            "TEST_INTERESTED_PARTY", "interest details");
        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID_NON_EXISTING + "/stage/" + stageUUID + "/actions/interest", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateExternalInterest_shouldReturn200WhenExternalInterestUpdated() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "External Interest";

        ActionDataDto actionDataDto = new ActionDataExternalInterestInboundDto(ACTION_ENTITY_EXTERNAL_INTEREST_ID,
            EXTERNAL_INTEREST_CASE_TYPE_ACTION_ID, "TEST_EXTERNAL_INTEREST", caseTypeActionLabel,
            "TEST_INTERESTED_PARTY", "interest details");

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/interest/" + ACTION_ENTITY_EXTERNAL_INTEREST_ID,
            PUT, new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateExternalInterestUpdate_shouldReturn404WhenExternalInterestEntityDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "External Interest";

        ActionDataDto actionDataDto = new ActionDataExternalInterestInboundDto(null,
            EXTERNAL_INTEREST_CASE_TYPE_ACTION_ID, "TEST_EXTERNAL_INTEREST", caseTypeActionLabel,
            "TEST_INTERESTED_PARTY", "interest details");

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/interest/" + ACTION_ENTITY_ID_NON_EXISTING,
            PUT, new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // APPEALS - CREATE

    @Test
    public void appealCreate_shouldReturn404WhenActionDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";

        ActionDataDto actionDataDto = new ActionDataAppealDto(null, NON_EXISTENT_CASE_TYPE_ACTION_ID, "TEST_APPEAL",
            caseTypeActionLabel, null, null, null, null, null, "{}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/appeal", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void appealCreate_shouldReturn404WhenCaseIdDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";

        ActionDataDto actionDataDto = new ActionDataAppealDto(null, APPEAL_CASE_TYPE_ACTION_ID, caseTypeActionLabel,
            "TEST_APPEAL", null, null, null, null, null, "{}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID_NON_EXISTING + "/stage/" + stageUUID + "/actions/appeal", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void appealCreate_shouldReturn200AndCreateAppeal() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";

        ActionDataDto actionDataDto = new ActionDataAppealDto(null, APPEAL_CASE_TYPE_ACTION_ID_ALT_ID,
            caseTypeActionLabel, "TEST_APPEAL", "Pending", null, null, null, null, "{}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/appeal", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void appealCreate_shouldReturn200ForBothCasesAndCreateAppeal() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 2);
        UUID stageUUID1 = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";
        UUID caseUUID2 = UUID.fromString("bb915b78-6977-42db-b343-0915a7f412a1");

        ActionDataDto actionDataDto = new ActionDataAppealDto(null, APPEAL_CASE_TYPE_ACTION_ID_ALT_ID, "TEST_APPEAL",
            caseTypeActionLabel, "Pending", null, null, null, null, "{}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response1 = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID1 + "/actions/appeal", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        ResponseEntity<Void> response2 = testRestTemplate.exchange(
            getBasePath() + "/case/" + caseUUID2 + "/stage/" + stageUUID1 + "/actions/appeal", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // APPEALS - UPDATE

    @Test
    public void appealUpdate_shouldReturn404WhenAppealEntityDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";

        ActionDataDto actionDataDto = new ActionDataAppealDto(ACTION_ENTITY_ID_NON_EXISTING, APPEAL_CASE_TYPE_ACTION_ID,
            "TEST_APPEAL", caseTypeActionLabel, null, null, null, null, null, "{}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/appeal/" + ACTION_ENTITY_ID_NON_EXISTING,
            PUT, new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void appealUpdate_shouldReturn404WhenCaseDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";

        ActionDataDto actionDataDto = new ActionDataAppealDto(ACTION_ENTITY_ID, APPEAL_CASE_TYPE_ACTION_ID,
            "TEST_APPEAL", caseTypeActionLabel, null, null, null, null, null, "{}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID_NON_EXISTING + "/stage/" + stageUUID + "/actions/appeal/" + ACTION_ENTITY_ID,
            PUT, new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void appealUpdate_shouldReturn404WhenAppealCaseActionTypeDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";

        ActionDataDto actionDataDto = new ActionDataAppealDto(ACTION_ENTITY_ID, NON_EXISTENT_CASE_TYPE_ACTION_ID,
            "TEST_APPEAL", caseTypeActionLabel, null, null, null, null, null, "{}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/appeal/" + ACTION_ENTITY_ID, PUT,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void appealUpdate_shouldReturn200WhenAppealUpdated() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        UUID stageUUID = UUID.randomUUID();
        String caseTypeActionLabel = "IR Appeal";

        ActionDataDto actionDataDto = new ActionDataAppealDto(ACTION_ENTITY_ID, APPEAL_CASE_TYPE_ACTION_ID,
            "TEST_APPEAL", caseTypeActionLabel, "Complete", null, null, null, null, "{\"updated\":\"true\"}", null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + stageUUID + "/actions/appeal/" + ACTION_ENTITY_ID, PUT,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // - SUSPENSIONS
    @Test
    public void testCreateCaseSuspensionShouldReturn404WhenCaseDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        ActionDataSuspendDto actionDataDto = new ActionDataSuspendDto(null, SUSPEND_CASE_TYPE_ACTION_ID, "SUSPEND",
            "SUSPEND", LocalDate.now(), null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID_NON_EXISTING + "/stage/" + MOCK_STAGE_UUID + "/actions/suspension", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testCreateCaseSuspensionShouldReturn404WhenCaseActionTypeNotFound() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        ActionDataSuspendDto actionDataDto = new ActionDataSuspendDto(null, NON_EXISTENT_CASE_TYPE_ACTION_ID, "SUSPEND",
            "SUSPEND", LocalDate.now(), null);

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + MOCK_STAGE_UUID + "/actions/suspension", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testCreateCaseSuspensionShouldReturn400WhenMaxSuspensionsForTypeExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        ActionDataSuspendDto actionDataDto = new ActionDataSuspendDto(null, SUSPEND_CASE_TYPE_ACTION_ID_ALT_ID,
            "SUSPEND", "SUSPEND", LocalDate.now(), null);

        String requestBody = mapper.writeValueAsString(actionDataDto);

        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + MOCK_STAGE_UUID + "/actions/suspension", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCreateCaseSuspensionShouldReturn200AndCreateSuspension() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        ActionDataSuspendDto actionDataDto = new ActionDataSuspendDto(null, SUSPEND_CASE_TYPE_ACTION_ID, "SUSPEND",
            "SUSPEND", LocalDate.now(), null);

        String requestBody = mapper.writeValueAsString(actionDataDto);

        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + MOCK_STAGE_UUID + "/actions/suspension", POST,
            new HttpEntity<>(requestBody, createValidAuthHeaders()), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testDeleteCaseSuspensionShouldReturn404WhenCaseDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID_NON_EXISTING + "/stage/" + MOCK_STAGE_UUID + "/actions/suspension/" + SUSPENSION_ENTITY_ID,
            PUT, new HttpEntity<>(createValidAuthHeaders()), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeleteCaseSuspensionShouldReturn404WhenSuspensionDoesNotExist() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);

        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + MOCK_STAGE_UUID + "/actions/suspension/" + SUSPENSION_ENTITY_ID_NON_EXISTING,
            PUT, new HttpEntity<>(createValidAuthHeaders()), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeleteCaseSuspensionShouldReturn200AndCreateSuspension() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        ResponseEntity<Void> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/stage/" + MOCK_STAGE_UUID + "/actions/suspension/" + SUSPENSION_ENTITY_ID,
            PUT, new HttpEntity<>(createValidAuthHeaders()), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getAllCaseActionsByCaseId_shouldReturn200AndCaseActionDataResponseDto() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel(), 1);
        ResponseEntity<CaseActionDataResponseDto> response = testRestTemplate.exchange(
            getBasePath() + "/case/" + CASE_ID + "/actions", GET, new HttpEntity<>(createValidAuthHeaders()),
            CaseActionDataResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCaseActionData().containsKey("appeals")).isTrue();
        assertThat(response.getBody().getCaseActionData().containsKey("extensions")).isTrue();

    }

    // -------- HELPERS ----------

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "a.person@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    private void setupMockTeams(String caseType,
                                int permission,
                                int expectedCallsNumber) throws JsonProcessingException {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true,
            permissionDtos);
        teamDtos.add(teamDto);

        mockInfoService.expect(times(expectedCallsNumber), requestTo("http://localhost:8085/team")).andExpect(
            method(GET)).andRespond(withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));
    }

}
