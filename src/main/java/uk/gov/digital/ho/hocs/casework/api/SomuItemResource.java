package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class SomuItemResource {

    private final SomuItemService somuItemService;

    @Autowired
    public SomuItemResource(SomuItemService somuItemService) {
        this.somuItemService = somuItemService;
    }

    @Authorised (accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUuid}/item", produces = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<Set<GetSomuItemResponse>> getAllCaseSomuItems(@PathVariable UUID caseUuid) {
        Set<SomuItem> somuItems = somuItemService.getCaseSomuItemsBySomuType(caseUuid);
        return ResponseEntity.ok(somuItems.stream().map(GetSomuItemResponse::from).collect(Collectors.toSet()));
    }

    @Authorised (accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUuid}/item/{somuTypeUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<Set<GetSomuItemResponse>> getCaseSomuItemsBySomuType(@PathVariable UUID caseUuid, @PathVariable UUID somuTypeUuid) {
        Set<SomuItem> somuItems = somuItemService.getCaseSomuItemsBySomuType(caseUuid, somuTypeUuid);
        return ResponseEntity.ok(somuItems.stream().map(GetSomuItemResponse::from).collect(Collectors.toSet()));
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @DeleteMapping(value = "/item/{somuUuid}")
    ResponseEntity deleteSomuItem(@PathVariable UUID somuUuid) {
        somuItemService.deleteSomuItem(somuUuid);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case/{caseUuid}/item/{somuTypeUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<GetSomuItemResponse> upsertCaseSomuItemBySomuType(@PathVariable UUID caseUuid, @PathVariable UUID somuTypeUuid, @RequestBody CreateSomuItemRequest data) {
        SomuItem somuItem = somuItemService.upsertCaseSomuItemBySomuType(caseUuid, somuTypeUuid, data);
        return ResponseEntity.ok(GetSomuItemResponse.from(somuItem));
    }
}
