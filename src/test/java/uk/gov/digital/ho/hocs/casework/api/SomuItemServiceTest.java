package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateSomuItemRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.repository.SomuItemRepository;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SomuItemServiceTest {

    private final UUID uuid = UUID.randomUUID();
    private final UUID caseUuid = UUID.randomUUID();
    private final UUID somuTypeUuid = UUID.randomUUID();
    
    @Mock
    private SomuItemRepository somuItemRepository;
   
    private SomuItemService somuItemService;
   
    @Mock
    private AuditClient auditClient;

    @Before
    public void setUp() {
        somuItemService = new SomuItemService(somuItemRepository, auditClient);
    }

    @Test
    public void shouldGetSomuItems() {
        SomuItem somuItem = new SomuItem(uuid, caseUuid, somuTypeUuid, "{}");
        
        when(somuItemRepository.findAllByCaseUuid(any())).thenReturn(Set.of(somuItem));
        
        somuItemService.getCaseSomuItemsBySomuType(caseUuid, true);

        verify(somuItemRepository, times(1)).findAllByCaseUuid(caseUuid);
        verify(auditClient, times(1)).viewAllSomuItemsAudit(caseUuid);
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }

    @Test
    public void shouldGetSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUuid, somuTypeUuid, "{}");

        when(somuItemRepository.findByCaseUuidAndSomuUuid(any(), any())).thenReturn(Set.of(somuItem));

        somuItemService.getCaseSomuItemsBySomuType(caseUuid, somuTypeUuid);

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        verify(auditClient, times(1)).viewCaseSomuItemsBySomuTypeAudit(caseUuid, somuTypeUuid);
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }
   
    @Test()
    public void shouldGetSomuItem_EmptySetReturnedWhenNoneExist() throws ApplicationExceptions.EntityNotFoundException {
        when(somuItemRepository.findByCaseUuidAndSomuUuid(any(), any())).thenReturn(Collections.emptySet());

        Set<SomuItem> somuItem = somuItemService.getCaseSomuItemsBySomuType(caseUuid, somuTypeUuid);

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        verify(auditClient, times(1)).viewCaseSomuItemsBySomuTypeAudit(caseUuid, somuTypeUuid);

        verifyNoMoreInteractions(somuItemRepository, auditClient);

        assertThat(somuItem.size()).isEqualTo(0);
    }

    @Test
    public void shouldCreateSomuItem() {
        SomuItem somuItem = somuItemService.upsertCaseSomuItemBySomuType(caseUuid, somuTypeUuid, new CreateSomuItemRequest(null, "{}"));

        verify(somuItemRepository, times(1)).save(somuItem);
        verify(auditClient, times(1)).createCaseSomuItemAudit(somuItem);
        verifyNoMoreInteractions(somuItemRepository, auditClient);

        assertThat(somuItem.getCaseUuid()).isEqualTo(caseUuid);
        assertThat(somuItem.getSomuUuid()).isEqualTo(somuTypeUuid);
        assertThat(somuItem.getData()).isEqualTo("{}");
    }

    @Test
    public void shouldUpdateSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUuid, somuTypeUuid, "{}");

        when(somuItemRepository.findByUuid(uuid)).thenReturn(somuItem);

        SomuItem somuItem1 = somuItemService.upsertCaseSomuItemBySomuType(caseUuid, somuTypeUuid, new CreateSomuItemRequest(uuid, "{\"Test\": 1}"));

        verify(somuItemRepository, times(1)).findByUuid(uuid);
        verify(somuItemRepository, times(1)).save(somuItem);
        verify(auditClient, times(1)).updateSomuItemAudit(somuItem);
        verifyNoMoreInteractions(somuItemRepository, auditClient);

        assertThat(somuItem1.getCaseUuid()).isEqualTo(caseUuid);
        assertThat(somuItem1.getSomuUuid()).isEqualTo(somuTypeUuid);
        assertThat(somuItem1.getData()).isEqualTo("{\"Test\": 1}");
    }

    @Test
    public void shouldCreateItem_IfItemCannotBeFound() {
        when(somuItemRepository.findByUuid(uuid)).thenReturn(null);

        SomuItem somuItem1 = somuItemService.upsertCaseSomuItemBySomuType(caseUuid, somuTypeUuid, new CreateSomuItemRequest(uuid, "{\"Test\": 1}"));

        verify(somuItemRepository, times(1)).findByUuid(uuid);
        verify(somuItemRepository, times(1)).save(somuItem1);
        verify(auditClient, times(1)).createCaseSomuItemAudit(somuItem1);
        verifyNoMoreInteractions(somuItemRepository, auditClient);

        assertThat(somuItem1.getCaseUuid()).isEqualTo(caseUuid);
        assertThat(somuItem1.getSomuUuid()).isEqualTo(somuTypeUuid);
        assertThat(somuItem1.getData()).isEqualTo("{\"Test\": 1}");
    }

    @Test
    public void shouldDeleteSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUuid, somuTypeUuid, "{}");

        when(somuItemRepository.findByUuid(uuid)).thenReturn(somuItem);
        
        somuItemService.deleteSomuItem(uuid);

        verify(somuItemRepository, times(1)).findByUuid(uuid);
        verify(somuItemRepository, times(1)).save(somuItem);
        verify(auditClient, times(1)).deleteSomuItemAudit(somuItem);
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }
    
    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldDeleteSomuItem_ThrowsWhenNotFound() {
        somuItemService.deleteSomuItem(uuid);
    }

}
