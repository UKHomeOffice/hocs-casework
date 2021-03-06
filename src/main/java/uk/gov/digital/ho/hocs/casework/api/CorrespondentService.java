package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class CorrespondentService {

    private final CorrespondentRepository correspondentRepository;
    private final CaseDataRepository caseDataRepository;
    private final CaseDataService caseDataService;
    private final AuditClient auditClient;
    private final InfoClient infoClient;

    @Autowired
    public CorrespondentService(
            CorrespondentRepository correspondentRepository,
            CaseDataRepository caseDataRepository,
            AuditClient auditClient,
            InfoClient infoClient,
            CaseDataService caseDataService) {
        this.correspondentRepository = correspondentRepository;
        this.caseDataRepository = caseDataRepository;
        this.auditClient = auditClient;
        this.infoClient = infoClient;
        this.caseDataService = caseDataService;
    }

    Set<Correspondent> getAllActiveCorrespondents() {
        log.debug("Getting all active Correspondents");
        Set<Correspondent> correspondents = correspondentRepository.findAllActive();
        log.info("Got {} all active Correspondents", correspondents.size(), value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondents;
    }

    Set<CorrespondentWithPrimaryFlag> getCorrespondents(UUID caseUUID) {
        log.debug("Getting all Correspondents for Case: {}", caseUUID);
        Set<CorrespondentWithPrimaryFlag> correspondents = correspondentRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} Correspondents for Case: {}", correspondents.size(), caseUUID, value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondents;
    }

    Correspondent getCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        log.debug("Getting Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
        Correspondent correspondent = correspondentRepository.findByUUID(caseUUID, correspondentUUID);
        if (correspondent != null) {
            log.info("Got Correspondent: {} for Case: {}", correspondentUUID, caseUUID, value(EVENT, CORRESPONDENT_RETRIEVED));
            return correspondent;
        } else {
            log.error("Correspondent: {} for Case UUID: {} not found!", correspondentUUID, caseUUID, value(EVENT, CORRESPONDENT_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Correspondent %s not found for Case: %s", correspondentUUID, caseUUID), CORRESPONDENT_NOT_FOUND);
        }
    }

    Set<CorrespondentTypeDto> getCorrespondentTypes(UUID caseUUID){
        log.debug("Getting all Correspondent Types for Case: {}", caseUUID);
        String caseDataType = caseDataRepository.getCaseType(caseUUID);
        GetCorrespondentTypeResponse correspondentType = infoClient.getCorrespondentType(caseDataType);
        Set<CorrespondentTypeDto> correspondentTypes = correspondentType.getCorrespondentTypes();
        log.info("Got {} Correspondent Types for Case: {}", correspondentType.getCorrespondentTypes().size(), caseUUID, value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondentTypes;
    }

    Set<CorrespondentTypeDto> getSelectableCorrespondentTypes(UUID caseUUID){
        log.debug("Getting all Selectable Correspondent Types for Case: {}", caseUUID);
        String caseDataType = caseDataRepository.getCaseType(caseUUID);
        GetCorrespondentTypeResponse correspondentType = infoClient.getSelectableCorrespondentType(caseDataType);
        Set<CorrespondentTypeDto> correspondentTypes = correspondentType.getCorrespondentTypes();
        log.info("Got {} Selectable Correspondent Types for Case: {}", correspondentType.getCorrespondentTypes().size(), caseUUID, value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondentTypes;
    }

    void createCorrespondent(UUID caseUUID, UUID stageUUID, String correspondentType, String fullname, Address address, String telephone, String email, String reference, String externalKey){
        log.debug("Creating Correspondent of Type: {} for Case: {}", correspondentType, caseUUID);
        Correspondent correspondent = new Correspondent(caseUUID, correspondentType, fullname, address, telephone, email, reference, externalKey);
        try {
            correspondentRepository.save(correspondent);
            auditClient.createCorrespondentAudit(correspondent);

            Set<CorrespondentWithPrimaryFlag> caseCorrespondents = correspondentRepository.findAllByCaseUUID(caseUUID);
            if(!CollectionUtils.isEmpty(caseCorrespondents) && caseCorrespondents.size() == 1){
                caseDataService.updatePrimaryCorrespondent(caseUUID, stageUUID, caseCorrespondents.iterator().next().getUuid());
            }

        } catch (DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Failed to create correspondent %s for Case: %s", correspondent.getUuid(), caseUUID), CORRESPONDENT_CREATE_FAILURE, e);
        }
        log.info("Created Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID, value(EVENT, CORRESPONDENT_CREATED));
    }

    void updateCorrespondent(UUID caseUUID, UUID correspondentUUID, UpdateCorrespondentRequest updateCorrespondentRequest){
        log.debug("Updating Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
        Correspondent correspondent = getCorrespondent(caseUUID, correspondentUUID);
        correspondent.setFullName(updateCorrespondentRequest.getFullname());
        correspondent.setAddress1(updateCorrespondentRequest.getAddress1());
        correspondent.setAddress2(updateCorrespondentRequest.getAddress2());
        correspondent.setAddress3(updateCorrespondentRequest.getAddress3());
        correspondent.setPostcode(updateCorrespondentRequest.getPostcode());
        correspondent.setTelephone(updateCorrespondentRequest.getTelephone());
        correspondent.setEmail(updateCorrespondentRequest.getEmail());
        correspondent.setReference(updateCorrespondentRequest.getReference());

        try {
            correspondentRepository.save(correspondent);
            auditClient.updateCorrespondentAudit(correspondent);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Failed to update correspondent %s for Case: %s", correspondent.getUuid(), caseUUID), CORRESPONDENT_UPDATE_FAILURE, e);
        }
        log.info("Updated Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID, value(EVENT, CORRESPONDENT_UPDATED));
    }

    void deleteCorrespondent(UUID caseUUID, UUID stageUUID, UUID correspondentUUID) {
        log.debug("Deleting Correspondent: {}", correspondentUUID);
        Correspondent correspondent = getCorrespondent(caseUUID, correspondentUUID);
        correspondent.setDeleted(true);
        correspondentRepository.save(correspondent);
        auditClient.deleteCorrespondentAudit(correspondent);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null && correspondentUUID.equals(caseData.getPrimaryCorrespondentUUID())) {
            caseData.setPrimaryCorrespondentUUID(null);
            caseDataRepository.save(caseData);
            auditClient.updateCaseAudit(caseData, stageUUID);
        }
        log.info("Deleted Correspondent: {}", caseUUID, value(EVENT, CORRESPONDENT_DELETED));
    }
}
