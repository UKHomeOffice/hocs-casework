package uk.gov.digital.ho.hocs.casework.migration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActiveCaseViewDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_COMPLETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_TOPIC_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.EXTENSION_APPLIED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_COMPLETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_RECREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.*;

@Service
@Slf4j
@Qualifier("CaseDataService")
public class MigrationCaseDataService {
    protected final CaseDataRepository caseDataRepository;
    protected final ActiveCaseViewDataRepository activeCaseViewDataRepository;
    protected final AuditClient auditClient;
    protected final ObjectMapper objectMapper;
    protected final InfoClient infoClient;

    @Autowired
    public MigrationCaseDataService(CaseDataRepository caseDataRepository,
                                    ActiveCaseViewDataRepository activeCaseViewDataRepository,
                                    InfoClient infoClient,
                                    ObjectMapper objectMapper,
                                    AuditClient auditClient) {

        this.caseDataRepository = caseDataRepository;
        this.activeCaseViewDataRepository = activeCaseViewDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    public static final List<String> TIMELINE_EVENTS = List.of(
            CASE_CREATED.toString(),
            CASE_COMPLETED.toString(),
            CASE_TOPIC_CREATED.toString(),
            CASE_TOPIC_DELETED.toString(),
            STAGE_ALLOCATED_TO_TEAM.toString(),
            STAGE_CREATED.toString(),
            STAGE_RECREATED.toString(),
            STAGE_COMPLETED.toString(),
            STAGE_ALLOCATED_TO_USER.toString(),
            CORRESPONDENT_DELETED.toString(),
            CORRESPONDENT_CREATED.toString(),
            CORRESPONDENT_UPDATED.toString(),
            DOCUMENT_CREATED.toString(),
            DOCUMENT_DELETED.toString(),
            APPEAL_UPDATED.toString(),
            APPEAL_CREATED.toString(),
            EXTENSION_APPLIED.toString(),
            EXTERNAL_INTEREST_CREATED.toString(),
            EXTERNAL_INTEREST_UPDATED.toString()

    );

    protected CaseData getCaseData(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData == null) {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
        log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
        return caseData;
    }

    public String getCaseType(UUID caseUUID) {
        String shortCode = caseUUID.toString().substring(34);
        log.debug("Looking up CaseType for Case: {} Shortcode: {}", caseUUID, shortCode);
        String caseType;
        try {
            CaseDataType caseDataType = infoClient.getCaseTypeByShortCode(shortCode);
            caseType = caseDataType.getDisplayCode();
        } catch (RestClientException e) {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID, value(EVENT, CASE_TYPE_LOOKUP_FAILED), value(EXCEPTION, e));
            caseType = getCaseData(caseUUID).getType();
        }
        log.debug("CaseType {} found for Case: {}", caseType, caseUUID);
        return caseType;
    }


    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        if (data == null) {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
            return;
        }
        updateCaseData(getCaseData(caseUUID), stageUUID, data);
    }

    public void updateCaseData(CaseData caseData, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseData.getUuid());
        if (data == null) {
            log.warn("Data was null for Case: {} Stage: {}", caseData.getUuid(), stageUUID, value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
            return;
        }

        log.debug("Data size {}", data.size());
        caseData.update(data);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Case Data for Case: {} Stage: {}", caseData.getUuid(), stageUUID, value(EVENT, CASE_UPDATED));
    }
}
