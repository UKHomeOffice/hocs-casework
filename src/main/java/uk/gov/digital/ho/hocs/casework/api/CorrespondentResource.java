package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentsResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class CorrespondentResource {

    private final CorrespondentService correspondentService;

    @Autowired
    public CorrespondentResource(CorrespondentService correspondentService) {
        this.correspondentService = correspondentService;
    }

    @PostMapping(value = "/case/{caseUUID}/correspondent", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addCorrespondentToCase(@PathVariable UUID caseUUID, @RequestBody CreateCorrespondentRequest request) {
        Address addr = new Address(request.getPostcode(), request.getAddress1(), request.getAddress2(), request.getAddress3(), request.getCountry());
        correspondentService.createCorrespondent(caseUUID, request.getType(), request.getFullname(), addr, request.getTelephone(), request.getEmail(), request.getReference());
        return ResponseEntity.ok().build();
    }

    @Authorised
    @GetMapping(value = "/case/{caseUUID}/correspondent", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCorrespondentsResponse> getCorrespondents(@PathVariable UUID caseUUID) {
        Set<Correspondent> correspondents = correspondentService.getCorrespondents(caseUUID);
        return ResponseEntity.ok(GetCorrespondentsResponse.from(correspondents));
    }

    @Authorised
    @GetMapping(value = "/case/{caseUUID}/correspondent/{correspondentUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CorrespondentDto> getCorrespondent(@PathVariable UUID caseUUID, @PathVariable UUID correspondentUUID) {
        Correspondent correspondent = correspondentService.getCorrespondent(caseUUID, correspondentUUID);
        return ResponseEntity.ok(CorrespondentDto.from(correspondent));
    }

    @Authorised
    @GetMapping(value = "/case/{caseUUID}/correspondent/primary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CorrespondentDto> getPrimaryCorrespondent(@PathVariable UUID caseUUID) {
        Correspondent correspondent = correspondentService.getPrimaryCorrespondent(caseUUID);
        return ResponseEntity.ok(CorrespondentDto.from(correspondent));
    }

    @Authorised
    @DeleteMapping(value = "/case/{caseUUID}/correspondent/{correspondentUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CorrespondentDto> deleteCorrespondent(@PathVariable UUID caseUUID, @PathVariable UUID correspondentUUID) {
        correspondentService.deleteCorrespondent(caseUUID, correspondentUUID);
        return ResponseEntity.ok().build();
    }
}