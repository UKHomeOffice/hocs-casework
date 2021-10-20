package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class ActionDataAppealsService implements ActionService {

    private final ActionDataAppealsRepository appealsRepository;
    private final CaseDataRepository caseDataRepository;
    private final InfoClient infoClient;
    private final AuditClient auditClient;
    private final CaseNoteService caseNoteService;

    private static final String CREATE_CASE_NOTE_KEY = "APPEAL_CREATED";
    private static final String UPDATE_CASE_NOTE_KEY = "APPEAL_UPDATED";

    @Autowired
    public ActionDataAppealsService(ActionDataAppealsRepository appealsRepository, CaseDataRepository caseDataRepository, InfoClient infoClient, AuditClient auditClient, CaseNoteService caseNoteService) {
        this.appealsRepository = appealsRepository;
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.caseNoteService = caseNoteService;
    }

    @Override
    public String getServiceDtoTypeKey() {
        return ActionDataAppealDto.class.getSimpleName();
    }

    @Override
    public String getServiceMapKey() {
        return "appeals";
    }

    @Override
    public void create(UUID caseUuid, UUID stageUuid, String caseDataType, ActionDataDto actionData) {

        ActionDataAppealDto appealDto = (ActionDataAppealDto) actionData;
        log.debug("Received request to create action: {} for case: {}, stage: {}, caseType: {}", appealDto, caseUuid, stageUuid, caseDataType);
        UUID appealUuid = appealDto.getCaseTypeActionUuid();

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseDataType, appealDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("No Case Type Action found for actionId: %s", appealUuid), ACTION_DATA_CREATE_FAILURE);
        }

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        ActionDataAppeal appealEntity = new ActionDataAppeal(
                appealDto.getCaseTypeActionUuid(),
                appealDto.getCaseTypeActionLabel(),
                caseDataType,
                caseUuid,
                appealDto.getData()
        );

        ActionDataAppeal createdAppealEntity = appealsRepository.save(appealEntity);
        caseNoteService.createCaseNote(caseUuid, CREATE_CASE_NOTE_KEY, appealEntity.getCaseTypeActionLabel());
        auditClient.createAppealAudit(createdAppealEntity);
        log.info("Created Action: {}  for Case: {}", createdAppealEntity, caseData.getUuid(), value(EVENT, ACTION_DATA_CREATE_SUCCESS) );
    }

    @Override
    public void update(UUID caseUuid, UUID stageUuid, String caseDataType, UUID actionEntityId, ActionDataDto updatedActionData) {

        ActionDataAppealDto appealDto = (ActionDataAppealDto) updatedActionData;
        log.debug("Received request to update action: {} for case: {}, stage: {}, caseDataType: {}", appealDto, caseUuid, stageUuid, caseDataType);

        UUID appealUuid = updatedActionData.getUuid();

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseDataType, appealDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("No Case Type Action found for actionId: %s", appealUuid), ACTION_DATA_UPDATE_FAILURE);
        }

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        ActionDataAppeal existingAppealData = appealsRepository.findByUuidAndCaseDataUuid(appealUuid, caseUuid);
        if (existingAppealData == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Action with id:  %s does not exist.", appealUuid), ACTION_DATA_UPDATE_FAILURE);
        }

        // todo: make more like the appeal form on front end.
        existingAppealData.setData((appealDto.getData()));
        ActionDataAppeal updatedAppealEntity = appealsRepository.save(existingAppealData);

        caseNoteService.createCaseNote(caseUuid, UPDATE_CASE_NOTE_KEY, updatedAppealEntity.getCaseTypeActionLabel());
        auditClient.updateAppealAudit(updatedAppealEntity);
        log.info("Updated Action: {}  for Case: {}", updatedActionData, caseData.getUuid(), value(EVENT, ACTION_DATA_UPDATE_SUCCESS) );

    }

    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        List<ActionDataAppeal> appeals = appealsRepository.findAllByCaseDataUuid(caseUUID);
        log.info("Returning {} Appeals for caseId: {}", appeals.size(), caseUUID);
        return appeals.stream().map(appeal -> new ActionDataAppealDto(
                appeal.getUuid(),
                appeal.getCaseTypeActionUuid(),
                appeal.getCaseTypeActionLabel(),
                appeal.getData()
        )).collect(Collectors.toList());
    }
}
