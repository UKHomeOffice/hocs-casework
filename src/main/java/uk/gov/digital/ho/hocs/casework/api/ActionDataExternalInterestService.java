package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataExternalInterestInboundDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataExternalInterestOutboundDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataExternalInterestRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_UPDATE_FAILURE;

@Service
@Slf4j
public class ActionDataExternalInterestService implements ActionService {
    private final ActionDataExternalInterestRepository actionDataExternalInterestRepository;
    private final CaseDataRepository caseDataRepository;
    private final InfoClient infoClient;
    private final AuditClient auditClient;

    @Autowired
    public ActionDataExternalInterestService(ActionDataExternalInterestRepository actionDataExternalInterestRepository,
                                             CaseDataRepository caseDataRepository,
                                             InfoClient infoClient,
                                             AuditClient auditClient) {
        this.actionDataExternalInterestRepository = actionDataExternalInterestRepository;
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
    }

    @Override
    public String getServiceMapKey() {
        return "recordInterest";
    }

    public void createExternalInterest(UUID caseUuid, UUID stageUuid, ActionDataExternalInterestInboundDto interestDto) {

        log.debug("Received request to create external " +
                "interest: {} for case: {}, stage: {}", interestDto, caseUuid, stageUuid);


        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto
                = infoClient.getCaseTypeActionByUuid(caseData.getType(), interestDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(
                    String.format("No Case Type Action found for actionId: %s", interestDto.getUuid())
                    , ACTION_DATA_CREATE_FAILURE);
        }


        ActionDataExternalInterest actionDataExternalInterest = new ActionDataExternalInterest(
                interestDto.getCaseTypeActionUuid(),
                interestDto.getCaseTypeActionLabel(),
                caseData.getType(),
                caseUuid,
                interestDto.getInterestedPartyType(),
                interestDto.getDetailsOfInterest()
        );

        EntityDto<Map<String, String>> partyType = infoClient.getEntityBySimpleName(actionDataExternalInterest.getPartyType());
        String partyTitle = partyType.getData().get("title");

        actionDataExternalInterestRepository.save(actionDataExternalInterest);

        auditClient.createExternalInterestAudit(actionDataExternalInterest);

        log.info("Created action:  {} for case: {}", interestDto, caseUuid);
    }

    public void updateExternalInterest(UUID caseUuid, UUID actionEntityId, ActionDataExternalInterestInboundDto updateExternalInterestDto) {
        log.debug("Received request to update external interest: {} for case: {}", updateExternalInterestDto, caseUuid);

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseData.getType(), updateExternalInterestDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException
                    (String.format("No Case Type Action found for actionId: %s", actionEntityId), ACTION_DATA_UPDATE_FAILURE);
        }

        ActionDataExternalInterest existingExternalInterestData =
                actionDataExternalInterestRepository.findByUuidAndCaseDataUuid(actionEntityId, caseUuid);

        if (existingExternalInterestData == null) {
            throw new ApplicationExceptions.EntityNotFoundException
                    (String.format("Action with id: %s does not exist.", actionEntityId), ACTION_DATA_UPDATE_FAILURE);
        }

        existingExternalInterestData.setDetailsOfInterest(updateExternalInterestDto.getDetailsOfInterest());
        existingExternalInterestData.setPartyType(updateExternalInterestDto.getInterestedPartyType());

        EntityDto<Map<String, String>> partyType = infoClient.getEntityBySimpleName(existingExternalInterestData.getPartyType());
        String partyTitle = partyType.getData().get("title");

        actionDataExternalInterestRepository.save(existingExternalInterestData);

        auditClient.updateExternalInterestAudit(existingExternalInterestData);
    }

    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        List<ActionDataExternalInterest> externalInterests =
                actionDataExternalInterestRepository.findAllByCaseDataUuid(caseUUID);
        log.info("Returning {} Extensions for caseIsd: {}", externalInterests.size(), caseUUID);

        return externalInterests.stream().map(interest ->
                {
                    Map<String, String> interestedPartyEntity =
                            infoClient.getEntityBySimpleName(interest.getPartyType()).getData();

                    return new ActionDataExternalInterestOutboundDto(
                            interest.getUuid(),
                            interest.getCaseTypeActionUuid(),
                            interest.getAction_subtype(),
                            interest.getCaseTypeActionLabel(),
                            interest.getPartyType(),
                            interestedPartyEntity,
                            interest.getDetailsOfInterest());
                }
        ).collect(Collectors.toList());
    }
}
