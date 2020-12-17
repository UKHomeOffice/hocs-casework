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
        
        somuItemService.getSomuItems(caseUuid);

        verify(somuItemRepository, times(1)).findAllByCaseUuid(caseUuid);
        verify(auditClient, times(1)).viewSomuItemsAudit(caseUuid);
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }

    @Test
    public void shouldGetSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUuid, somuTypeUuid, "{}");

        when(somuItemRepository.findByCaseUuidAndSomuUuid(any(), any())).thenReturn(somuItem);

        somuItemService.getSomuItem(caseUuid, somuTypeUuid);

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        verify(auditClient, times(1)).viewSomuItemAudit(somuItem);
        verifyNoMoreInteractions(somuItemRepository, auditClient);
    }

    @Test()
    public void shouldGetSomuItem_CreatesNewItemWhenNotFound() throws ApplicationExceptions.EntityNotFoundException {
        when(somuItemRepository.findByCaseUuidAndSomuUuid(any(), any())).thenReturn(null);

        SomuItem somuItem = somuItemService.getSomuItem(caseUuid, somuTypeUuid);

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        verify(somuItemRepository, times(1)).save(any());
        verify(auditClient, times(1)).createSomuItemAudit(somuItem);
        verifyNoMoreInteractions(somuItemRepository, auditClient);

        assertThat(somuItem.getCaseUuid()).isEqualTo(caseUuid);
        assertThat(somuItem.getSomuUuid()).isEqualTo(somuTypeUuid);
        assertThat(somuItem.getData()).isEqualTo(null);
        assertThat(somuItem.isDeleted()).isTrue();
    }

    @Test
    public void shouldCreateSomuItem() {
        when(somuItemRepository.findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid)).thenReturn(null);

        SomuItem somuItem = somuItemService.upsertSomuItem(caseUuid, somuTypeUuid, "{}");

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        verify(somuItemRepository, times(1)).save(somuItem);
        verify(auditClient, times(1)).createSomuItemAudit(somuItem);
        verifyNoMoreInteractions(somuItemRepository, auditClient);

        assertThat(somuItem.getCaseUuid()).isEqualTo(caseUuid);
        assertThat(somuItem.getSomuUuid()).isEqualTo(somuTypeUuid);
        assertThat(somuItem.getData()).isEqualTo("{}");
    }

    @Test
    public void shouldUpdateSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUuid, somuTypeUuid, "{}");

        when(somuItemRepository.findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid)).thenReturn(somuItem);

        SomuItem somuItem1 = somuItemService.upsertSomuItem(caseUuid, somuTypeUuid, "{\"Test\": 1}");

        verify(somuItemRepository, times(1)).findByCaseUuidAndSomuUuid(caseUuid, somuTypeUuid);
        verify(somuItemRepository, times(1)).save(somuItem);
        verify(auditClient, times(1)).updateSomuItemAudit(somuItem);
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
