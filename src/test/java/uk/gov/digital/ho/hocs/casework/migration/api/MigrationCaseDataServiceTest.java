package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.casework.migration.client.auditclient.MigrationAuditClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseDataServiceTest {

    private static final long caseID = 12345L;

    private final CaseDataType caseType = CaseDataTypeFactory.from("MIN", "a1");

    private final String STAGE_TYPE = "Migration";

    private MigrationCaseDataService migrationCaseDataService;

    private final Map<String, String> data = new HashMap<>(0);

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private CorrespondentRepository correspondentRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private MigrationAuditClient migrationAuditClient;

    @Mock
    private AuditClient auditClient;

    @Before
    public void setUp() {
        this.migrationCaseDataService = new MigrationCaseDataService(caseDataRepository, infoClient, migrationAuditClient, auditClient, correspondentRepository);
    }

    @Test
    public void shouldCreateMigrationCase() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        // when
        CaseData caseData = migrationCaseDataService.createCompletedCase(caseType.getDisplayCode(), data,
            originalReceivedDate);

        // then
        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws ApplicationExceptions.EntityCreationException {
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        migrationCaseDataService.createCompletedCase(null, new HashMap<>(), originalReceivedDate);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            migrationCaseDataService.createCompletedCase(null, new HashMap<>(), originalReceivedDate);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreatedAPrimaryCorrespondent() {
        // given
        CaseData caseData = new CaseData();
        when(caseDataRepository.findActiveByUuid(any())).thenReturn(caseData);

        Set<Correspondent> correspondents = new HashSet<>();
        correspondents.add(createCorrespondent());
        when(correspondentRepository.findAllByCaseUUID(any())).thenReturn(correspondents);

        // when
        migrationCaseDataService.createPrimaryCorrespondent(createMigrationComplaintCorrespondent(), UUID.randomUUID(), UUID.randomUUID());

        //then
        verify(correspondentRepository, times(1)).save(any());
        verify(caseDataRepository, times(1)).save(any());
    }

    @Test
    public void shouldCreatedAnAdditionalCorrespondent() {
        // given
        Set<Correspondent> correspondents = new HashSet<>();
        correspondents.add(createCorrespondent());
        when(correspondentRepository.findAllByCaseUUID(any())).thenReturn(correspondents);

        List<MigrationComplaintCorrespondent> additionalCorrespondents = new ArrayList<>();
        additionalCorrespondents.add(createMigrationComplaintCorrespondent());

        // when
        migrationCaseDataService.createAdditionalCorrespondent(additionalCorrespondents, UUID.randomUUID(), UUID.randomUUID());

        //then
        verify(correspondentRepository, times(1)).save(any());
        //verify(caseDataRepository, times(1)).save(any());
    }

    MigrationComplaintCorrespondent createMigrationComplaintCorrespondent() {
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
            "reference"
        );
    }

    Correspondent createCorrespondent() {
        return new Correspondent(
            UUID.randomUUID(),
            "correspondentType",
            "fullName",
            "organisation",
            new Address(
                "postcode",
                "address1",
                "address2",
                "address3",
                "country"
            ),
            "telephone",
            "email",
            "reference",
            "reference"
        );
    }
}
