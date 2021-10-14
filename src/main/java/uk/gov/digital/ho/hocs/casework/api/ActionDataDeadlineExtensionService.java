package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionDto;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataDeadlineExtensionRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class ActionDataDeadlineExtensionService implements ActionService {

    private final ActionDataDeadlineExtensionRepository extensionRepository;
    private final CaseDataRepository caseDataRepository;
    private final CaseDataService caseDataService;
    private final InfoClient infoClient;
    private final AuditClient auditClient;

    @Autowired
    public ActionDataDeadlineExtensionService(ActionDataDeadlineExtensionRepository extensionRepository, CaseDataRepository caseDataRepository, CaseDataService caseDataService, InfoClient infoClient, AuditClient auditClient) {
        this.extensionRepository = extensionRepository;
        this.caseDataRepository = caseDataRepository;
        this.caseDataService = caseDataService;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
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
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);

        if (!extendFrom.equals("today")) {
            extendFromDate = caseData.getCaseDeadline();
        }

        LocalDate updatedDeadline = infoClient.getCaseDeadline(caseDataType,extendFromDate,extendByNumberOfDays);
        LocalDate updateDeadlineWarning = infoClient.getCaseDeadlineWarning(caseDataType,extendFromDate,extendByNumberOfDays);

        ActionDataDeadlineExtension extensionEntity = new ActionDataDeadlineExtension(
                UUID.randomUUID(),
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
        auditClient.updateCaseAudit(caseData, stageUuid);
        auditClient.createExtensionAudit(createdExtension);
        caseDataService.updateStageDeadlinesForExtension(caseData);

        log.info("Created action:  {} for case: {}, caseType {}", actionData, caseUuid, caseDataType);
    }

    @Override
    public void update(UUID caseUuid, UUID stageUuid, String caseType, UUID actionDataUuid, ActionDataDto actionData) {
        throw new UnsupportedOperationException(String.format("Update of Case Deadline Extension Data is not supported, caseUuid: %s, actionDataUuid: %s", caseUuid, actionDataUuid));
    }
}
