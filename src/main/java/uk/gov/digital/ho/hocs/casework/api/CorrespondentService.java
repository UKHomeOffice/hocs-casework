package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.api.utils.CorrespondentTypeNameDecorator;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENTS_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_DELETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_UPDATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Slf4j
@Service
public class CorrespondentService {

    private final CorrespondentRepository correspondentRepository;

    private final CaseDataRepository caseDataRepository;

    private final CaseDataService caseDataService;

    private final CorrespondentTypeNameDecorator correspondentTypeNameDecorator;

    private final AuditClient auditClient;

    private final InfoClient infoClient;

    @Autowired
    public CorrespondentService(CorrespondentRepository correspondentRepository,
                                CaseDataRepository caseDataRepository,
                                AuditClient auditClient,
                                InfoClient infoClient,
                                CaseDataService caseDataService,
                                CorrespondentTypeNameDecorator correspondentTypeNameDecorator) {
        this.correspondentRepository = correspondentRepository;
        this.caseDataRepository = caseDataRepository;
        this.auditClient = auditClient;
        this.infoClient = infoClient;
        this.caseDataService = caseDataService;
        this.correspondentTypeNameDecorator = correspondentTypeNameDecorator;
    }

    Set<Correspondent> getAllCorrespondents(boolean includeDeleted) {
        log.debug("Getting all active Correspondents");

        Set<Correspondent> correspondents = includeDeleted
            ? correspondentRepository.findAll()
            :correspondentRepository.findAllActive();

        log.info("Got {} all active Correspondents", correspondents.size(), value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondents;
    }

    Stream<String> streamCorrespondentOutlineJson(boolean includeDeleted) {
        log.debug("Getting all active Correspondents");

        return includeDeleted
            ? correspondentRepository.findAllUuidToNameMappingJson()
            : correspondentRepository.findActiveUuidToNameMappingJson();
    }

    Set<CorrespondentWithPrimaryFlag> getCorrespondents(UUID caseUUID) {
        log.debug("Getting all Correspondents for Case: {}", caseUUID);

        Set<Correspondent> correspondents = correspondentRepository.findAllByCaseUUID(caseUUID);

        if (correspondents.isEmpty()) {
            return Collections.emptySet();
        }

        CaseData caseData = caseDataService.getCaseData(caseUUID);

        correspondents = correspondentTypeNameDecorator.addCorrespondentTypeName(getCorrespondentTypes(caseUUID),
            correspondents);

        var correspondentsWithFlag = correspondents.stream().map(
            correspondent -> new CorrespondentWithPrimaryFlag(correspondent,
                correspondent.getUuid().equals(caseData.getPrimaryCorrespondentUUID()))).collect(Collectors.toSet());

        log.info("Got {} Correspondents for Case: {}", correspondents.size(), caseUUID,
            value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondentsWithFlag;
    }

    Correspondent getCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        log.debug("Getting Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
        Correspondent correspondent = correspondentRepository.findByUUID(caseUUID, correspondentUUID);
        if (correspondent!=null) {
            correspondentTypeNameDecorator.addCorrespondentTypeName(getCorrespondentTypes(caseUUID), correspondent);

            log.info("Got Correspondent: {} for Case: {}", correspondentUUID, caseUUID,
                value(EVENT, CORRESPONDENT_RETRIEVED));
            return correspondent;
        } else {
            log.error("Correspondent: {} for Case UUID: {} not found!", correspondentUUID, caseUUID,
                value(EVENT, CORRESPONDENT_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("Correspondent %s not found for Case: %s", correspondentUUID, caseUUID),
                CORRESPONDENT_NOT_FOUND);
        }
    }

    Set<CorrespondentTypeDto> getCorrespondentTypes(UUID caseUUID) {
        log.debug("Getting all Correspondent Types for Case: {}", caseUUID);
        String caseDataType = caseDataRepository.getCaseType(caseUUID);
        GetCorrespondentTypeResponse correspondentType = infoClient.getCorrespondentType(caseDataType);
        Set<CorrespondentTypeDto> correspondentTypes = correspondentType.getCorrespondentTypes();
        log.info("Got {} Correspondent Types for Case: {}", correspondentType.getCorrespondentTypes().size(), caseUUID,
            value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondentTypes;
    }

    Set<CorrespondentTypeDto> getSelectableCorrespondentTypes(UUID caseUUID) {
        log.debug("Getting all Selectable Correspondent Types for Case: {}", caseUUID);
        String caseDataType = caseDataRepository.getCaseType(caseUUID);
        GetCorrespondentTypeResponse correspondentType = infoClient.getSelectableCorrespondentType(caseDataType);
        Set<CorrespondentTypeDto> correspondentTypes = correspondentType.getCorrespondentTypes();
        log.info("Got {} Selectable Correspondent Types for Case: {}", correspondentType.getCorrespondentTypes().size(),
            caseUUID, value(EVENT, CORRESPONDENTS_RETRIEVED));
        return correspondentTypes;
    }

    void createCorrespondent(UUID caseUUID,
                             UUID stageUUID,
                             String correspondentType,
                             String fullname,
                             String organisation,
                             Address address,
                             String telephone,
                             String email,
                             String reference,
                             String externalKey) {
        log.debug("Creating Correspondent of Type: {} for Case: {}", correspondentType, caseUUID);
        Correspondent correspondent = new Correspondent(caseUUID, correspondentType, fullname, organisation, address,
            telephone, email, reference, externalKey);
        try {
            correspondentRepository.save(correspondent);
            auditClient.createCorrespondentAudit(correspondent);

            Set<Correspondent> caseCorrespondents = correspondentRepository.findAllByCaseUUID(caseUUID);

            if (caseCorrespondents.size()==1) {
                caseDataService.updatePrimaryCorrespondent(caseUUID, stageUUID,
                    caseCorrespondents.stream().findFirst().get().getUuid());
            }
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format(
                    "Failed to create correspondent %s for Case: %s - %s",
                    correspondent.getUuid(),
                    caseUUID,
                    e.getMessage()
                ),
                CORRESPONDENT_CREATE_FAILURE
            );
        }
        log.info("Created Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID,
            value(EVENT, CORRESPONDENT_CREATED));
    }

    void createCorrespondent(UUID caseUUID, Correspondent correspondent) {
        createCorrespondent(caseUUID, null, correspondent.getCorrespondentType(), correspondent.getFullName(),
            correspondent.getOrganisation(),
            Address.builder().address1(correspondent.getAddress1()).address2(correspondent.getAddress2()).address3(
                correspondent.getAddress3()).postcode(correspondent.getPostcode()).country(
                correspondent.getCountry()).build(), correspondent.getTelephone(), correspondent.getEmail(),
            correspondent.getReference(), correspondent.getExternalKey());

    }

    void updateCorrespondent(UUID caseUUID,
                             UUID correspondentUUID,
                             UpdateCorrespondentRequest updateCorrespondentRequest) {
        log.debug("Updating Correspondent: {} for Case: {}", correspondentUUID, caseUUID);
        Correspondent correspondent = getCorrespondent(caseUUID, correspondentUUID);
        correspondent.setFullName(updateCorrespondentRequest.getFullname());
        correspondent.setOrganisation(updateCorrespondentRequest.getOrganisation());
        correspondent.setAddress1(updateCorrespondentRequest.getAddress1());
        correspondent.setAddress2(updateCorrespondentRequest.getAddress2());
        correspondent.setAddress3(updateCorrespondentRequest.getAddress3());
        correspondent.setPostcode(updateCorrespondentRequest.getPostcode());
        correspondent.setCountry(updateCorrespondentRequest.getCountry());
        correspondent.setTelephone(updateCorrespondentRequest.getTelephone());
        correspondent.setEmail(updateCorrespondentRequest.getEmail());
        correspondent.setReference(updateCorrespondentRequest.getReference());

        try {
            correspondentRepository.save(correspondent);
            auditClient.updateCorrespondentAudit(correspondent);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format(
                    "Failed to update correspondent %s for Case: %s - %s",
                    correspondent.getUuid(),
                    caseUUID,
                    e.getMessage()
                ),
                CORRESPONDENT_UPDATE_FAILURE
            );
        }
        log.info("Updated Correspondent: {} for Case: {}", correspondent.getUuid(), caseUUID,
            value(EVENT, CORRESPONDENT_UPDATED));
    }

    void deleteCorrespondent(UUID caseUUID, UUID stageUUID, UUID correspondentUUID) {
        log.debug("Deleting Correspondent: {}", correspondentUUID);
        Correspondent correspondent = getCorrespondent(caseUUID, correspondentUUID);
        correspondent.setDeleted(true);
        correspondentRepository.save(correspondent);
        auditClient.deleteCorrespondentAudit(correspondent);
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData!=null && correspondentUUID.equals(caseData.getPrimaryCorrespondentUUID())) {
            caseData.setPrimaryCorrespondentUUID(null);
            caseDataRepository.save(caseData);
            auditClient.updateCaseAudit(caseData, stageUUID);
        }
        log.info("Deleted Correspondent: {}", caseUUID, value(EVENT, CORRESPONDENT_DELETED));
    }

    public void copyCorrespondents(UUID fromCase, UUID toCase) {

        // get the case correspondents
        Set<CorrespondentWithPrimaryFlag> correspondents = getCorrespondents(fromCase);

        // save the primary first and the existing logic will assign it within the case
        Optional<CorrespondentWithPrimaryFlag> primaryCorrespondent = correspondents.stream().filter(
            CorrespondentWithPrimaryFlag::getIsPrimary).findFirst();
        primaryCorrespondent.ifPresent(
            correspondentWithPrimaryFlag -> createCorrespondent(toCase, correspondentWithPrimaryFlag)
        );

        // save the rest
        correspondents.stream().filter(correspondent -> !correspondent.getIsPrimary()).forEach(
            correspondent -> createCorrespondent(toCase, correspondent));

    }

}
