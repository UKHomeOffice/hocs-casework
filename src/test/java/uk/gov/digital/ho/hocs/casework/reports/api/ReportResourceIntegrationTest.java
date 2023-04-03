package uk.gov.digital.ho.hocs.casework.reports.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.StringStartsWith;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportMetadataDto;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;
import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.ExemptionDatesAgeAdjustmentLookup;
import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.StageNameValueMapper;
import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.TeamNameValueMapper;
import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.UserNameValueMapper;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withRawStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:reports/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:reports/afterTest.sql",
     config = @SqlConfig(transactionMode = ISOLATED),
     executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles({ "local", "integration", "reports" })
public class ReportResourceIntegrationTest {

    private MockRestServiceServer mockInfoService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    int port;

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private UserNameValueMapper userMapper;

    @Autowired
    private TeamNameValueMapper teamMapper;

    @Autowired
    private StageNameValueMapper stageMapper;

    @Autowired
    private ExemptionDatesAgeAdjustmentLookup exemptionDatesLookup;

    @Autowired
    CacheManager cacheManager;

    @Before
    public void setup() {
        mockInfoService = buildMockService(restTemplate);
    }

    @Test
    public void whenIRequestAvailableReports_thenIShouldSeeTheOpenCasesReportInTheResponse() {
        ResponseEntity<List<ReportMetadataDto>> result = getReportListResponse();
        assertThat(result.getBody()).anyMatch(meta -> Objects.equals(meta.getSlug(), "open-cases"));
    }

    @Test
    public void whenIRequestCOMPOpenCasesReport_thenIOnlySeeIncompleteCOMPCasesInTheResult() throws IOException {
        setupInfoServiceMocks();

        JSONObject report = getCOMPOpenCasesReportResponseJson();

        ReportMetadataDto meta = extractMetadata(report);
        List<OpenCasesRow> data = extractData(report);

        assertThat(report.getString("case_type")).isEqualTo("COMP");

        assertThat(meta.getSlug()).isEqualTo("open-cases");
        assertThat(data.stream().map(OpenCasesRow::getCaseReference)).contains(
            "COMP/123450/23", "COMP/123453/23", "COMP/123454/23", "COMP/123455/23");
        assertThat(data.stream().map(OpenCasesRow::getCaseReference)).doesNotContain(
            "COMP/123451/23", // Completed
            "COMP2/123452/23" // COMP2 case
                                                                                    );
    }

    @Test
    public void whenIRequestCOMPOpenCasesReport_thenTeamUUIDsAreMappedToNamesWherePossible() throws IOException {
        setupInfoServiceMocks();

        JSONObject report = getCOMPOpenCasesReportResponseJson();
        List<OpenCasesRow> data = extractData(report);

        assertThat(data.stream().map(OpenCasesRow::getTeamName)).contains(
            "Test team one", "Test team two", "3525de88-a684-4d33-9cca-14ead39acf19");
        assertThat(data.stream().map(OpenCasesRow::getTeamName)).doesNotContain(
            "e4ed4136-6dde-448e-a810-94cc2fc18097", "6761deeb-950a-4735-8b4c-6c6708d3153f");
    }

    @Test
    public void whenIRequestCOMPOpenCasesReport_thenUserUUIDsAreMappedToNamesWherePossible() throws IOException {
        setupInfoServiceMocks();

        JSONObject report = getCOMPOpenCasesReportResponseJson();
        List<OpenCasesRow> data = extractData(report);

        assertThat(data.stream().map(OpenCasesRow::getUserName))
            // Name two is on a completed case
            .contains(
                "FirstNameOne SecondNameOne", "FirstNameThree SecondNameThree", "8da250c9-1028-4b08-9c8b-fb413da7b287");
        assertThat(data.stream().map(OpenCasesRow::getUserName)).doesNotContain(
            "ed6e908d-ecb1-4d40-861f-742ab21b506c", "3eabb8f7-8084-4ab1-aefe-cbd6f3063fbb");
    }

    @Test
    public void whenIRequestCOMPOpenCasesReport_thenStageTypesAreMappedToNamesWherePossible() throws IOException {
        setupInfoServiceMocks();

        JSONObject report = getCOMPOpenCasesReportResponseJson();
        List<OpenCasesRow> data = extractData(report);

        assertThat(data.stream().map(OpenCasesRow::getStageName))
            // Stage two is on a completed case
            .contains("Test stage type one", "Test stage type three", "TEST_STAGE_0");
        assertThat(data.stream().map(OpenCasesRow::getStageName)).doesNotContain("TEST_STAGE_1", "TEST_STAGE_2");
    }

