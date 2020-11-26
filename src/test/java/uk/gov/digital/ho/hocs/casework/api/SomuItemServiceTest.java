package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.repository.SomuItemRepository;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SomuItemServiceTest {

    private final UUID uuid = UUID.randomUUID();
    private final UUID caseUUID = UUID.randomUUID();
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
        SomuItem somuItem = new SomuItem(uuid, caseUUID, somuTypeUuid, "{}");
        
        when(somuItemRepository.findAllByCaseUuid(any())).thenReturn(Set.of(somuItem));
        
        somuItemService.getSomuItems(caseUUID);

        verify(somuItemRepository, times(1)).findAllByCaseUuid(caseUUID);
        verify(auditClient, times(1)).viewSomuItemsAudit(caseUUID);
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }

    @Test
    public void shouldGetSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUUID, somuTypeUuid, "{}");

        when(somuItemRepository.findByCaseUuidAndSomuUuid(any(), any())).thenReturn(somuItem);

        somuItemService.getSomuItem(caseUUID, somuTypeUuid);

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUUID, somuTypeUuid);
        verify(auditClient, times(1)).viewSomuItemAudit(somuItem);
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldGetSomuItem_ThrowsWhenNotFound() throws ApplicationExceptions.EntityNotFoundException {
        somuItemService.getSomuItem(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void shouldCreateSomuItem() {
        when(somuItemRepository.findByCaseUuidAndSomuUuid(caseUUID, somuTypeUuid)).thenReturn(null);

        SomuItem somuItem = somuItemService.upsertSomuItem(caseUUID, somuTypeUuid, "{}");

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUUID, somuTypeUuid);
        verify(somuItemRepository, times(1)).save(somuItem);
        verify(auditClient, times(1)).createSomuItemAudit(somuItem);

        assertThat(somuItem.getCaseUuid()).isEqualTo(caseUUID);
        assertThat(somuItem.getSomuUuid()).isEqualTo(somuTypeUuid);
        assertThat(somuItem.getData()).isEqualTo("{}");
        
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }

    @Test
    public void shouldUpdateSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUUID, somuTypeUuid, "{}");

        when(somuItemRepository.findByCaseUuidAndSomuUuid(caseUUID, somuTypeUuid)).thenReturn(somuItem);

        SomuItem somuItem1 = somuItemService.upsertSomuItem(caseUUID, somuTypeUuid, "{\"Test\": 1}");

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUUID, somuTypeUuid);
        verify(somuItemRepository, times(1)).save(somuItem);
        verify(auditClient, times(1)).updateSomuItemAudit(somuItem);

        assertThat(somuItem1.getCaseUuid()).isEqualTo(caseUUID);
        assertThat(somuItem1.getSomuUuid()).isEqualTo(somuTypeUuid);
        assertThat(somuItem1.getData()).isEqualTo("{\"Test\": 1}");

        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }

    @Test
    public void shouldDeleteSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUUID, somuTypeUuid, "{}");

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
