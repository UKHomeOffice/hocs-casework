package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateSomuItemRequest;
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

    public Set<SomuItem> getCaseSomuItemsBySomuType(UUID caseUUID, boolean audit) {
        log.debug("Getting all Somu Items for Case: {}", caseUUID);
        Set<SomuItem> somuItems = somuItemRepository.findAllByCaseUuid(caseUUID);
        log.info("Got {} Somu Item for Case: {}, Event {}", somuItems.size(), caseUUID, value(EVENT, SOMU_ITEM_RETRIEVED));
        if(audit){
            auditClient.viewAllSomuItemsAudit(caseUUID);
        }
        return somuItems;
    }

    public Set<SomuItem> getCaseSomuItemsBySomuType(UUID caseUuid, UUID somuTypeUuid) {
        Set<SomuItem> somuItems = somuItemRepository.findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        
        log.info("Got SomuItems for UUID: {}, Event {}", somuTypeUuid, value(EVENT, SOMU_ITEM_RETRIEVED));
        auditClient.viewCaseSomuItemsBySomuTypeAudit(caseUuid, somuTypeUuid);
        return somuItems;
    }

    public SomuItem upsertCaseSomuItemBySomuType(UUID caseUuid, UUID somuTypeUuid, CreateSomuItemRequest data) {
        log.debug("Upserting Somu Item of Type: {} for Case: {}", somuTypeUuid, caseUuid);
        
        SomuItem somuItem;
        
        if (data.getUuid() != null) {
            somuItem = somuItemRepository.findByUuid(data.getUuid());

            if (somuItem != null) {
                somuItem.setData(data.getData());
                somuItemRepository.save(somuItem);
                log.info("Updated Somu Item: {} for Case: {}, Event {}", somuItem.getUuid(), caseUuid, value(EVENT, SOMU_ITEM_UPDATED));

                auditClient.updateSomuItemAudit(somuItem);
            } else {
                somuItem = addCaseSomuItemBySomuTypeUuid(caseUuid, somuTypeUuid, data.getData());
            }
        } else {
            somuItem = addCaseSomuItemBySomuTypeUuid(caseUuid, somuTypeUuid, data.getData());
        }
        
        return somuItem;
    }
    
    private SomuItem addCaseSomuItemBySomuTypeUuid(UUID caseUUID, UUID somuTypeUuid, String data) {
        SomuItem somuItem = new SomuItem(UUID.randomUUID(), caseUUID, somuTypeUuid, data);
        somuItemRepository.save(somuItem);
        log.info("Created Somu Item: {} for Case: {}, Event {}", somuItem.getUuid(), caseUUID, value(EVENT, SOMU_ITEM_CREATED));

        auditClient.createCaseSomuItemAudit(somuItem);
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
