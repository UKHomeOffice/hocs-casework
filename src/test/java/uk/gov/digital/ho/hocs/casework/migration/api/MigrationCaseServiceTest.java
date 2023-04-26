package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private CorrespondentService correspondentService;

    LocalDate originalReceivedDate;

    LocalDate originalCompletedDate;

    UUID caseUUID;

    Stage stage;

    @Before
    public void setUp() {
        this.migrationCaseService = new MigrationCaseService(migrationCaseDataService, migrationStageService,  correspondentService);
        originalReceivedDate = LocalDate.parse("2020-02-01");
        originalCompletedDate = LocalDate.parse("2020-03-01");
        data = Collections.emptyMap();
        caseUUID = UUID.randomUUID();
        stage = new Stage(caseUUID, STAGE_TYPE, null, null, null);
    }

    private CaseData getCaseData(boolean completed) {
        return new CaseData(1L, caseUUID, LocalDateTime.now(), "COMP", null, false, data, null, null, null,
            null, LocalDate.now(), LocalDate.now(), LocalDate.now().minusDays(10), completed, completed ? originalCompletedDate.atStartOfDay() : null, null, null
        );
    }

    @Test
    public void shouldCreateCompletedMigrationCase() throws ApplicationExceptions.EntityCreationException {
        //when
        CaseData caseData = getCaseData(true);
        when(migrationCaseDataService.createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, originalCompletedDate)).thenReturn(caseData);

        when(migrationStageService.createStageForClosedCase(caseData.getUuid(), STAGE_TYPE)).thenReturn(stage);

        migrationCaseService.createMigrationCase(
            caseDataType.getDisplayName(),
            STAGE_TYPE,
            data,
            originalReceivedDate,
            originalCompletedDate);

        // then
        verify(migrationCaseDataService, times(1)).createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, originalCompletedDate);
        verify(migrationStageService, times(1)).createStageForClosedCase(caseData.getUuid(), STAGE_TYPE);

        verifyNoMoreInteractions(migrationCaseDataService);
        verifyNoMoreInteractions(migrationStageService);
    }

    @Test
    public void shouldCreateOpenMigrationCase() throws ApplicationExceptions.EntityCreationException {
        //when
        CaseData caseData = getCaseData(false);
        when(migrationCaseDataService.createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, null)).thenReturn(caseData);

        migrationCaseService.createMigrationCase(
            caseDataType.getDisplayName(),
            STAGE_TYPE,
            data,
            originalReceivedDate,
            null);

        // then
        verify(migrationCaseDataService, times(1)).createCase(caseDataType.getDisplayName(), data,
            originalReceivedDate, null);

        verify(migrationStageService, never()).createStageForClosedCase(caseData.getUuid(), STAGE_TYPE);
        verifyNoMoreInteractions(migrationCaseDataService);
        verifyNoMoreInteractions(migrationStageService);
    }
}
