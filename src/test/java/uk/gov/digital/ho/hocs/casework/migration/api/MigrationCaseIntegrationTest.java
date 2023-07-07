package uk.gov.digital.ho.hocs.casework.migration.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.text.ValuePrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.UpdateCaseDataRequest;
import uk.gov.digital.ho.hocs.casework.migration.client.auditclient.MigrationAuditClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:migration/case/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:migration/case/afterTest.sql",
     config = @SqlConfig(transactionMode = ISOLATED),
     executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles({ "local", "integration" })
public class MigrationCaseIntegrationTest {

    public static final LocalDateTime UPDATE_EVENT_TIMESTAMP = LocalDateTime.parse("2021-01-01T00:00:00.000");

    @LocalServerPort
    int port;

    String CLOSED_MIGRATED_REFERENCE = "ClosedMigratedRef123";

    UUID CLOSED_CASE_UUID = UUID.fromString("85492dfa-6642-4eee-9513-700b4bf4de8b");

    UUID CLOSED_STAGE_UUID = UUID.fromString("c0b3dd14-f59c-4bd7-bb8f-870d17c8a54a");

    String OPEN_MIGRATED_REFERENCE = "OpenMigratedRef123";

    UUID OPEN_CASE_UUID = UUID.fromString("b81482e9-4822-4792-9773-2d4a22b923e0");

    UUID OPEN_STAGE_UUID = UUID.fromString("9658f450-0786-4a66-8dea-23adb7484795");

    String MISSING_STAGE_MIGRATED_REFERENCE = "MissingStageMigratedRef123";

    String MISSING_STAGE_CASE_UUID = "e69a1e91-885e-4bf4-a2d4-4af90cd8e475";

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @MockBean
    MigrationAuditClient migrationAuditClient;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void whenDataIsPostedToAClosedMigrationCase_anAuditEventIsGeneratedWithTheNewData() throws JsonProcessingException {
        var request = new UpdateCaseDataRequest(UPDATE_EVENT_TIMESTAMP,
            Map.of("ExistingField1", "UpdatedValue", "AdditionalField", "NewValue")
        );
        ResponseEntity<Void> result = testRestTemplate.exchange(
            "%s/migrate/case/%s/case-data".formatted(getBasePath(), CLOSED_MIGRATED_REFERENCE), POST,
            new HttpEntity<>(mapper.writeValueAsString(request), createValidAuthHeaders()), Void.class
                                                               );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(migrationAuditClient, times(1)).updateCaseAudit(argThat(new MatchesCaseData(CLOSED_CASE_UUID,
            Map.of("ExistingField1", "UpdatedValue", "ExistingField2", "ExistingValue2", "AdditionalField", "NewValue")
        )), eq(CLOSED_STAGE_UUID), eq(UPDATE_EVENT_TIMESTAMP));
    }

    @Test
    public void whenDataIsPostedToAnOpenMigrationCase_anAuditEventIsGeneratedWithTheNewData() throws JsonProcessingException {
        var request = new UpdateCaseDataRequest(UPDATE_EVENT_TIMESTAMP,
            Map.of("ExistingField1", "UpdatedValue", "AdditionalField", "NewValue")
        );
        ResponseEntity<Void> result = testRestTemplate.exchange(
            "%s/migrate/case/%s/case-data".formatted(getBasePath(), OPEN_MIGRATED_REFERENCE), POST,
            new HttpEntity<>(mapper.writeValueAsString(request), createValidAuthHeaders()), Void.class
                                                               );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(migrationAuditClient, times(1)).updateCaseAudit(argThat(new MatchesCaseData(OPEN_CASE_UUID,
            Map.of("ExistingField1", "UpdatedValue", "ExistingField2", "ExistingValue2", "AdditionalField", "NewValue")
        )), eq(OPEN_STAGE_UUID), eq(UPDATE_EVENT_TIMESTAMP));
    }

    @Test
    public void whenDataIsPostedToAnMissingMigratedReference_a404ResponseIsReturned() throws JsonProcessingException {
        var request = new UpdateCaseDataRequest(UPDATE_EVENT_TIMESTAMP,
            Map.of("ExistingField1", "UpdatedValue", "AdditionalField", "NewValue")
        );
        ResponseEntity<String> result = testRestTemplate.exchange(
            "%s/migrate/case/MissingRef/case-data".formatted(getBasePath()), POST,
            new HttpEntity<>(mapper.writeValueAsString(request), createValidAuthHeaders()), String.class
                                                                 );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo("Migrated Case: MissingRef, not found!");
        verifyNoInteractions(migrationAuditClient);
    }

    @Test
    public void whenDataIsPostedToAnMigratedCaseWithoutAValidStage_a400ResponseIsReturned() throws JsonProcessingException {
        var request = new UpdateCaseDataRequest(UPDATE_EVENT_TIMESTAMP,
            Map.of("ExistingField1", "UpdatedValue", "AdditionalField", "NewValue")
        );
        ResponseEntity<String> result = testRestTemplate.exchange(
            "%s/migrate/case/%s/case-data".formatted(getBasePath(), MISSING_STAGE_MIGRATED_REFERENCE), POST,
            new HttpEntity<>(mapper.writeValueAsString(request), createValidAuthHeaders()), String.class
                                                                 );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo(
            "Could not find a stage when updating migrated reference %s found and case UUID: %s".formatted(
                MISSING_STAGE_MIGRATED_REFERENCE, MISSING_STAGE_CASE_UUID));
        verifyNoInteractions(migrationAuditClient);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "a.person@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    public static class MatchesCaseData implements ArgumentMatcher<CaseData> {

        private final UUID expectedUUID;

        private final Map<String, String> expectedData;

        public MatchesCaseData(UUID expectedUUID, Map<String, String> expectedData) {
            this.expectedUUID = expectedUUID;
            this.expectedData = expectedData;
        }

        @Override
        public boolean matches(CaseData argument) {
            if (!Objects.equals(argument.getUuid(), expectedUUID)) {
                return false;
            }

            if (expectedData == null) {
                return argument.getDataMap() == null;
            }

            if (argument.getDataMap() == null) {
                return false;
            }

            if (argument.getDataMap().size() != expectedData.size()) {
                return false;
            }

            return expectedData
                .entrySet()
                .stream()
                .allMatch(es -> Objects.equals(argument.getData(es.getKey()), es.getValue()));
        }

        @Override
        public String toString() {
            return "CaseData with UUID: %s and data: %s".formatted(describe(expectedUUID), describe(expectedData));
        }

        private String describe(Object object) {
            return ValuePrinter.print(object);
        }

    }

}
