package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;

import java.time.LocalDate;
import java.util.Map;
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

    protected final AuditClient auditClient;

    protected final InfoClient infoClient;

    public MigrationCaseDataService(CaseDataRepository caseDataRepository,
                                    InfoClient infoClient,
                                    AuditClient auditClient,
                                    CorrespondentRepository correspondentRepository) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.correspondentRepository = correspondentRepository;
    }

    protected CaseData getCaseData(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData==null) {
            log.error("Migration Case: {}, not found!", caseUUID, value(EVENT, MIGRATION_CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID),
                MIGRATION_CASE_NOT_FOUND);
        }
        log.info("Got Migration Case: {}", caseData.getUuid(), value(EVENT, MIGRATION_CASE_RETRIEVED));
        return caseData;
    }

    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        if (data==null) {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID,
                value(EVENT, MIGRATION_CASE_NOT_UPDATED_NULL_DATA));
            return;
        }
        updateCaseData(getCaseData(caseUUID), stageUUID, data);
    }

    public void updateCaseData(CaseData caseData, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseData.getUuid());
        if (data==null) {
            log.warn("Data was null for Case: {} Stage: {}", caseData.getUuid(), stageUUID,
                value(EVENT, MIGRATION_CASE_NOT_UPDATED_NULL_DATA));
            return;
        }
        log.debug("Data size {}", data.size());
        caseData.update(data);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Case Data for Case: {} Stage: {}", caseData.getUuid(), stageUUID,
            value(EVENT, MIGRATION_CASE_UPDATED));
    }

    @Transactional
    CaseData createCompletedCase(String caseType, Map<String, String> data, LocalDate dateReceived) {
        log.debug("Creating Case of type: {}", caseType);
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseDataType caseDataType = infoClient.getCaseType(caseType);
        CaseData caseData = new CaseData(caseDataType, caseNumber, data, dateReceived);
        LocalDate deadline = LocalDate.now();
        caseData.setCaseDeadline(deadline);
        caseData.setCompleted(true);
        caseDataRepository.save(caseData);
        auditClient.createCaseAudit(caseData);
        return caseData;
    }

    void createPrimaryCorrespondent(MigrationComplaintCorrespondent primaryCorrespondent, UUID caseUUID, UUID stageUUID) {
        log.debug("Creating Correspondent of Type: {} for Migrated Case: {}", primaryCorrespondent.getCorrespondentType(), caseUUID);
        Correspondent correspondent = new Correspondent(caseUUID,
            primaryCorrespondent.getCorrespondentType().toString(),
            primaryCorrespondent.getFullName(),
            primaryCorrespondent.getOrganisation(),
            new Address(primaryCorrespondent.getPostcode(),
                primaryCorrespondent.getAddress1(), primaryCorrespondent.getAddress2(), primaryCorrespondent.getAddress3(),
                primaryCorrespondent.getCountry()
            ),
            primaryCorrespondent.getTelephone(),
            primaryCorrespondent.getEmail(),
            primaryCorrespondent.getReference(),
            primaryCorrespondent.getReference());

        try {
            correspondentRepository.save(correspondent);
            auditClient.createCorrespondentAudit(correspondent);

            Set<Correspondent> caseCorrespondents = correspondentRepository.findAllByCaseUUID(caseUUID);

            if (caseCorrespondents.size()==1) {
                updatePrimaryCorrespondent(caseUUID,
                    stageUUID,
                    caseCorrespondents.stream().findFirst().get().getCaseUUID());
            }

        } catch(DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Failed to create correspondent %s for Migrated Case: %s", correspondent.getUuid(), caseUUID),
                CORRESPONDENT_CREATE_FAILURE, e);
        }
        log.info("Created Correspondent: {} for Migrated Case: {}", correspondent.getUuid(), caseUUID,
            value(EVENT, CORRESPONDENT_CREATED));
    }

    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Migrated Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Correspondent for Migrated Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID,
            value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }
}
