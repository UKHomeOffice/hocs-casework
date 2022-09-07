package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataAppealDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataAppeal;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataAppealsRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_CREATE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_UPDATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_UPDATE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Service
@Slf4j
public class ActionDataAppealsService implements ActionService {

    private final ActionDataAppealsRepository appealsRepository;

    private final CaseDataRepository caseDataRepository;

    private final InfoClient infoClient;

    private final AuditClient auditClient;

    @Autowired
    public ActionDataAppealsService(ActionDataAppealsRepository appealsRepository,
                                    CaseDataRepository caseDataRepository,
                                    InfoClient infoClient,
                                    AuditClient auditClient) {
        this.appealsRepository = appealsRepository;
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
    }

    @Override
    public String getServiceMapKey() {
        return "appeals";
    }

    public UUID createAppeal(UUID caseUuid, UUID stageUuid, ActionDataAppealDto appealDto) {

        log.debug("Received request to create Appeal: {} for case: {}, stage: {}", appealDto, caseUuid, stageUuid);
        UUID appealUuid = appealDto.getCaseTypeActionUuid();

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseData.getType(),
            appealDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("No Case Type Action found for actionId: %s", appealUuid), ACTION_DATA_CREATE_FAILURE);
        }

        if (hasMaxActiveRequests(caseTypeActionDto, caseUuid)) {
            String msg = String.format(
                "The maximum number of 'Pending' requests of type: %s already exist for caseId: %s",
                caseTypeActionDto.getActionLabel(), caseUuid);
            log.error(msg);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, msg);
        }

        ActionDataAppeal appealEntity = new ActionDataAppeal(appealDto.getCaseTypeActionUuid(),
            caseTypeActionDto.getActionLabel(), caseTypeActionDto.getActionSubtype(), caseData.getType(), caseUuid,
            appealDto.getStatus(), appealDto.getDateSentRMS(), appealDto.getOutcome(), appealDto.getComplexCase(),
            appealDto.getNote(), appealDto.getAppealOfficerData(), appealDto.getDocument());

        ActionDataAppeal createdAppealEntity = appealsRepository.save(appealEntity);
        auditClient.createAppealAudit(createdAppealEntity, caseTypeActionDto);
        log.info("Created Action: {}  for Case: {}", createdAppealEntity, caseData.getUuid(),
            value(EVENT, ACTION_DATA_CREATE_SUCCESS));

        return createdAppealEntity.getUuid();
    }

    public void updateAppeal(UUID caseUuid, UUID actionEntityId, ActionDataAppealDto appealDto) {

        log.debug("Received request to update Appeal: {} for case: {}", appealDto, caseUuid);

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseData.getType(),
            appealDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("No Case Type Action found for actionId: %s", actionEntityId),
                ACTION_DATA_UPDATE_FAILURE);
        }

        ActionDataAppeal existingAppealData = appealsRepository.findByUuidAndCaseDataUuid(actionEntityId, caseUuid);
        if (existingAppealData == null) {
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("Action with id:  %s does not exist.", actionEntityId), ACTION_DATA_UPDATE_FAILURE);
        }

        existingAppealData.setStatus(appealDto.getStatus());
        existingAppealData.setDateSentRMS(appealDto.getDateSentRMS());
        existingAppealData.setOutcome(appealDto.getOutcome());
        existingAppealData.setComplexCase(appealDto.getComplexCase());
        existingAppealData.setNote(appealDto.getNote());
        existingAppealData.setAppealOfficerData(appealDto.getAppealOfficerData());
        existingAppealData.setDocument(appealDto.getDocument());

        ActionDataAppeal updatedAppealEntity = appealsRepository.save(existingAppealData);

        auditClient.updateAppealAudit(updatedAppealEntity, caseTypeActionDto);
        log.info("Updated Action: {}  for Case: {}", appealDto, caseData.getUuid(),
            value(EVENT, ACTION_DATA_UPDATE_SUCCESS));

    }

    /**
     * @param caseUUID
     *
     * @return
     */
    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        List<ActionDataAppeal> appeals = appealsRepository.findAllByCaseDataUuid(caseUUID);
        log.info("Returning {} Appeals for caseId: {}", appeals.size(), caseUUID);
        return appeals.stream().map(appeal -> new ActionDataAppealDto(appeal.getUuid(), appeal.getCaseTypeActionUuid(),
            appeal.getActionSubtype(), appeal.getCaseTypeActionLabel(), appeal.getStatus(), appeal.getDateSentRMS(),
            appeal.getOutcome(), appeal.getComplexCase(), appeal.getNote(), appeal.getAppealOfficerData(),
            appeal.getDocument())).collect(Collectors.toList());
    }

    private boolean hasMaxActiveRequests(CaseTypeActionDto caseTypeActionDto, UUID caseUUID) {
        List<ActionDataAppeal> existing = appealsRepository.findAllByCaseTypeActionUuidAndCaseDataUuid(
            caseTypeActionDto.getUuid(), caseUUID);
        return existing.stream().filter(
            appeal -> !appeal.getStatus().equals("Complete")).count() >= caseTypeActionDto.getMaxConcurrentEvents();
    }

}
