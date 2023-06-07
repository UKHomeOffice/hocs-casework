package uk.gov.digital.ho.hocs.casework.migration.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.DeadlineService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.casework.migration.api.exception.MigrationExceptions;
import uk.gov.digital.ho.hocs.casework.migration.client.auditclient.MigrationAuditClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationCaseDataServiceTest {

    private static final long caseID = 12345L;

    private final CaseDataType caseType = CaseDataTypeFactory.from("MIN", "a1");

    private final String migratedReference = "12345";

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

    @Mock
    private DocumentClient documentClient;

    @Mock
    private DeadlineService deadlineService;

    @Before
    public void setUp() {
        this.migrationCaseDataService = new MigrationCaseDataService(caseDataRepository, documentClient, infoClient,
            migrationAuditClient, auditClient, correspondentRepository, deadlineService, true
        );
    }

    @Test
    public void shouldCreateCompletedMigrationCase() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate originalCompletedDate = LocalDate.parse("2020-03-01");
        LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        // when
        CaseData caseData = migrationCaseDataService.createCase(caseType.getDisplayCode(), data,
            originalReceivedDate, null, originalCompletedDate, originalCreatedDate, migratedReference);

        // then
        assertThat(caseData.getDateCompleted()).isEqualTo(originalCompletedDate.atStartOfDay());
        assertThat(caseData.getCreated()).isEqualTo(LocalDateTime.of(originalCreatedDate, LocalTime.MIN));

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreateOpenMigrationCase() throws ApplicationExceptions.EntityCreationException {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        // when
        CaseData caseData = migrationCaseDataService.createCase(caseType.getDisplayCode(), data,
            originalReceivedDate, null, null, originalCreatedDate, migratedReference);

        // then
        assertThat(caseData.isCompleted()).isEqualTo(false);
        assertThat(caseData.getDateCompleted()).isNull();
        assertThat(caseData.getCreated()).isEqualTo(LocalDateTime.of(originalCreatedDate, LocalTime.MIN));

        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateCaseMissingTypeException() throws ApplicationExceptions.EntityCreationException {
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate originalCompletedDate = LocalDate.parse("2020-03-01");
        LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");
        migrationCaseDataService.createCase(null, new HashMap<>(), originalReceivedDate, null, originalCompletedDate, originalCreatedDate, migratedReference);
    }

    @Test()
    public void shouldNotCreateCaseMissingType() {
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate originalCompletedDate = LocalDate.parse("2020-03-01");
        LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        try {
            migrationCaseDataService.createCase(null, new HashMap<>(), originalReceivedDate, null, originalCompletedDate, originalCreatedDate, migratedReference);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).getNextSeriesId();

        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void createCaseThrowsExceptionWhenADuplicateMigratedReferenceIsFound() {
        migrationCaseDataService = new MigrationCaseDataService(caseDataRepository, documentClient, infoClient,
            migrationAuditClient, auditClient, correspondentRepository, deadlineService, false
        );

        final String migratedReference = "MigratedReference";
        final UUID existingCaseUUID = UUID.randomUUID();

        final LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        final LocalDate originalCompletedDate = LocalDate.parse("2020-03-01");
        final LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");

        when(caseDataRepository.findUUIDByMigratedReference(migratedReference)).thenReturn(
            Optional.of(existingCaseUUID));

        assertThatThrownBy(() -> migrationCaseDataService.createCase(caseType.getDisplayCode(), data,
            originalReceivedDate, null, originalCompletedDate, originalCreatedDate, migratedReference))
            .isInstanceOf(MigrationExceptions.DuplicateMigratedReferenceException.class)
            .hasMessage("Existing case with migrated reference %s found, existing case UUID: %s", migratedReference, existingCaseUUID);

        verify(caseDataRepository, times(1)).findUUIDByMigratedReference(migratedReference);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void createCaseSavesTheCaseWhenTheMigratedReferenceIsNotADuplicate() {
        migrationCaseDataService = new MigrationCaseDataService(caseDataRepository, documentClient, infoClient,
            migrationAuditClient, auditClient, correspondentRepository, deadlineService, false
        );

        final String migratedReference = "MigratedReference";

        final LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        final LocalDate originalCompletedDate = LocalDate.parse("2020-03-01");
        final LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");

        when(caseDataRepository.findUUIDByMigratedReference(migratedReference)).thenReturn(Optional.empty());
        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = migrationCaseDataService.createCase(caseType.getDisplayCode(), data,
            originalReceivedDate, null, originalCompletedDate, originalCreatedDate, migratedReference);

        verify(caseDataRepository, times(1)).findUUIDByMigratedReference(migratedReference);
        verify(caseDataRepository, times(1)).getNextSeriesId();
        verify(caseDataRepository, times(1)).save(caseData);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    public void shouldCreatedAPrimaryCorrespondent() {
        // given
        Set<Correspondent> correspondents = new HashSet<>();
        Correspondent correspondent = createCorrespondent();
        correspondents.add(correspondent);

        CaseData caseData = mock(CaseData.class);
        when(caseData.getUuid()).thenReturn(UUID.randomUUID());
        doNothing().when(caseData).setPrimaryCorrespondentUUID(correspondent.getUuid());
        when(caseDataRepository.findActiveByUuid(any())).thenReturn(caseData);

        when(correspondentRepository.findAllByCaseUUID(caseData.getUuid())).thenReturn(correspondents);

        // when
        migrationCaseDataService.createPrimaryCorrespondent(
            createMigrationComplaintCorrespondent(), caseData.getUuid(), UUID.randomUUID());

        //then
        verify(caseData, times(1)).setPrimaryCorrespondentUUID(correspondent.getUuid());
        verify(correspondentRepository, times(1)).save(any());
        verify(caseDataRepository, times(1)).save(any());
    }

    @Test
    public void shouldCreateAnAdditionalCorrespondent() {
        // given
        CaseData caseData = mock(CaseData.class);
        when(caseData.getUuid()).thenReturn(UUID.randomUUID());
        List<MigrationComplaintCorrespondent> additionalCorrespondents = new ArrayList<>();
        additionalCorrespondents.add(createMigrationComplaintCorrespondent());
        when(caseDataRepository.findActiveByUuid(any())).thenReturn(caseData);

        // when
        migrationCaseDataService.createAdditionalCorrespondent(
            additionalCorrespondents, UUID.randomUUID(), UUID.randomUUID());

        //then
        verify(correspondentRepository, times(1)).save(any());
    }

    @Test
    public void shouldUseProvidedDeadlineWhenMigratingACase() {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate originalDeadline = LocalDate.parse("2020-02-28");
        LocalDate originalCompletedDate = LocalDate.parse("2020-03-01");
        LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        // when
        CaseData caseData = migrationCaseDataService.createCase(caseType.getDisplayCode(), data,
            originalReceivedDate, originalDeadline, originalCompletedDate, originalCreatedDate, migratedReference);

        // then
        assertThat(caseData.getCaseDeadline()).isEqualTo(originalDeadline);

        verifyNoMoreInteractions(deadlineService);
    }

    @Test
    public void shouldCalculateDeadlineWhenNoDeadlineIsProvided() {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate originalCompletedDate = LocalDate.parse("2020-03-01");
        LocalDate originalCreatedDate = LocalDate.parse("2020-02-01");

        LocalDate exampleDeadline = LocalDate.parse("2020-02-28");

        when(infoClient.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        when(deadlineService.calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate, caseType.getSla()))
            .thenReturn(exampleDeadline);

        // when
        CaseData caseData = migrationCaseDataService.createCase(caseType.getDisplayCode(), data,
            originalReceivedDate, null, originalCompletedDate, originalCreatedDate, migratedReference);

        // then
        assertThat(caseData.getCaseDeadline()).isEqualTo(exampleDeadline);

        verify(deadlineService, times(1)).calculateWorkingDaysForCaseType(caseType.getDisplayCode(), originalReceivedDate, caseType.getSla());
        verifyNoMoreInteractions(deadlineService);
    }

    MigrationComplaintCorrespondent createMigrationComplaintCorrespondent() {
        return new MigrationComplaintCorrespondent("fullName", CorrespondentType.COMPLAINANT, "address1", "address2",
            "address3", "postcode", "country", "organisation", "telephone", "email", "reference"
        );
    }

    Correspondent createCorrespondent() {
        return new Correspondent(UUID.randomUUID(), "correspondentType", "fullName", "organisation",
            new Address("postcode", "address1", "address2", "address3", "country"), "telephone", "email", "reference",
            "reference"
        );
    }

}
