package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionDto;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataDeadlineExtensionRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_FOUND;

@Service
@Slf4j
public class ActionDataDeadlineExtensionService implements ActionService {

    private final ActionDataDeadlineExtensionRepository extensionRepository;
    private final CaseDataRepository caseDataRepository;
    private final CaseDataService caseDataService;
    private final InfoClient infoClient;
    private final AuditClient auditClient;
    private final CaseNoteService caseNoteService;

    private static final String CREATE_CASE_NOTE_KEY = "EXTENSION";

    @Autowired
    public ActionDataDeadlineExtensionService(ActionDataDeadlineExtensionRepository extensionRepository, CaseDataRepository caseDataRepository, CaseDataService caseDataService, InfoClient infoClient, AuditClient auditClient, CaseNoteService caseNoteService) {
        this.extensionRepository = extensionRepository;
        this.caseDataRepository = caseDataRepository;
        this.caseDataService = caseDataService;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.caseNoteService = caseNoteService;
    }

    @Override
    public String getActionName() {
        return ActionDataDeadlineExtensionDto.class.getSimpleName();
    }

    @Override
    public void create(UUID caseUuid, UUID stageUuid, String caseDataType, ActionDataDto actionData) {

        ActionDataDeadlineExtensionDto extensionDto = (ActionDataDeadlineExtensionDto) actionData;
        log.debug("Received request to create action: {} for case: {}, stage: {}, caseType: {}", extensionDto, caseUuid, stageUuid, caseDataType);

        int extendByNumberOfDays = extensionDto.getExtendBy();
        String extendFrom = extensionDto.getExtendFrom();
        LocalDate extendFromDate = LocalDate.now();
        UUID extensionTypeUuid = extensionDto.getCaseTypeActionUuid();


        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseDataType, extensionTypeUuid);
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("No Case Type Action found for actionId: %s", extensionTypeUuid), ACTION_DATA_CREATE_FAILURE);
        }

        CaseData caseData = caseDataService.getCase(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        if (!extendFrom.equals("today")) {
            extendFromDate = caseData.getCaseDeadline();
        }

        LocalDate updatedDeadline = infoClient.getCaseDeadline(caseDataType,extendFromDate,extendByNumberOfDays);
        LocalDate updateDeadlineWarning = infoClient.getCaseDeadlineWarning(caseDataType,extendFromDate,extendByNumberOfDays);

        ActionDataDeadlineExtension extensionEntity = new ActionDataDeadlineExtension(
                extensionDto.getCaseTypeActionUuid(),
                extensionDto.getCaseTypeActionLabel(),
                caseData.getType(),
                caseUuid,
                caseData.getCaseDeadline(),
                updatedDeadline,
                extensionDto.getNote()
        );

        caseData.setCaseDeadline(updatedDeadline);
        caseData.setCaseDeadlineWarning(updateDeadlineWarning);

        ActionDataDeadlineExtension createdExtension = extensionRepository.save(extensionEntity);
        caseDataRepository.save(caseData);
        caseNoteService.createCaseNote(caseUuid, CREATE_CASE_NOTE_KEY, extensionDto.getNote());
        auditClient.updateCaseAudit(caseData, stageUuid);
        auditClient.createExtensionAudit(createdExtension);
        caseDataService.updateStageDeadlinesForExtension(caseData);

        log.info("Created action:  {} for case: {}, caseType {}", actionData, caseUuid, caseDataType);
    }

    @Override
    public void update(UUID caseUuid, UUID stageUuid, String caseType, ActionDataDto actionData) {
        String msg = (String.format("Update of Case Deadline Extension Data is not supported, caseUuid: %s, actionData: %s", caseUuid, actionData.toString()));
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }
}
