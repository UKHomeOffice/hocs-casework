package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataSuspendDto;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataSuspension;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.SuspensionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.api.utils.ActionDataHelpers.updateStageDeadlines;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_FOUND;

@Slf4j
@Service
public class ActionDataSuspendService implements ActionService {

    private final CaseDataRepository caseDataRepository;
    private final SuspensionRepository suspensionRepository;
    private final InfoClient infoClient;
    private final AuditClient auditClient;

    @Autowired
    public ActionDataSuspendService(CaseDataRepository caseDataRepository, SuspensionRepository suspensionRepository, InfoClient infoClient, AuditClient auditClient) {
        this.caseDataRepository = caseDataRepository;
        this.suspensionRepository = suspensionRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
    }

    @Override
    public String getServiceMapKey() {
        return "suspensions";
    }

    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        Optional<List<ActionDataSuspension>> maybeSuspensions = suspensionRepository.findAllByCaseDataUuid(caseUUID);
        if (maybeSuspensions.isEmpty()) return List.of();
        return maybeSuspensions.get().stream().map(suspension ->
                        new ActionDataSuspendDto(
                            suspension.getUuid(),
                            suspension.getCaseTypeActionUuid(),
                            suspension.getActionSubtype(),
                            suspension.getCaseTypeActionLabel(),
                            suspension.getDateSuspensionApplied(),
                            suspension.getDateSuspensionRemoved()
                    )).collect(Collectors.toList());
    }

    public void suspend(UUID caseUUID, UUID existingStageUuid, ActionDataSuspendDto suspendDto) {

        log.debug("Request to suspend case {} received", caseUUID);

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUUID), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseData.getType(), suspendDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("No Case Type Action found for actionId: %s", suspendDto.getCaseTypeActionUuid()), ACTION_DATA_CREATE_FAILURE);
        }

        if (hasMaxActiveRequests(caseTypeActionDto, caseUUID)) {
            String msg = String.format("The maximum number of 'Pending' requests of type: %s already exist for caseId: %s", caseTypeActionDto.getActionLabel(), caseUUID);
            log.error(msg);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,msg);
        }

        // Save the suspension event.
        ActionDataSuspension suspensionEntity = new ActionDataSuspension(
                suspendDto.getCaseTypeActionUuid(),
                suspendDto.getCaseSubtype(),
                suspendDto.getCaseTypeActionLabel(),
                caseData.getType(),
                caseUUID,
                suspendDto.getDateSuspensionApplied(),
                null
        );

        suspensionRepository.save(suspensionEntity);
        auditClient.createSuspensionAudit(suspensionEntity);

        // update case data
        caseData.getDataMap().put("suspended", "true");
        caseData.setCaseDeadline(LocalDate.MAX);
        caseData.setCaseDeadlineWarning(LocalDate.MAX);
        updateStageDeadlines(caseData);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, existingStageUuid);

        log.info("Case {} has successfully been suspended with type {}", caseUUID, caseTypeActionDto.getUuid());
    }

    public void unsuspend(UUID caseUUID, UUID existingStageUuid, UUID existingSuspensionUUID) {
        log.debug("Request to un-suspend case {} received", caseUUID);

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUUID), CASE_NOT_FOUND);
        }

        Optional<ActionDataSuspension> existingSuspension = suspensionRepository.findByUuidAndCaseDataUuid(existingSuspensionUUID, caseUUID);

        existingSuspension.ifPresentOrElse((ActionDataSuspension suspension) -> {

            suspension.setDateSuspensionRemoved(LocalDate.now());
            suspensionRepository.save(suspension);
            auditClient.updateSuspensionAudit(suspension);

            caseData.update("suspended", "false");
            caseDataRepository.save(caseData);
            auditClient.updateCaseAudit(caseData, existingStageUuid);

        }, () -> {
            String msg = String.format("No suspension action exists for id %s in case %s", existingSuspensionUUID, caseUUID);
            log.error(msg);
            throw new ApplicationExceptions.EntityNotFoundException(msg, LogEvent.ACTION_DATA_UPDATE_FAILURE);
        });
    }

    private boolean hasMaxActiveRequests(CaseTypeActionDto caseTypeActionDto, UUID caseUUID) {
        Optional<List<ActionDataSuspension>> caseSuspensions = suspensionRepository.findAllByCaseDataUuidAndCaseTypeActionUuidAndDateSuspensionRemovedIsNull(caseUUID, caseTypeActionDto.getCaseTypeUuid());
        return caseSuspensions.isPresent() && caseSuspensions.get().size() >= caseTypeActionDto.getMaxConcurrentEvents();
    }
}
