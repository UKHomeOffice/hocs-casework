package uk.gov.digital.ho.hocs.casework.migration;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseReferenceGenerator;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_UPDATED_NULL_DATA;

@Service
@Slf4j
public class MigrationCaseDataService {

    private final CaseDataRepository caseDataRepository;
    private final MigrationStageRepository migrationStageRepository;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;
    private final InfoClient infoClient;
    private final CorrespondentService correspondentService;
    private final StageService stageService;

    @Autowired
    public MigrationCaseDataService(CaseDataRepository caseDataRepository, MigrationStageRepository migrationStageRepository, InfoClient infoClient,
                                    ObjectMapper objectMapper, CorrespondentService correspondentService,
                                    StageService stageService, AuditClient auditClient) {
        this.caseDataRepository = caseDataRepository;
        this.migrationStageRepository = migrationStageRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
        this.correspondentService = correspondentService;
        this.stageService = stageService;
    }

    CaseData createCase(String caseType, String caseReference, Map<String, String> data, LocalDate caseDeadline, LocalDate dateReceived) {
        log.debug("Creating Case of type: {}", caseType);
        CaseDataType caseDataType = infoClient.getCaseType(caseType);

        String newCaseReference = caseReference;
        if(StringUtils.isNullOrEmpty(newCaseReference)){
            newCaseReference = CaseReferenceGenerator.generateCaseReference(caseType, caseDataRepository.getNextSeriesId(), LocalDateTime.now());
        }

        CaseData caseData = new CaseData(caseDataType, newCaseReference, data, objectMapper, caseDeadline, dateReceived);
        caseDataRepository.save(caseData);
        auditClient.createCaseAudit(caseData);
        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    UUID getStageUUID(UUID caseUUID) {
        UUID stageUUID = migrationStageRepository.findByCaseUUID(caseUUID).getUuid();
        return stageUUID;
    }

    void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
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

    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }
}
