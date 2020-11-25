package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class SomuItemResource {

    private final SomuItemService somuItemService;

    @Autowired
    public SomuItemResource(SomuItemService somuItemService) {
        this.somuItemService = somuItemService;
    }
    
    @GetMapping(value = "/case/{caseUuid}/item")
    ResponseEntity<Set<GetSomuItemResponse>> getSomuItems(@PathVariable UUID caseUuid) {
        Set<SomuItem> somuItems = somuItemService.getSomuItems(caseUuid);
        return ResponseEntity.ok(somuItems.stream().map(GetSomuItemResponse::from).collect(Collectors.toSet()));
    }

    @GetMapping(value = "/case/{caseUuid}/item/{somuTypeUuid}")
    ResponseEntity<GetSomuItemResponse> getSomuItem(@PathVariable UUID caseUuid, @PathVariable UUID somuTypeUuid) {
        SomuItem somuItem = somuItemService.getSomuItem(caseUuid, somuTypeUuid);
        return ResponseEntity.ok(GetSomuItemResponse.from(somuItem));
    }

    @PutMapping(value = "/item/{somuUuid}")
    ResponseEntity<GetSomuItemResponse> updateSomuItem(@PathVariable UUID somuUuid, @RequestBody String data) {
        SomuItem somuItem = somuItemService.updateSomuItem(somuUuid, data);
        return ResponseEntity.ok(GetSomuItemResponse.from(somuItem));
    }

    @DeleteMapping(value = "/item/{somuUuid}")
    ResponseEntity deleteSomuItem(@PathVariable UUID somuUuid) {
        somuItemService.updateSomuItem(somuUuid, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/case/{caseUuid}/item/{somuTypeUuid}")
    ResponseEntity<GetSomuItemResponse> createSomuItem(@PathVariable UUID caseUuid, @PathVariable UUID somuTypeUuid, @RequestBody String data) {
        SomuItem somuItem = somuItemService.createSomuItem(caseUuid, somuTypeUuid, data);
        return ResponseEntity.ok(GetSomuItemResponse.from(somuItem));
    }
}
