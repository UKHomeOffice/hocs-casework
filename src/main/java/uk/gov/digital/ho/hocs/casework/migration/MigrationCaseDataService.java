package uk.gov.digital.ho.hocs.casework.migration;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopyFactory;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseReferenceGenerator;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActiveCaseViewDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDeadlineExtensionTypeRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_UPDATED;

@Service
@Slf4j
@Qualifier("MigrationCaseDataService")
public class MigrationCaseDataService extends CaseDataService {

    private final MigrationStageRepository migrationStageRepository;

    @Autowired
    public MigrationCaseDataService(CaseDataRepository caseDataRepository, ActiveCaseViewDataRepository activeCaseViewDataRepository,
                                    MigrationStageRepository migrationStageRepository, InfoClient infoClient,
                                    ObjectMapper objectMapper, AuditClient auditClient, CaseCopyFactory caseCopyFactory, CaseDeadlineExtensionTypeRepository caseDeadlineExtensionTypeRepository) {
        super(caseDataRepository, activeCaseViewDataRepository,null, infoClient, objectMapper, auditClient, caseCopyFactory, caseDeadlineExtensionTypeRepository);
        this.migrationStageRepository = migrationStageRepository;
    }

    CaseData createCase(MigrationCreateCaseRequest request) {
        String caseType = request.getType();
        String totalsListName = request.getTotalsListName();

        log.debug("Creating Case of type: {}", caseType);
        CaseDataType caseDataType = infoClient.getCaseType(caseType);

        String newCaseReference = request.getCaseReference();
        LocalDateTime caseCreated = request.getCaseCreated() != null ? request.getCaseCreated() : LocalDateTime.now();
        if (StringUtils.isNullOrEmpty(newCaseReference)) {
            newCaseReference = CaseReferenceGenerator.generateCaseReference(caseType, caseDataRepository.getNextSeriesId(), caseCreated);
        }

        CaseData caseData = new CaseData(caseDataType, newCaseReference, request.getData(), objectMapper, request.getCaseDeadline(), request.getDateReceived(), caseCreated);
        caseDataRepository.save(caseData);

        if (!StringUtils.isNullOrEmpty(totalsListName)) {
            calculateTotals(caseData.getUuid(), null, totalsListName);
            caseData = caseDataRepository.findActiveByUuid(caseData.getUuid());
        }

        auditClient.createCaseAudit(caseData);
        log.info("Created Case: {} Ref: {} UUID: {}, Event: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    UUID getStageUUID(UUID caseUUID) {
        return migrationStageRepository.findByCaseUUID(caseUUID).getUuid();
    }

    @Override
    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseUUID);
        if (data != null) {
            log.debug("Data size {}", data.size());
            CaseData caseData = getCase(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            log.info("Updated Case Data for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_UPDATED));
        } else {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
        }
    }

    @Override
    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }
}
