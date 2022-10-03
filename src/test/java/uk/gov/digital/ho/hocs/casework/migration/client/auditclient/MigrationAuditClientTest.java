package uk.gov.digital.ho.hocs.casework.migration.client.auditclient;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.client.auditclient.EventType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.util.SnsStringMessageAttributeValue;
import uk.gov.digital.ho.hocs.casework.utils.BaseAwsTest;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class MigrationAuditClientTest extends BaseAwsTest {

    @Captor
    private ArgumentCaptor<PublishRequest> publicRequestCaptor;

    private ResultCaptor<PublishResult> snsPublishResult;

    @SpyBean
    private AmazonSNSAsync auditSearchSnsClient;

    @MockBean(name = "requestData")
    private RequestData requestData;

    @MockBean
    private RestHelper restHelper;

    @Autowired
    private MigrationAuditClient migrationAuditClient;

    @Value("${hocs.audit-service}")
    private String auditService;

    @Value("${migration.userid}")
    private String userId;

    @Value("${migration.username}")
    private String userName;

    @Before
    public void setUp() {
        when(requestData.correlationId()).thenReturn(UUID.randomUUID().toString());
        when(requestData.userId()).thenReturn("some user id");
        when(requestData.groups()).thenReturn("some groups");
        when(requestData.username()).thenReturn("some username");

        snsPublishResult = new ResultCaptor<>();
        doAnswer(snsPublishResult).when(auditSearchSnsClient).publish(any());
    }

    @Test
    public void shouldSendCaseCreateEvent() throws JsonProcessingException {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        migrationAuditClient.createCaseAudit(caseData);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(caseData.getUuid(), EventType.CASE_CREATED);
    }

    @Test
    public void auditStageCreated() throws IOException {
        var caseUUID = UUID.randomUUID();
        var stage = new Stage(caseUUID, "SOME_STAGE", null, null, null);

        migrationAuditClient.createStage(stage);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        assertSnsValues(stage.getCaseUUID(), EventType.STAGE_CREATED);
    }

    @Test
    public void shouldSetHeaders() {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());
        Map<String, MessageAttributeValue> expectedHeaders = Map.of("event_type",
            new SnsStringMessageAttributeValue(EventType.CASE_CREATED.toString()), RequestData.CORRELATION_ID_HEADER,
            new SnsStringMessageAttributeValue(requestData.correlationId()), RequestData.USER_ID_HEADER,
            new SnsStringMessageAttributeValue(userId), RequestData.USERNAME_HEADER,
            new SnsStringMessageAttributeValue(userName));

        migrationAuditClient.createCaseAudit(caseData);

        verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());

        Assertions.assertTrue(
            publicRequestCaptor.getValue().getMessageAttributes().entrySet().containsAll(expectedHeaders.entrySet()));
    }

    @Test
    public void shouldNotThrowExceptionOnFailure() {
        var caseID = 12345L;
        var caseType = CaseDataTypeFactory.from("TEST", "F0");
        var stageUUID = UUID.randomUUID();
        var caseData = new CaseData(caseType, caseID, new HashMap<>(), LocalDate.now());

        doThrow(new RuntimeException("An error occurred")).when(auditSearchSnsClient).publish(any());

        assertThatCode(() -> migrationAuditClient.updateCaseAudit(caseData, stageUUID)).doesNotThrowAnyException();
    }

    private void assertSnsValues(UUID caseUuid, EventType event) throws JsonProcessingException {
        assertSnsValues(caseUuid, event, Collections.emptyMap());
    }

    private void assertSnsValues(UUID caseUuid,
                                 EventType event,
                                 @NotNull Map<String, String> otherValues) throws JsonProcessingException {
        var caseCreated = objectMapper.readValue(publicRequestCaptor.getValue().getMessage(), CreateAuditRequest.class);

        Assertions.assertNotNull(snsPublishResult.getResult());
        Assertions.assertNotNull(snsPublishResult.getResult().getMessageId());
        Assertions.assertEquals(snsPublishResult.getResult().getSdkHttpMetadata().getHttpStatusCode(), 200);
        Assertions.assertEquals(caseCreated.getCaseUUID(), caseUuid);
        Assertions.assertEquals(caseCreated.getType(), event);

        if (!otherValues.isEmpty()) {
            var caseCreatedData = objectMapper.readValue(caseCreated.getAuditPayload(), Map.class);

            Assertions.assertTrue(caseCreatedData.entrySet().containsAll(otherValues.entrySet()));
        }
    }

}
