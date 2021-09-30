package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@RestController
class CaseDataResource {

    private final CaseDataService caseDataService;
    private final CaseNoteService caseNoteService;

    @Autowired
    public CaseDataResource(
            @Qualifier("CaseDataService") CaseDataService caseDataService,
            CaseNoteService caseNoteService) {
        this.caseDataService = caseDataService;
        this.caseNoteService = caseNoteService;
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @PostMapping(value = "/case")
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CaseData caseData = caseDataService.createCase(request.getType(), request.getData(), request.getDateRecieved(), request.getFromCaseUUID());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}")
    public ResponseEntity<GetCaseResponse> getCase(@PathVariable UUID caseUUID, @RequestParam("full") Optional<Boolean> full) {
        CaseData caseData = caseDataService.getCase(caseUUID);
        return ResponseEntity.ok(GetCaseResponse.from(caseData, full.orElse(false)));
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @DeleteMapping(value = "/case/{caseUUID}/{deleted}")
    public ResponseEntity<Void> deleteCase(@PathVariable UUID caseUUID, @PathVariable Boolean deleted) {
        caseDataService.deleteCase(caseUUID, deleted);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/timeline")
    public ResponseEntity<Set<TimelineItemDto>> getCaseTimeline(@PathVariable UUID caseUUID) {
        Stream<TimelineItem> timeline = caseDataService.getCaseTimeline(caseUUID);
        return ResponseEntity.ok(timeline.map(TimelineItemDto::from).collect(Collectors.toSet()));
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/summary")
    public ResponseEntity<GetCaseSummaryResponse> getCaseSummary(@PathVariable UUID caseUUID) {
        CaseSummary caseSummary = caseDataService.getCaseSummary(caseUUID);
        return ResponseEntity.ok(GetCaseSummaryResponse.from(caseSummary));
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/calculateTotals")
    public ResponseEntity<Map<String, String>> calculateTotals(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody String listName) {
        Map<String, String> totals = caseDataService.calculateTotals(caseUUID, stageUUID, listName);
        return ResponseEntity.ok(totals);
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/data")
    public ResponseEntity<Void> updateCaseData(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateCaseDataRequest request) {
        caseDataService.updateCaseData(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok().build();
    }

    @Allocated(allocatedTo = AllocationLevel.USER_OR_TEAM)
    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}/extension")
    ResponseEntity<GetCaseReferenceResponse> applyExtension(@PathVariable UUID caseUUID,
                                                            @PathVariable UUID stageUUID,
                                                            @RequestBody ApplyExtensionRequest request) {
        try {
            caseDataService.applyExtension(caseUUID, stageUUID, request.getType(), request.getCaseNote());
            caseNoteService.createCaseNote(caseUUID, "EXTENSION", request.getCaseNote());

            return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
        } catch (NoSuchElementException e) {
            log.error("Failed to apply extension to case: {}, invalid extension type", caseUUID, value(EVENT, EXTENSION_APPLY_FAILED));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Failed to apply extension to case: {}", caseUUID, value(EVENT, EXTENSION_APPLY_FAILED));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/dateReceived")
    public ResponseEntity<Void> updateCaseDateReceived(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody LocalDate dateReceived) {
        caseDataService.updateDateReceived(caseUUID, stageUUID, dateReceived, 0);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/dispatchDeadlineDate")
    public ResponseEntity<Void> updateCaseDispatchDeadlineDate(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody LocalDate dispatchDate) {
        caseDataService.updateDispatchDeadlineDate(caseUUID, stageUUID, dispatchDate);
        return ResponseEntity.ok().build();
    }

    // TODO: Add test
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/deadline")
    public ResponseEntity<Void> updateCaseDeadlineDays(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody int days) {
        caseDataService.updateDateReceived(caseUUID, stageUUID, null, days);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/stageDeadline")
    public ResponseEntity<Void> updateStageDeadline(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageDeadlineRequest request) {
        caseDataService.updateStageDeadline(caseUUID, stageUUID, request.getStageType(), request.getDays());
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/stageDeadlines")
    public ResponseEntity<Void> updateDeadlineForStages(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateDeadlineForStagesRequest request) {
        caseDataService.updateDeadlineForStages(caseUUID, stageUUID, request.getStageTypeAndDaysMap());
        return ResponseEntity.ok().build();
    }

    // TODO: Add test
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/primaryCorrespondent")
    public ResponseEntity<Void> updateCasePrimaryCorrespondent(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UUID primaryCorrespondentUUID) {
        caseDataService.updatePrimaryCorrespondent(caseUUID, stageUUID, primaryCorrespondentUUID);
        return ResponseEntity.ok().build();
    }

    @Allocated(allocatedTo = AllocationLevel.USER_OR_TEAM)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/updatePrimaryCorrespondent")
    public ResponseEntity<Void> updatePrimaryCorrespondent(
            @PathVariable UUID caseUUID,
            @PathVariable UUID stageUUID,
            @RequestBody UpdatePrimaryCorrespondentRequest primaryCorrespondentUUID) {
        UUID correspondentUUID = primaryCorrespondentUUID.getPrimaryCorrespondentUUID();
        caseDataService.updatePrimaryCorrespondent(caseUUID, stageUUID, correspondentUUID);
        return ResponseEntity.ok().build();
    }

    // TODO: Add test
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/primaryTopic")
    public ResponseEntity<Void> updateCasePrimaryTopic(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UUID primaryTopicUUID) {
        caseDataService.updatePrimaryTopic(caseUUID, stageUUID, primaryTopicUUID);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/teamTexts")
    public ResponseEntity<UpdateTeamByStageAndTextsResponse> updateTeamByStageAndTexts(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateTeamByStageAndTextsRequest request) {
        Map<String, String> teamMap = caseDataService.updateTeamByStageAndTexts(caseUUID, stageUUID, request.getStageType(), request.getTeamUUIDKey(), request.getTeamNameKey(), request.getTexts());
        return ResponseEntity.ok(UpdateTeamByStageAndTextsResponse.from(teamMap));
    }

    // TODO: Add test
    @PutMapping(value = "/case/{caseUUID}/complete")
    public ResponseEntity<Void> updateCompleteCase(@PathVariable UUID caseUUID, @RequestBody boolean complete) {
        caseDataService.completeCase(caseUUID, complete);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/documentTags")
    public ResponseEntity<List<String>> getDocumentTags(@PathVariable UUID caseUUID) {
        List<String> documentTags = caseDataService.getDocumentTags(caseUUID);
        return ResponseEntity.ok(documentTags);
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/standardLine")
    public ResponseEntity<Set<GetStandardLineResponse>> getStandardLine(@PathVariable UUID caseUUID) {
        Set<GetStandardLineResponse> standardLine = caseDataService.getStandardLine(caseUUID);
        return ResponseEntity.ok(standardLine);
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/templates")
    public ResponseEntity<List<TemplateDto>> getTemplate(@PathVariable UUID caseUUID) {
        List<TemplateDto> template = caseDataService.getTemplates(caseUUID);
        return ResponseEntity.ok(template);
    }

    @PostMapping(value = "/caseType/{caseType}/clearCachedTemplate")
    public ResponseEntity<String> clearCachedTemplateForCaseType(@PathVariable String caseType) {
        caseDataService.clearCachedTemplateForCaseType(caseType);
        return ResponseEntity.ok("Cache Cleared");
    }

    @GetMapping(value = "/case/{caseUUID}/data/{variableName}")
    public ResponseEntity<String> getCaseDataValue(@PathVariable UUID caseUUID, @PathVariable String variableName) {
        return ResponseEntity.ok(caseDataService.getCaseDataField(caseUUID, variableName));
    }

    @PutMapping(value = "/case/{caseUUID}/data/{variableName}")
    public ResponseEntity<Void> updateCaseDataValue(@PathVariable UUID caseUUID, @PathVariable String variableName, @RequestBody String value) {
        caseDataService.updateCaseData(caseUUID, null, Map.of(variableName, value));
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/data/{reference}")
    public ResponseEntity<GetCaseResponse> getCaseDataByReference(@PathVariable String reference) throws UnsupportedEncodingException {
        String decodedRef = URLDecoder.decode(reference, StandardCharsets.UTF_8.name());
        CaseData caseData = caseDataService.getCaseDataByReference(decodedRef);
        return ResponseEntity.ok(GetCaseResponse.from(caseData, true));
    }

    @Cacheable (value = "UUIDToCaseReference")
    @GetMapping(value = "/case/reference/{caseUUID}")
    public ResponseEntity<GetCaseReferenceResponse> getCaseReference(@PathVariable UUID caseUUID) {
        final String caseRef = caseDataService.getCaseDataCaseRef(caseUUID);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseRef));
    }
}