    @Test
    public void whenIRequestCOMPOpenCasesReport_thenAgesExcludeWeekendsAndExclusionDates() throws IOException {
        setupInfoServiceMocks();

        JSONObject report = getCOMPOpenCasesReportResponseJson();
        Map<String, Integer> ageByCaseRef = extractData(report)
            .stream()
            .collect(Collectors.toMap(OpenCasesRow::getCaseReference, OpenCasesRow::getAge));

        assertThat(ageByCaseRef.get("COMP/123450/23")).isEqualTo(5);
        assertThat(ageByCaseRef.get("COMP/123453/23")).isEqualTo(19); // There are exemption dates 2 weeks ago and
        assertThat(ageByCaseRef.get("COMP/123454/23")).isEqualTo(23); // 5 weeks ago so case from 4 weeks ago should
        assertThat(ageByCaseRef.get("COMP/123455/23")).isEqualTo(28); // have 1 day, 5 and 6 weeks two days.
    }

    @Test
    public void whenIRequestCOMPOpenCasesReport_thenTheOutsideServiceStandardFlagMatchesTheCaseDeadline() throws IOException {
        setupInfoServiceMocks();

        JSONObject report = getCOMPOpenCasesReportResponseJson();
        List<OpenCasesRow> data = extractData(report);

        assertThat(data).allSatisfy(row -> assertThat(row.getOutsideServiceStandard()).isEqualTo(
            row.getCaseDeadline().isBefore(LocalDate.now())));
    }

    @Test
    public void whenIRequestAnOpenCasesReportForAnUnsupportedCaseType_thenIExpectANotFoundResponse() {
        ResponseEntity<String> response = getOpenCasesReportResponseEntity(CaseType.IEDET);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(
            "The \"Open cases report\" report does not support the \"IEDET\" case type");
    }

    private List<OpenCasesRow> extractData(JSONObject report) throws JsonProcessingException {
        return mapper.readerForListOf(OpenCasesRow.class).readValue(report.getJSONArray("data").toString());
    }

    private ReportMetadataDto extractMetadata(JSONObject report) throws JsonProcessingException {
        return mapper.readValue(report.getJSONObject("metadata").toString(), ReportMetadataDto.class);
    }

