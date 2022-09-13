package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionInboundDto;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionOutboundDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataDeadlineExtensionRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.api.utils.CaseDeadlineHelpers.overrideStageDeadlines;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_FOUND;

@Service
@Slf4j
public class ActionDataDeadlineExtensionService implements ActionService {

    private final ActionDataDeadlineExtensionRepository extensionRepository;

    private final CaseDataRepository caseDataRepository;

    private final InfoClient infoClient;

    private final AuditClient auditClient;

    private final DeadlineService deadlineService;

    private final Clock clock;

    @Autowired
    public ActionDataDeadlineExtensionService(ActionDataDeadlineExtensionRepository extensionRepository,
                                              CaseDataRepository caseDataRepository,
                                              InfoClient infoClient,
                                              AuditClient auditClient,
                                              DeadlineService deadlineService,
                                              Clock clock) {
        this.extensionRepository = extensionRepository;
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.deadlineService = deadlineService;
        this.clock = clock;
    }

    @Override
    public String getServiceMapKey() {
        return "extensions";
    }

    public boolean hasExtensions(UUID caseUUID) {
        return extensionRepository.existsDistinctByCaseDataUuid(caseUUID);
    }

    public void createExtension(UUID caseUuid, UUID stageUuid, ActionDataDeadlineExtensionInboundDto extensionDto) {

        log.debug("Received request to create extension: {} for case: {}, stage: {}", extensionDto, caseUuid,
            stageUuid);

        int extendByNumberOfDays = extensionDto.getExtendBy();

        ExtendFrom extendFrom;
        try {
            extendFrom = ExtendFrom.valueOf(extensionDto.getExtendFrom());
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        LocalDate extendFromDate = LocalDate.now(clock);
        UUID extensionTypeUuid = extensionDto.getCaseTypeActionUuid();

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData==null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseData.getType(), extensionTypeUuid);

        String hydratedReasonList = Arrays.stream(extensionDto.getReasons().split(",")).map(
            reasonSimpleName -> infoClient.getEntityBySimpleName(reasonSimpleName).getData().get("title")).collect(
            Collectors.joining(", "));

        if (caseTypeActionDto==null) {
            throw new ApplicationExceptions.EntityNotFoundException(
                String.format("No Case Type Action exists for actionId: %s", extensionTypeUuid),
                ACTION_DATA_CREATE_FAILURE);
        }

        if (hasMaxRequests(caseTypeActionDto, caseUuid)) {
            String msg = String.format(
                "The maximum number of extensions of type: %s have already been applied for caseId: %s",
                caseTypeActionDto.getActionLabel(), caseUuid);
            log.error(msg);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, msg);
        }

        if (extendFrom!=ExtendFrom.TODAY) {
            extendFromDate = caseData.getCaseDeadline();
        }

        final CaseDataType caseType = infoClient.getCaseType(caseData.getType());
        final int daysUntilDeadline = deadlineService.daysUntilDeadline(extendByNumberOfDays, caseType);

        LocalDate updatedDeadline = deadlineService.calculateWorkingDaysForCaseType(caseData.getType(), extendFromDate,
            extendByNumberOfDays);

        LocalDate updatedDeadlineWarning = deadlineService.calculateWorkingDaysForCaseType(caseData.getType(),
            extendFromDate, Math.max(daysUntilDeadline, 0));

        if (caseData.getCaseDeadline().isAfter(updatedDeadline)) {
            String msg = String.format(
                "CaseId: %s, existing deadline (%s) is later than requested extension (%s). Extension not applied.",
                caseUuid, caseData.getCaseDeadline(), updatedDeadline);
            log.warn(msg);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, msg);
        }

        ActionDataDeadlineExtension extensionEntity = new ActionDataDeadlineExtension(
            extensionDto.getCaseTypeActionUuid(), caseTypeActionDto.getActionLabel(), caseData.getType(), caseUuid,
            caseData.getCaseDeadline(), updatedDeadline, extensionDto.getNote() + "\nReason: " + hydratedReasonList,
            extensionDto.getReasons());

        caseData.setCaseDeadline(updatedDeadline);
        caseData.setCaseDeadlineWarning(updatedDeadlineWarning);

        overrideStageDeadlines(caseData);

        ActionDataDeadlineExtension createdExtension = extensionRepository.save(extensionEntity);

        // add extension indicator - is picked up in workstack Extended column.
        caseData.getDataMap().put("isCaseExtended", "True");
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUuid);
        auditClient.createExtensionAudit(createdExtension);

        log.info("Created action:  {} for case: {}", createdExtension, caseUuid);
    }

    private boolean hasMaxRequests(CaseTypeActionDto caseTypeActionDto, UUID caseUUID) {
        List<ActionDataDeadlineExtension> existingDeadlinesOfMatchingType = extensionRepository.findAllByCaseTypeActionUuidAndCaseDataUuid(
            caseTypeActionDto.getUuid(), caseUUID);
        return existingDeadlinesOfMatchingType.size() >= caseTypeActionDto.getMaxConcurrentEvents();
    }

    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        List<ActionDataDeadlineExtension> extensions = extensionRepository.findAllByCaseDataUuid(caseUUID);
        log.info("Returning {} Extensions for caseId: {}", extensions.size(), caseUUID);
        return extensions.stream().map(extension -> new ActionDataDeadlineExtensionOutboundDto(extension.getUuid(),
            extension.getCaseTypeActionUuid(), extension.getActionSubtype(), extension.getCaseTypeActionLabel(),
            extension.getOriginalDeadline(), extension.getUpdatedDeadline(), extension.getNote())).collect(
            Collectors.toList());
    }

    enum ExtendFrom {
        TODAY,
        CURRENT_DEADLINE
    }

}
