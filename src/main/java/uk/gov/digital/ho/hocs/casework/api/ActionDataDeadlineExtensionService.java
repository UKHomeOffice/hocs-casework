package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionInboundDto;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionOutboundDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataDeadlineExtensionRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_FOUND;

@Service
@Slf4j
public class ActionDataDeadlineExtensionService implements ActionService {

    private final ActionDataDeadlineExtensionRepository extensionRepository;
    private final CaseDataRepository caseDataRepository;
    private final InfoClient infoClient;
    private final AuditClient auditClient;
    private final CaseNoteService caseNoteService;
    private final ObjectMapper objectMapper;

    private static final String CREATE_CASE_NOTE_KEY = "EXTENSION";

    @Autowired
    public ActionDataDeadlineExtensionService(ActionDataDeadlineExtensionRepository extensionRepository, CaseDataRepository caseDataRepository, InfoClient infoClient, AuditClient auditClient, CaseNoteService caseNoteService, ObjectMapper objectMapper) {
        this.extensionRepository = extensionRepository;
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.caseNoteService = caseNoteService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getServiceDtoTypeKey() {
        return ActionDataDeadlineExtensionInboundDto.class.getSimpleName();
    }

    @Override
    public String getServiceMapKey() {
        return "extensions";
    }

    @Override
    public void create(UUID caseUuid, UUID stageUuid, String caseDataType, ActionDataDto actionData) {

        ActionDataDeadlineExtensionInboundDto extensionDto = (ActionDataDeadlineExtensionInboundDto) actionData;
        log.debug("Received request to create action: {} for case: {}, stage: {}, caseType: {}", extensionDto, caseUuid, stageUuid, caseDataType);

        int extendByNumberOfDays = extensionDto.getExtendBy();

        ExtendFrom extendFrom = null;
        try {
            extendFrom = ExtendFrom.valueOf(extensionDto.getExtendFrom());
        } catch (IllegalArgumentException e) {
            String msg = String.format("\"extendFrom\" value invalid: %s", extensionDto.getExtendFrom());
            log.info(msg);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, msg);
        }
        LocalDate extendFromDate = LocalDate.now();
        UUID extensionTypeUuid = extensionDto.getCaseTypeActionUuid();

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseDataType, extensionTypeUuid);
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("No Case Type Action exists for actionId: %s", extensionTypeUuid), ACTION_DATA_CREATE_FAILURE);
        }

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        if (extendFrom != ExtendFrom.TODAY) {
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
        updateStageDeadlines(caseData);

        log.info("Created action:  {} for case: {}, caseType {}", actionData, caseUuid, caseDataType);
    }

    @Override
    public void update(UUID caseUuid, UUID stageUuid, String caseType, UUID actionEntityId, ActionDataDto actionData) {
        String msg = (String.format("Update of Case Deadline Extension Data is not supported, caseUuid: %s, actionData: %s", caseUuid, actionData.toString()));
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        List<ActionDataDeadlineExtension> extensions = extensionRepository.findAllByCaseDataUuid(caseUUID);
        log.info("Returning {} Extensions for caseId: {}", extensions.size(), caseUUID);
        return extensions.stream().map(extension ->
            new ActionDataDeadlineExtensionOutboundDto(
                    extension.getUuid(),
                    extension.getCaseTypeActionUuid(),
                    extension.getCaseTypeActionLabel(),
                    extension.getOriginalDeadline(),
                    extension.getUpdatedDeadline(),
                    extension.getNote()
            )
        ).collect(Collectors.toList());
    }

    // COPIED FROM CaseDataService to avoid cyclic dependency.
    private void updateStageDeadlines(CaseData caseData) {

        if (caseData.getActiveStages() == null) {
            log.warn("Case uuid:{} supplied with null active stages", caseData.getUuid());
            return;
        }

        Map<String, String> dataMap = caseData.getDataMap(objectMapper);
        for (ActiveStage stage : caseData.getActiveStages()) {
            // Try and overwrite the deadlines with inputted values from the data map.
            String overrideDeadline = dataMap.get(String.format("%s_DEADLINE", stage.getStageType()));
            if (overrideDeadline == null) {
                LocalDate dateReceived = caseData.getDateReceived();
                LocalDate caseDeadline = caseData.getCaseDeadline();
                LocalDate caseDeadlineWarning = caseData.getCaseDeadlineWarning();
                LocalDate deadline = infoClient.getStageDeadline(stage.getStageType(), dateReceived, caseDeadline);
                stage.setDeadline(deadline);
                if (caseDeadlineWarning != null) {
                    LocalDate deadlineWarning = infoClient.getStageDeadlineWarning(stage.getStageType(), dateReceived, caseDeadlineWarning);
                    stage.setDeadlineWarning(deadlineWarning);
                }
            } else {
                LocalDate deadline = LocalDate.parse(overrideDeadline);
                stage.setDeadline(deadline);
            }
        }
    }

    enum ExtendFrom {
        TODAY("today"),
        DATE_RECEIVED("DateReceived");

        private final String dataSchemaName;

        ExtendFrom(String dataSchemaName) {
            this.dataSchemaName = dataSchemaName;
        }
    }
}