    void setupInfoServiceMocks() throws JsonProcessingException {
        Set<TeamDto> cachedTeamDtos = Set.of(
            new TeamDto("Test team one", UUID.fromString("6761deeb-950a-4735-8b4c-6c6708d3153f"), true, Set.of()));
        mockInfoService
            .expect(requestTo("http://localhost:8085/team"))
            .andExpect(method(GET))
            .andRespond(withSuccess(mapper.writeValueAsString(cachedTeamDtos), MediaType.APPLICATION_JSON));

        TeamDto uncachedTeamDto = new TeamDto(
            "Test team two", UUID.fromString("e4ed4136-6dde-448e-a810-94cc2fc18097"), true, Set.of());
        mockInfoService
            .expect(requestTo("http://localhost:8085/team/%s".formatted(uncachedTeamDto.getUuid())))
            .andExpect(method(GET))
            .andRespond(withSuccess(mapper.writeValueAsString(uncachedTeamDto), MediaType.APPLICATION_JSON));
        mockInfoService
            .expect(requestTo(new StringStartsWith("http://localhost:8085/team")))
            .andExpect(method(GET))
            .andRespond(withRawStatus(404));

        Set<UserDto> cachedUserDtos = Set.of(new UserDto(
                "ed6e908d-ecb1-4d40-861f-742ab21b506c", "username1", "FirstNameOne", "SecondNameOne", "one@example.org"),
            new UserDto("212db2a6-c61d-47cc-98b8-09e0cd19a755", "username2", "FirstNameTwo", "SecondNameTwo",
                "two@example.org"
            )
                                            );
        mockInfoService
            .expect(requestTo("http://localhost:8085/users"))
            .andExpect(method(GET))
            .andRespond(withSuccess(mapper.writeValueAsString(cachedUserDtos), MediaType.APPLICATION_JSON));

        UserDto uncachedUserDto = new UserDto("3eabb8f7-8084-4ab1-aefe-cbd6f3063fbb", "username3", "FirstNameThree",
            "SecondNameThree", "three@example.org"
        );
        mockInfoService
            .expect(requestTo("http://localhost:8085/user/%s".formatted(uncachedUserDto.getId())))
            .andExpect(method(GET))
            .andRespond(withSuccess(mapper.writeValueAsString(uncachedUserDto), MediaType.APPLICATION_JSON));
        mockInfoService
            .expect(requestTo(new StringStartsWith("http://localhost:8085/user/")))
            .andExpect(method(GET))
            .andRespond(withRawStatus(404));

        Set<StageTypeDto> cachedStageTypeDtos = Set.of(
            new StageTypeDto("Test stage type one", "T1", "TEST_STAGE_1", 20, 0, 0),
            new StageTypeDto("Test stage type two", "T2", "TEST_STAGE_2", 20, 0, 0)
                                                      );
        mockInfoService
            .expect(requestTo("http://localhost:8085/stageType"))
            .andExpect(method(GET))
            .andRespond(withSuccess(mapper.writeValueAsString(cachedStageTypeDtos), MediaType.APPLICATION_JSON));

        StageTypeDto uncachedStageTypeDto = new StageTypeDto("Test stage type three", "T3", "TEST_STAGE_3", 20, 0, 1);
        mockInfoService
            .expect(requestTo("http://localhost:8085/stageType/type/%s".formatted("TEST_STAGE_3")))
            .andExpect(method(GET))
            .andRespond(withSuccess(mapper.writeValueAsString(uncachedStageTypeDto), MediaType.APPLICATION_JSON));
        mockInfoService
            .expect(requestTo(new StringStartsWith("http://localhost:8085/stageType/type/")))
            .andExpect(method(GET))
            .andRespond(withRawStatus(404));

        // Report view uses now() in the database, so we need to use relative dates in tests rather than using a fixed Clock.
        String exemptionDates = mapper.writeValueAsString(
            Set.of(LocalDate.now().minusWeeks(2).plusDays(1).format(DateTimeFormatter.ISO_DATE),
                LocalDate.now().minusWeeks(5).plusDays(1).format(DateTimeFormatter.ISO_DATE)
                  ));
        List<CaseDataType> caseTypes = List.of(new CaseDataType("COMP", "COMP", "COMP", null, 0, 0),
            new CaseDataType("COMP2", "COMP2", "COMP2", null, 0, 0),
            new CaseDataType("MPAM", "MPAM", "MPAM", null, 0, 0),
            new CaseDataType("IEDET", "IEDET", "IEDET", null, 0, 0)
                                              );

        mockInfoService
            .expect(requestTo("http://localhost:8085/caseType?initialCaseType=false"))
            .andExpect(method(GET))
            .andRespond(withSuccess(mapper.writeValueAsString(caseTypes), MediaType.APPLICATION_JSON));

        caseTypes.forEach(ct -> mockInfoService
            .expect(requestTo("http://localhost:8085/caseType/%s/exemptionDates".formatted(ct.getDisplayCode())))
            .andExpect(method(GET))
            .andRespond(withSuccess(exemptionDates, MediaType.APPLICATION_JSON)));

        // Values are cached during bootstrap before mocks are set up.
        Stream
            .of("InfoClientGetUser", "InfoClientGetUsers", "InfoClientGetTeamForUUID", "InfoClientGetTeams",
                "InfoClientGetStageTypeByTypeString", "InfoClientGetAllStages", "InfoClientGetExemptionDatesForType"
               )
            .forEach(name -> Optional.ofNullable(cacheManager.getCache(name)).ifPresent(Cache::clear));

        userMapper.refreshCache();
        teamMapper.refreshCache();
        stageMapper.refreshCache();
        exemptionDatesLookup.refreshCache();
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private ResponseEntity<List<ReportMetadataDto>> getReportListResponse() {
        return testRestTemplate.exchange(getBasePath() + "/report", GET, null, new ParameterizedTypeReference<>() {});
    }

    private JSONObject getCOMPOpenCasesReportResponseJson() {
        ResponseEntity<String> response = getOpenCasesReportResponseEntity(CaseType.COMP);

        return new JSONObject(response.getBody());
    }

    private ResponseEntity<String> getOpenCasesReportResponseEntity(CaseType caseType) {
        return testRestTemplate.exchange(
            getBasePath() + "/report/%s/open-cases".formatted(caseType), GET, null, String.class);
    }

}
