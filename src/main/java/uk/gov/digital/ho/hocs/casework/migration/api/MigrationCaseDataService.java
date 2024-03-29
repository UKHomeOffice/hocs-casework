package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.api.DeadlineService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.casework.migration.api.exception.MigrationExceptions;
import uk.gov.digital.ho.hocs.casework.migration.client.auditclient.MigrationAuditClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_NOT_UPDATED_NULL_DATA;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.PRIMARY_CORRESPONDENT_UPDATED;

@Service
@Slf4j
public class MigrationCaseDataService {

    protected final CaseDataRepository caseDataRepository;

    private final CorrespondentRepository correspondentRepository;

    protected final MigrationAuditClient migrationAuditClient;

    protected final AuditClient auditClient;

    protected final InfoClient infoClient;

    protected final DocumentClient documentClient;

    private final DeadlineService deadlineService;

    private final MigrationStageService migrationStageService;

    private final boolean allowDuplicateMigratedReferences;

    public MigrationCaseDataService(
        CaseDataRepository caseDataRepository,
        DocumentClient documentClient,
        InfoClient infoClient,
        MigrationAuditClient migrationAuditClient,
        AuditClient auditClient,
        CorrespondentRepository correspondentRepository,
        DeadlineService deadlineService,
        MigrationStageService migrationStageService,
        @Value("${migration.allow-duplicate-migrated-references:false}") boolean allowDuplicateMigratedReferences
                                   )
    {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.migrationAuditClient = migrationAuditClient;
        this.auditClient = auditClient;
        this.correspondentRepository = correspondentRepository;
        this.documentClient = documentClient;
        this.deadlineService = deadlineService;
        this.migrationStageService = migrationStageService;
        this.allowDuplicateMigratedReferences = allowDuplicateMigratedReferences;
    }

