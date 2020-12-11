package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.repository.SomuItemRepository;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class SomuItemService {

    private final SomuItemRepository somuItemRepository;
    private final AuditClient auditClient;

    private static final String NOT_FOUND_MESSAGE = "Somu for UUID: %s not found!";

    @Autowired
    public SomuItemService(SomuItemRepository somuItemRepository, AuditClient auditClient) {
        this.somuItemRepository = somuItemRepository;
        this.auditClient = auditClient;
    }

    Set<SomuItem> getSomuItems(UUID caseUUID) {
        log.debug("Getting all Somu Items for Case: {}", caseUUID);
        Set<SomuItem> somuItems = somuItemRepository.findAllByCaseUuid(caseUUID);
        log.info("Got {} Somu Item for Case: {}, Event {}", somuItems.size(), caseUUID, value(EVENT, SOMU_ITEM_RETRIEVED));
        auditClient.viewSomuItemsAudit(caseUUID);
        return somuItems;
    }

    public SomuItem getSomuItem(UUID caseUuid, UUID somuTypeUuid) {
        SomuItem somuItem = somuItemRepository.findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        if (somuItem != null) {
            log.info("Got SomuItem for UUID: {}, Event {}", somuTypeUuid, value(EVENT, SOMU_ITEM_RETRIEVED));
            auditClient.viewSomuItemAudit(somuItem);
            return somuItem;
        }
        else {
            log.info("Could not find somu item for case {} for somu type {}, creating one. Event {}", caseUuid, somuTypeUuid, value(EVENT, SOMU_ITEM_RETRIEVED));
            return addSomuItem(caseUuid, somuTypeUuid, null);
        }
    }

    public SomuItem upsertSomuItem(UUID caseUuid, UUID somuTypeUuid, String data) {
        log.debug("Upserting Somu Item of Type: {} for Case: {}", somuTypeUuid, caseUuid);
        
        SomuItem somuItem = somuItemRepository.findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        
        if (somuItem != null) {
            somuItem.setData(data);
            somuItemRepository.save(somuItem);
            log.info("Updated Somu Item: {} for Case: {}, Event {}", somuItem.getUuid(), caseUuid, value(EVENT, SOMU_ITEM_UPDATED));

            auditClient.updateSomuItemAudit(somuItem);
        } else {
            somuItem = addSomuItem(caseUuid,somuTypeUuid, data);
        }
        
        return somuItem;
    }
    
    private SomuItem addSomuItem(UUID caseUUID, UUID somuTypeuuid, String data) {
        SomuItem somuItem = new SomuItem(UUID.randomUUID(), caseUUID, somuTypeuuid, data);
        somuItemRepository.save(somuItem);
        log.info("Created Somu Item: {} for Case: {}, Event {}", somuItem.getUuid(), caseUUID, value(EVENT, SOMU_ITEM_CREATED));

        auditClient.createSomuItemAudit(somuItem);
        return somuItem;
    }

    public void deleteSomuItem(UUID somuItemUuid) {
        log.debug("Deleting Somu Item: {}", somuItemUuid);
        SomuItem somuItem = somuItemRepository.findByUuid(somuItemUuid);
        if (somuItem != null){
            somuItem.setData(null);
            somuItemRepository.save(somuItem);
            log.info("Deleted Somu Item: {} for Case: {}, Event {}", somuItem.getUuid(), somuItem.getCaseUuid(), value(EVENT, SOMU_ITEM_DELETED));
            auditClient.deleteSomuItemAudit(somuItem);
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format(NOT_FOUND_MESSAGE, somuItemUuid), SOMU_ITEM_NOT_FOUND);
        }
    }

}
