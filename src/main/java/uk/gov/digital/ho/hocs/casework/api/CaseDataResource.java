package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseTypeResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.MigrateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.MigrateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.TemplateDto;
import uk.gov.digital.ho.hocs.casework.api.dto.TimelineItemDto;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCaseDataRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateDeadlineForStagesRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdatePrimaryCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateStageDeadlineRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateTeamByStageAndTextsRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateTeamByStageAndTextsResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
class CaseDataResource {

    private final CaseDataService caseDataService;

    private final InfoClient infoClient;

    @Autowired
    public CaseDataResource(@Qualifier("CaseDataService") CaseDataService caseDataService, InfoClient infoClient) {
        this.caseDataService = caseDataService;
        this.infoClient = infoClient;
    }

    @Authorised(accessLevel = AccessLevel.OWNER, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @PostMapping(value = "/case")
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CaseData caseData = caseDataService.createCase(request.getType(), request.getData(), request.getDateRecieved(),
            request.getFromCaseUUID());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @Authorised(accessLevel = AccessLevel.MIGRATE)
    @PostMapping(value = "/case/{caseUUID}/migrate")
    public ResponseEntity<MigrateCaseResponse> migrateCase(@PathVariable UUID caseUUID,
                                                           @RequestBody MigrateCaseRequest request) {
        MigrateCaseResponse migrateCaseResponse = caseDataService.migrateCase(request.getType(), caseUUID);
        return ResponseEntity.ok(migrateCaseResponse);
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @GetMapping(value = "/case/{caseUUID}")
    public ResponseEntity<GetCaseResponse> getCase(@PathVariable UUID caseUUID,
                                                   @RequestParam("full") Optional<Boolean> full) {
        CaseData caseData = caseDataService.getCase(caseUUID);
        return ResponseEntity.ok(GetCaseResponse.from(caseData, full.orElse(false)));
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @DeleteMapping(value = "/case/{caseUUID}/{deleted}")
    public ResponseEntity<Void> deleteCase(@PathVariable UUID caseUUID, @PathVariable Boolean deleted) {
        caseDataService.deleteCase(caseUUID, deleted);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @GetMapping(value = "/case/{caseUUID}/timeline")
    public ResponseEntity<Set<TimelineItemDto>> getCaseTimeline(@PathVariable UUID caseUUID) {
        Stream<TimelineItem> timeline = caseDataService.getCaseTimeline(caseUUID);
        return ResponseEntity.ok(timeline.map(TimelineItemDto::from).collect(Collectors.toSet()));
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @GetMapping(value = "/case/{caseUUID}/summary")
    public ResponseEntity<GetCaseSummaryResponse> getCaseSummary(@PathVariable UUID caseUUID) {
        CaseSummary caseSummary = caseDataService.getCaseSummary(caseUUID);
        return ResponseEntity.ok(GetCaseSummaryResponse.from(caseSummary));
    }

    @GetMapping(value = "/case/{caseUUID}/team/members")
    public ResponseEntity<List<UserDto>> getCaseTeams(@PathVariable UUID caseUUID) {
        Set<UUID> teamUUIDs = caseDataService.getCaseTeams(caseUUID);
        List<UserDto> users = teamUUIDs.stream().map(infoClient::getUsersForTeam).flatMap(List::stream).toList();

        return ResponseEntity.ok(users);
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/calculateTotals")
    public ResponseEntity<Map<String, String>> calculateTotals(@PathVariable UUID caseUUID,
                                                               @PathVariable UUID stageUUID,
                                                               @RequestBody String listName) {
        Map<String, String> totals = caseDataService.calculateTotals(caseUUID, stageUUID, listName);
        return ResponseEntity.ok(totals);
    }

    @Authorised(accessLevel = AccessLevel.WRITE, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/data")
    public ResponseEntity<Void> updateCaseData(@PathVariable UUID caseUUID,
                                               @PathVariable UUID stageUUID,
                                               @RequestBody UpdateCaseDataRequest request) {
        caseDataService.updateCaseData(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/dateReceived")
    public ResponseEntity<Void> updateCaseDateReceived(@PathVariable UUID caseUUID,
                                                       @PathVariable UUID stageUUID,
                                                       @RequestBody LocalDate dateReceived) {
        caseDataService.updateDateReceived_defaultSla(caseUUID, stageUUID, dateReceived);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/deadline")
    public ResponseEntity<Void> overrideSla(@PathVariable UUID caseUUID,
                                            @PathVariable UUID stageUUID,
                                            @RequestBody int days) {
        caseDataService.overrideSla(caseUUID, stageUUID, days);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/dispatchDeadlineDate")
    public ResponseEntity<Void> updateCaseDispatchDeadlineDate(@PathVariable UUID caseUUID,
                                                               @PathVariable UUID stageUUID,
                                                               @RequestBody LocalDate dispatchDate) {
        caseDataService.updateDispatchDeadlineDate(caseUUID, stageUUID, dispatchDate);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/stageDeadline")
    public ResponseEntity<Void> updateStageDeadline(@PathVariable UUID caseUUID,
                                                    @PathVariable UUID stageUUID,
                                                    @RequestBody UpdateStageDeadlineRequest request) {
        caseDataService.updateStageDeadline(caseUUID, stageUUID, request.getStageType(), request.getDays());
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/stageDeadlines")
    public ResponseEntity<Void> updateDeadlineForStages(@PathVariable UUID caseUUID,
                                                        @PathVariable UUID stageUUID,
                                                        @RequestBody UpdateDeadlineForStagesRequest request) {
        caseDataService.updateDeadlineForStages(caseUUID, stageUUID, request.getStageTypeAndDaysMap());
        return ResponseEntity.ok().build();
    }

    // TODO: Add test
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/primaryCorrespondent")
    public ResponseEntity<Void> updateCasePrimaryCorrespondent(@PathVariable UUID caseUUID,
                                                               @PathVariable UUID stageUUID,
                                                               @RequestBody UUID primaryCorrespondentUUID) {
        caseDataService.updatePrimaryCorrespondent(caseUUID, stageUUID, primaryCorrespondentUUID);
        return ResponseEntity.ok().build();
    }

    @Allocated(allocatedTo = AllocationLevel.USER_OR_TEAM)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/updatePrimaryCorrespondent")
    public ResponseEntity<Void> updatePrimaryCorrespondent(@PathVariable UUID caseUUID,
                                                           @PathVariable UUID stageUUID,
                                                           @RequestBody
                                                           UpdatePrimaryCorrespondentRequest primaryCorrespondentUUID) {
        UUID correspondentUUID = primaryCorrespondentUUID.getPrimaryCorrespondentUUID();
        caseDataService.updatePrimaryCorrespondent(caseUUID, stageUUID, correspondentUUID);
        return ResponseEntity.ok().build();
    }

    // TODO: Add test
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/primaryTopic")
    public ResponseEntity<Void> updateCasePrimaryTopic(@PathVariable UUID caseUUID,
                                                       @PathVariable UUID stageUUID,
                                                       @RequestBody UUID primaryTopicUUID) {
        caseDataService.updatePrimaryTopic(caseUUID, stageUUID, primaryTopicUUID);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.OWNER, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/teamTexts")
    public ResponseEntity<UpdateTeamByStageAndTextsResponse> updateTeamByStageAndTexts(@PathVariable UUID caseUUID,
                                                                                       @PathVariable UUID stageUUID,
                                                                                       @RequestBody
                                                                                       UpdateTeamByStageAndTextsRequest request) {
        Map<String, String> teamMap = caseDataService.updateTeamByStageAndTexts(caseUUID, stageUUID,
            request.getStageType(), request.getTeamUUIDKey(), request.getTeamNameKey(), request.getTexts());
        return ResponseEntity.ok(UpdateTeamByStageAndTextsResponse.from(teamMap));
    }

    // TODO: Add test
    @PutMapping(value = "/case/{caseUUID}/complete")
    public ResponseEntity<Void> updateCompleteCase(@PathVariable UUID caseUUID, @RequestBody boolean complete) {
        caseDataService.completeCase(caseUUID, complete);
        return ResponseEntity.ok().build();
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

    @GetMapping(value = "/case/{caseUUID}/data/{variableName}")
    public ResponseEntity<String> getCaseDataValue(@PathVariable UUID caseUUID, @PathVariable String variableName) {
        return ResponseEntity.ok(caseDataService.getCaseDataField(caseUUID, variableName));
    }

    @PutMapping(value = "/case/{caseUUID}/data/{variableName}")
    public ResponseEntity<Void> updateCaseDataValue(@PathVariable UUID caseUUID,
                                                    @PathVariable String variableName,
                                                    @RequestBody String value) {
        caseDataService.updateCaseData(caseUUID, null, Map.of(variableName, value));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/case/{caseUUID}/data/map")
    public ResponseEntity<Void> mapCaseDataValues(@PathVariable UUID caseUUID,
                                                  @RequestBody Map<String, String> keyMappings) {
        caseDataService.mapCaseDataValues(caseUUID, keyMappings);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/data/{reference}")
    public ResponseEntity<GetCaseResponse> getCaseDataByReference(
        @PathVariable String reference) throws UnsupportedEncodingException {
        String decodedRef = URLDecoder.decode(reference, StandardCharsets.UTF_8.name());
        CaseData caseData = caseDataService.getCaseDataByReference(decodedRef);
        return ResponseEntity.ok(GetCaseResponse.from(caseData, true));
    }

    @GetMapping(value = "/case/reference/{caseUUID}")
    public ResponseEntity<GetCaseReferenceResponse> getCaseReference(@PathVariable UUID caseUUID) {
        final String caseRef = caseDataService.getCaseRef(caseUUID);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseRef));
    }

    @GetMapping(value = "/case/{caseUUID}/type")
    public ResponseEntity<GetCaseTypeResponse> getCaseTypeForCase(@PathVariable UUID caseUUID) {
        String caseRef = caseDataService.getCaseType(caseUUID);
        return ResponseEntity.ok(GetCaseTypeResponse.from(caseRef));
    }

}
