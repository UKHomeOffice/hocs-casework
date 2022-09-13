package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("local")
public class MigrationStageServiceTest {

    private final UUID transitionNoteUUID = UUID.randomUUID();

    private final CaseDataType caseDataType = new CaseDataType("MIN", "1a", "MIN", null, 20, 15);

    private final String userID = UUID.randomUUID().toString();

    private MigrationStageService migrationStageService;

    @Mock
    private StageRepository stageRepository;

    private String ALLOCATION_TYPE = "ALLOCATION_TYPE";

    private final Set<Stage> MOCK_STAGE_LIST = new HashSet<>();

    private final UUID CASE_UUID = UUID.fromString("5a2e121f-f0c8-4725-870d-c8134a0f1e6b");

    @Before
    public void setUp() {
        this.migrationStageService = new MigrationStageService(stageRepository);
    }

    @Test
    public void testShouldCreateMigrationStage() {
        // given
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);
        String stageType = "COMP_MIGRATION_END";

        // when
        Stage stage = migrationStageService.createStageForClosedCase(caseData.getUuid(), stageType);

        // then
        verify(stageRepository, times(1)).save(any(Stage.class));
        verify(stageRepository).save(stage);

        verifyNoMoreInteractions(stageRepository);
    }

}
