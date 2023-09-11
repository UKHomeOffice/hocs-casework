package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.TopicService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseServiceTest {

    private final String STAGE_TYPE = "Migration";

    private MigrationCaseService migrationCaseService;

    private Map<String, String> data = new HashMap<>(0);

    private final CaseDataType caseDataType = new CaseDataType("MIN", "1a", "MIN", null, 20, 15);

    @Mock
    private MigrationStageService migrationStageService;

    @Mock
    private MigrationCaseDataService migrationCaseDataService;

    @Mock
    private TopicService topicService;

    @Mock
    private CaseDataService caseDataService;

    LocalDate originalReceivedDate;

    LocalDate originalDeadline;

    LocalDate originalCompletedDate;

    LocalDate originalCreatedDate;

    String migratedReference;

    UUID caseUUID;

    Stage stage;

    @Before
    public void setUp() {
        this.migrationCaseService = new MigrationCaseService(migrationCaseDataService, migrationStageService, topicService, caseDataService);
        originalReceivedDate = LocalDate.parse("2020-02-01");
        originalDeadline = LocalDate.parse("2020-02-28");
        originalCompletedDate = LocalDate.parse("2020-03-01");
        originalCreatedDate = LocalDate.parse("2020-02-01");
        migratedReference = "123456";
        data = Collections.emptyMap();
        caseUUID = UUID.randomUUID();
        stage = new Stage(caseUUID, STAGE_TYPE, null, null, null);
    }

    private CaseData getCaseData(boolean completed) {
        return new CaseData(1L, caseUUID, LocalDateTime.now(), "COMP", null, false, data, null, null, null,
            null, LocalDate.now(), LocalDate.now(), LocalDate.now().minusDays(10), completed ? originalCompletedDate.atStartOfDay() : null, null, null
        );
    }

    @Test
    public void shouldCreateCompletedMigrationCase() throws ApplicationExceptions.EntityCreationException {
        //when
        CaseData caseData = getCaseData(true);
        when(migrationCaseDataService.createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, originalDeadline, originalCompletedDate, originalCreatedDate, migratedReference)).thenReturn(caseData);

        when(migrationStageService.createStageForClosedCase(caseData.getUuid(), STAGE_TYPE)).thenReturn(stage);

        migrationCaseService.createMigrationCase(
            caseDataType.getDisplayName(),
            STAGE_TYPE,
            data,
            originalReceivedDate,
            originalDeadline,
            originalCompletedDate,
            originalCreatedDate,
            migratedReference);

        // then
        verify(migrationCaseDataService, times(1)).createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, originalDeadline, originalCompletedDate, originalCreatedDate, migratedReference);
        verify(migrationStageService, times(1)).createStageForClosedCase(caseData.getUuid(), STAGE_TYPE);

        verifyNoMoreInteractions(migrationCaseDataService);
        verifyNoMoreInteractions(migrationStageService);
    }

    @Test
    public void shouldCreateOpenMigrationCase() throws ApplicationExceptions.EntityCreationException {
        //when
        CaseData caseData = getCaseData(false);
        when(migrationCaseDataService.createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, originalDeadline,null, originalCreatedDate, migratedReference)).thenReturn(caseData);

        migrationCaseService.createMigrationCase(caseDataType.getDisplayName(), STAGE_TYPE, data, originalReceivedDate,
            originalDeadline, null, originalCreatedDate, migratedReference);

        // then
        verify(migrationCaseDataService, times(1)).createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, originalDeadline, null, originalCreatedDate, migratedReference);

        verify(migrationStageService, never()).createStageForClosedCase(caseData.getUuid(), STAGE_TYPE);
        verifyNoMoreInteractions(migrationCaseDataService);
        verifyNoMoreInteractions(migrationStageService);
    }

    @Test
    public void shouldSetPrimaryTopic() {
        UUID topicUUID = UUID.randomUUID();
        Topic createdTopic = new Topic(caseUUID, "topicName", topicUUID);

        when(topicService.createTopic(caseUUID, topicUUID)).thenReturn(createdTopic);
        doNothing().when(caseDataService).updatePrimaryTopic(caseUUID, stage.getUuid(), createdTopic.getUuid());

        migrationCaseService.createPrimaryTopic(caseUUID, stage.getUuid(), topicUUID);

        verify(topicService, times(1)).createTopic(caseUUID, topicUUID);
        verify(caseDataService, times(1)).updatePrimaryTopic(caseUUID, stage.getUuid(), createdTopic.getUuid());

        verifyNoMoreInteractions(topicService);
        verifyNoMoreInteractions(caseDataService);
    }
}
