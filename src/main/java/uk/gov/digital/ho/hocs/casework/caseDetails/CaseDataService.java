package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentToCaseRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageType;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
public class CaseDataService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;
    private final StageDataRepository stageDataRepository;
    private final DocumentService documentService;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository,
                           StageDataRepository stageDataRepository,
                           AuditService auditService,
                           DocumentService documentService,
                           RequestData requestData) {
        this.caseDataRepository = caseDataRepository;
        this.stageDataRepository = stageDataRepository;
        this.auditService = auditService;
        this.documentService = documentService;

        //TODO: This should be a Bean
        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());
        this.requestData = requestData;

    }

    private static String getDataString(Map<String, String> stageData, ObjectMapper objectMapper) throws EntityCreationException {
        String data = "{ }";
        if (stageData != null) {
            try {
                data = objectMapper.writeValueAsString(stageData);
            } catch (JsonProcessingException e) {
                throw new EntityCreationException("Object Mapper failed to parse!");
            }
        }

        return data;
    }

    @Transactional
    public void addDocumentToCase(AddDocumentToCaseRequest document) throws EntityCreationException, EntityNotFoundException {
        log.info("Adding document {} to case {}", document.getDocumentUUID(), document.getCaseUUID());
        if (isNullOrEmpty(document.getDocumentUUID()) && isNullOrEmpty(document.getCaseUUID())) {
            throw new EntityCreationException("Failed to add document to case");
        }

        documentService.updateDocument(UUID.fromString(document.getCaseUUID()),
                UUID.fromString(document.getDocumentUUID()),
                document.getS3OrigLink(),
                document.getS3PdfLink(),
                document.getStatus());

    }


    @Transactional
    public CaseData createCase(CaseType caseType) throws EntityCreationException {
        log.info("Requesting Create Case, Type: {}, User: {}", caseType, requestData.username());
        if (!isNullOrEmpty(caseType)) {
            CaseData caseData = new CaseData(caseType.toString(), caseDataRepository.getNextSeriesId());
            caseDataRepository.save(caseData);
            auditService.writeCreateCaseEvent(caseData);
            log.info("Created Case, Reference: {}, UUID: {} User: {}", caseData.getReference(), caseData.getUuid(), requestData.username());
            return caseData;
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    @Transactional
    public StageData createStage(UUID caseUUID, StageType stageType, Map<String, String> stageData) throws EntityCreationException {
        log.info("Requesting Create Stage, Type: {}, Case UUID: {}, User: {}", stageType, caseUUID, requestData.username());
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(stageType)) {
            String data = getDataString(stageData, objectMapper);
            StageData stageDetails = new StageData(caseUUID, stageType.toString(), data);
            stageDataRepository.save(stageDetails);
            auditService.writeCreateStageEvent(stageDetails);
            log.info("Created Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getType(), stageDetails.getUuid(), stageDetails.getCaseUUID(), requestData.username());
            return stageDetails;
        } else {
            throw new EntityCreationException("Failed to create stage, invalid stageType or caseUUID!");
        }
    }

    @Transactional
    public CaseData updateCase(UUID caseUUID, CaseType caseType) throws EntityCreationException, EntityNotFoundException {
        log.info("Requesting Update Case: {}, Type: {}, User: {}", caseUUID, caseType, requestData.username());
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(caseType)) {
            CaseData caseData = caseDataRepository.findByUuid(caseUUID);
            if (caseData != null) {
                caseData.setType(caseType.toString());
                caseDataRepository.save(caseData);
                auditService.writeUpdateCaseEvent(caseData);
                log.info("Updated Case, Reference: {}, UUID: {} User: {}", caseData.getReference(), caseData.getUuid(), requestData.username());
                return caseData;
            } else {
                throw new EntityNotFoundException("Case not found!");
            }
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    @Transactional
    public StageData updateStage(UUID caseUUID, UUID stageUUID, StageType stageType, Map<String, String> stageData) throws EntityNotFoundException, EntityCreationException {
        log.info("Requesting Update Stage, uuid: {}, User: {}", stageUUID, requestData.username());
        if (!isNullOrEmpty(stageUUID) && !isNullOrEmpty(stageType)) {
            StageData stageDetails = stageDataRepository.findByUuid(stageUUID);
            if (stageDetails != null) {
                String data = getDataString(stageData, objectMapper);
                stageDetails.setType(stageType.toString());
                stageDetails.setData(data);
                stageDataRepository.save(stageDetails);
                auditService.writeUpdateStageEvent(stageDetails);
                log.info("Updated Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getType(), stageDetails.getUuid(), stageDetails.getCaseUUID(), requestData.username());
                return stageDetails;
            } else {
                throw new EntityNotFoundException("Stage not found!");
        }
        } else {
            throw new EntityCreationException("Failed to update stage, invalid StageType!");
        }
    }

    @Transactional
    public CaseData getCase(UUID uuid) throws EntityNotFoundException {
        log.info("Requesting Case, UUID: {}, User: {}", uuid, requestData.username());
        if (uuid != null) {
            CaseData caseData = caseDataRepository.findByUuid(uuid);
            auditService.writeGetCaseEvent( uuid);
            if (caseData != null) {
                log.info("Found Case, Reference: {} ({}), UseFr: {}", caseData.getReference(), caseData.getUuid(), requestData.username());
                return caseData;
            } else {
                throw new EntityNotFoundException("Case not Found!");
            }
        } else {
            throw new EntityNotFoundException("CaseUUID was null");
        }
    }
}