    protected CaseData getCaseData(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData == null) {
            log.error("Migration Case: {}, not found!", caseUUID, value(EVENT, MIGRATION_CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID),
                MIGRATION_CASE_NOT_FOUND
            );
        }
        log.info("Got Migration Case: {}", caseData.getUuid(), value(EVENT, MIGRATION_CASE_RETRIEVED));
        return caseData;
    }

    protected CaseData getCaseData(String migratedReference) {
        log.debug("Getting Migrated Case: {}", migratedReference);
        Optional<UUID> caseUUID = caseDataRepository.findUUIDByMigratedReference(migratedReference);
        if (caseUUID.isEmpty()) {
            log.error("Migration Case: {}, not found!", migratedReference, value(EVENT, MIGRATION_CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Migrated Case: %s, not found!", migratedReference),
                MIGRATION_CASE_NOT_FOUND
            );
        }

        return getCaseData(caseUUID.get());
    }

    @Transactional
    public void updateCaseData(String migratedReference, LocalDateTime auditEventTimestamp, Map<String, String> data) {
        if (data == null) {
            log.warn("Data was null for Migrated Case: {}", migratedReference,
                value(EVENT, MIGRATION_CASE_NOT_UPDATED_NULL_DATA)
                    );
            return;
        }
        CaseData caseData = getCaseData(migratedReference);
        UUID stageUUID = migrationStageService
            .getStageForCaseUUID(caseData.getUuid())
            .orElseThrow(() -> new MigrationExceptions.ValidStageNotFoundException(migratedReference,
                caseData.getUuid().toString()
            ))
            .getUuid();

        updateCaseData(caseData, stageUUID, data, auditEventTimestamp);
    }

    @Transactional
    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        if (data == null) {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID,
                value(EVENT, MIGRATION_CASE_NOT_UPDATED_NULL_DATA)
                    );
            return;
        }
        CaseData caseData = getCaseData(caseUUID);
        updateCaseData(caseData, stageUUID, data, caseData.getCreated());
    }

    @Transactional
    public void updateCaseData(
        CaseData caseData, UUID stageUUID, Map<String, String> data, LocalDateTime auditEventTimestamp
                              ) {
        log.debug("Updating data for Case: {}", caseData.getUuid());
        if (data == null) {
            log.warn("Data was null for Case: {} Stage: {}", caseData.getUuid(), stageUUID,
                value(EVENT, MIGRATION_CASE_NOT_UPDATED_NULL_DATA)
                    );
            return;
        }
        log.debug("Data size {}", data.size());
        caseData.update(data);
        caseDataRepository.save(caseData);
        migrationAuditClient.updateCaseAudit(caseData, stageUUID, auditEventTimestamp);
        log.info("Updated Case Data for Case: {} Stage: {}", caseData.getUuid(), stageUUID,
            value(EVENT, MIGRATION_CASE_UPDATED)
                );
    }

    @Transactional
    public CaseData createCase(String caseType,
                               Map<String, String> data,
                               LocalDate dateReceived,
                               LocalDate caseDeadline,
                               LocalDate dateCompleted,
                               LocalDate dateCreated,
                               String migratedReference) {
        if(!allowDuplicateMigratedReferences) {
            Optional<UUID> existingUUID = caseDataRepository.findUUIDByMigratedReference(migratedReference);
            if(existingUUID.isPresent()) {
                throw new MigrationExceptions.DuplicateMigratedReferenceException(
                    migratedReference,
                    existingUUID.get()
                );
            }
        }

        log.debug("Creating Case of type: {}", caseType);
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseDataType caseDataType = infoClient.getCaseType(caseType);
        CaseData caseData = new CaseData(caseDataType, caseNumber, data, dateReceived,
            LocalDateTime.of(dateCreated, LocalTime.MIN), migratedReference);

        caseData.setCaseDeadline(caseDeadline != null
            ? caseDeadline
            : deadlineService.calculateWorkingDaysForCaseType(caseType, dateReceived, caseDataType.getSla()));

        if (dateCompleted!=null) {
            caseData.setDateCompleted(dateCompleted.atStartOfDay());
        }

        caseDataRepository.save(caseData);

        migrationAuditClient.createCaseAudit(caseData);

        if(dateCompleted != null) {
            migrationAuditClient.completeCaseAudit(caseData);
        }

        return caseData;
    }

    @Transactional
    public void createPrimaryCorrespondent(MigrationComplaintCorrespondent primaryCorrespondent, UUID caseUUID, UUID stageUUID) {
        log.debug("Creating Correspondent of Type: {} for Migrated Case: {}", primaryCorrespondent.getCorrespondentType(), caseUUID);
        Correspondent correspondent = getCorrespondent(primaryCorrespondent, caseUUID);

        try {
            correspondentRepository.save(correspondent);
            CaseData caseData = getCaseData(caseUUID);
            migrationAuditClient.createCorrespondentAudit(caseData, correspondent);
            Set<Correspondent> caseCorrespondents = correspondentRepository.findAllByCaseUUID(caseUUID);

            if (caseCorrespondents.size()==1) {
                updatePrimaryCorrespondent(caseUUID,
                    stageUUID,
                    caseCorrespondents.stream().findFirst().get().getUuid());
            }

        } catch(DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format(
                    "Failed to create correspondent %s for Migrated Case: %s - %s",
                    correspondent.getUuid(),
                    caseUUID,
                    e.getMessage()
                ),
                CORRESPONDENT_CREATE_FAILURE
            );
        }
        log.info("Created Correspondent: {} for Migrated Case: {}", correspondent.getUuid(), caseUUID,
            value(EVENT, CORRESPONDENT_CREATED));
    }

    @Transactional
    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Migrated Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        migrationAuditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Correspondent for Migrated Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID,
            value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }

    @Transactional
    public void createAdditionalCorrespondent(List<MigrationComplaintCorrespondent> additionalCorrespondents, UUID caseUUID, UUID stageUUID) {
        for (MigrationComplaintCorrespondent additionalCorrespondent : additionalCorrespondents) {
            log.debug("Creating Additional Correspondent of Type: {} for Migrated Case: {}", additionalCorrespondent.getCorrespondentType(), caseUUID);
            Correspondent correspondent = getCorrespondent(additionalCorrespondent, caseUUID);

            try {
                correspondentRepository.save(correspondent);
                CaseData caseData = getCaseData(caseUUID);
                migrationAuditClient.createCorrespondentAudit(caseData, correspondent);

            } catch(DataIntegrityViolationException e) {
                throw new ApplicationExceptions.EntityCreationException(
                    String.format(
                        "Failed to create correspondent %s for Migrated Case: %s - %s",
                        correspondent.getUuid(),
                        caseUUID,
                        e.getMessage()
                    ),
                    CORRESPONDENT_CREATE_FAILURE
                );
            }
            log.info("Created Correspondent: {} for Migrated Case: {}", correspondent.getUuid(), caseUUID,
                value(EVENT, CORRESPONDENT_CREATED));
        }
    }

    private Correspondent getCorrespondent(MigrationComplaintCorrespondent complaintCorrespondent, UUID caseUUID) {
        return new Correspondent(caseUUID, complaintCorrespondent.getCorrespondentType().toString(),
            complaintCorrespondent.getFullName(), complaintCorrespondent.getOrganisation(),
            new Address(complaintCorrespondent.getPostcode(), complaintCorrespondent.getAddress1(),
                complaintCorrespondent.getAddress2(), complaintCorrespondent.getAddress3(),
                complaintCorrespondent.getCountry()
            ), complaintCorrespondent.getTelephone(), complaintCorrespondent.getEmail(),
            complaintCorrespondent.getReference(),
            complaintCorrespondent.getReference());
    }
}
