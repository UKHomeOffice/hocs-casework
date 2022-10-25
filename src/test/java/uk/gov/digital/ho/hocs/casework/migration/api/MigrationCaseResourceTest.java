package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CaseAttachment;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCaseRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseResourceTest {

    private static final long caseID = 12345L;

    public static final String STAGE_TYPE = "Migration";

    private final CaseDataType caseDataType = CaseDataTypeFactory.from("MIN", "a1");

    private final Map<String, String> data = new HashMap<>(0);

    @Mock
    private MigrationCaseService migrationCaseService;

    private final LocalDate dateArg = LocalDate.now();

    private MigrationCaseResource migrationCaseResource;

    private List<CaseAttachment> caseAttachments;

    @Before
    public void setUp() {

        migrationCaseResource = new MigrationCaseResource(migrationCaseService);
        CaseAttachment caseAttachment1 = new CaseAttachment("","","");
        CaseAttachment caseAttachment2 = new CaseAttachment("","","");
        caseAttachments = new ArrayList<>(List.of(caseAttachment1,caseAttachment2));
    }

    @Test
    public void shouldCreateCase() {

        //given
        CaseData caseData = new CaseData(caseDataType, caseID, data, dateArg);
        CreateMigrationCaseRequest request = new CreateMigrationCaseRequest(caseDataType.getDisplayCode(), data, dateArg, caseAttachments, UUID.randomUUID(), STAGE_TYPE);
        when(migrationCaseService.createMigrationCase(caseDataType.getDisplayCode(), STAGE_TYPE, data,
            dateArg, caseAttachments)).thenReturn(caseData);

        ResponseEntity<CreateCaseResponse> response = migrationCaseResource.createMigrationCase(request);

        verify(migrationCaseService, times(1)).createMigrationCase(caseDataType.getDisplayCode(), STAGE_TYPE, data, dateArg, caseAttachments);

        verifyNoMoreInteractions(migrationCaseService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
