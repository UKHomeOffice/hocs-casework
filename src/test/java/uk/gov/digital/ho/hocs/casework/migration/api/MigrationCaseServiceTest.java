package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseServiceTest {

    private static final long caseID = 12345L;

    private final CaseDataType caseType = CaseDataTypeFactory.from("MIN", "a1");

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

    CaseData caseData;

    Stage stage;

    @Before
    public void setUp() {
        this.migrationCaseService = new MigrationCaseService(migrationCaseDataService, migrationStageService,  correspondentService);
        originalReceivedDate = LocalDate.parse("2020-02-01");
        data = Collections.emptyMap();
        caseData = new CaseData(1L, UUID.randomUUID(), LocalDateTime.now(), "COMP", null, false, data, null,
            null, null, null, LocalDate.now(), LocalDate.now(), LocalDate.now().minusDays(10), false, null, null, null);
        stage = new Stage(caseData.getUuid(), STAGE_TYPE, null, null, null);
    }

    @Test
    public void shouldCreateMigrationCase() throws ApplicationExceptions.EntityCreationException {
        //when
        when(migrationCaseDataService.createCompletedCase(caseDataType.getDisplayName(), data,
            originalReceivedDate)).thenReturn(caseData);

        when(migrationStageService.createStageForClosedCase(caseData.getUuid(), STAGE_TYPE)).thenReturn(stage);

        List<MigrationComplaintCorrespondent> additionalCorrespondents = new ArrayList<>();
        additionalCorrespondents.add(createCorrespondent());
        migrationCaseService.createMigrationCase(
            caseDataType.getDisplayName(),
            STAGE_TYPE,
            data,
            originalReceivedDate);

        // then
        verify(migrationCaseDataService, times(1)).createCompletedCase(caseDataType.getDisplayName(), data,
            originalReceivedDate);
        verify(migrationStageService, times(1)).createStageForClosedCase(caseData.getUuid(), STAGE_TYPE);

        verifyNoMoreInteractions(migrationCaseDataService);
        verifyNoMoreInteractions(migrationStageService);
    }

    MigrationComplaintCorrespondent createCorrespondent() {
        return new MigrationComplaintCorrespondent(
                "fullName",
                CorrespondentType.COMPLAINANT,
                "address1",
                "address2",
                "address3",
                "postcode",
                "country",
                "organisation",
                "telephone",
                "email",
                "reference");
    }
}
