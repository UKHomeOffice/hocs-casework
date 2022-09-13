package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseServiceTest {
    private static final long caseID = 12345L;

    private final CaseDataType caseType = CaseDataTypeFactory.from("MIN", "a1");

    private final String STAGE_TYPE = "Migration";

    private MigrationCaseService migrationCaseService;

    private final Map<String, String> data = new HashMap<>(0);

    private final CaseDataType caseDataType = new CaseDataType(
            "MIN",
            "1a",
            "MIN",
            null,
            20,
            15
    );

    @Mock
    private MigrationStageService migrationStageService;

    @Mock
    private MigrationCaseDataService migrationCaseDataService;

    @Before
    public void setUp() {
        this.migrationCaseService = new MigrationCaseService(migrationCaseDataService, migrationStageService);
    }

    @Test
    public void shouldCreateMigrationCase() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        Map<String, String> data = Collections.emptyMap();
        CaseData caseData = new CaseData(
                1L,
                UUID.randomUUID(),
                LocalDateTime.now(),
                "COMP",
                null,
                false,
                data,
                null,
                null,
                null,
                null,
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().minusDays(10),
                false,
                null,
                null);

        //when
        when(migrationCaseDataService.createCompletedCase(caseDataType.getDisplayName(), data, originalReceivedDate)).thenReturn(caseData);

        migrationCaseService.createMigrationCase(caseDataType.getDisplayName(), STAGE_TYPE, data, originalReceivedDate);

        // then
        verify(migrationCaseDataService, times(1)).createCompletedCase(caseDataType.getDisplayName(), data, originalReceivedDate);
        verify(migrationStageService, times(1)).createStageForClosedCase(caseData.getUuid(), STAGE_TYPE);
        verifyNoMoreInteractions(migrationCaseDataService);
        verifyNoMoreInteractions(migrationStageService);
    }
}
