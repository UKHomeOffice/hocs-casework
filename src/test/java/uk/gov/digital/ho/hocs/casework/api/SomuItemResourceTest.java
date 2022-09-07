package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateSomuItemRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetSomuItemResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SomuItemResourceTest {

    private final UUID uuid = UUID.randomUUID();

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID somuUUID = UUID.randomUUID();

    @Mock
    private SomuItemService somuItemService;

    private SomuItemResource somuItemResource;

    @Before
    public void setUp() {
        somuItemResource = new SomuItemResource(somuItemService);
    }

    @Test
    public void shouldUpsertSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUUID, somuUUID, "{}");
        CreateSomuItemRequest somuItemRequest = new CreateSomuItemRequest(null, "{}");

        when(somuItemService.upsertCaseSomuItemBySomuType(caseUUID, somuUUID, somuItemRequest)).thenReturn(somuItem);

        ResponseEntity<GetSomuItemResponse> response = somuItemResource.upsertCaseSomuItemBySomuType(caseUUID, somuUUID,
            somuItemRequest);

        verify(somuItemService).upsertCaseSomuItemBySomuType(caseUUID, somuUUID, somuItemRequest);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getSomuUuid()).isEqualTo(somuUUID);
        assertThat(response.getBody().getCaseUuid()).isEqualTo(caseUUID);
        assertThat(response.getBody().getData()).isEqualTo("{}");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteSomuItem() {
        ResponseEntity<Void> response = somuItemResource.deleteSomuItem(uuid);

        verify(somuItemService).deleteSomuItem(uuid);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetSomuItems() {
        SomuItem somuItem = new SomuItem(uuid, caseUUID, somuUUID, "{}");

        when(somuItemService.getItemsByCaseUuid(caseUUID)).thenReturn(Set.of(somuItem));

        ResponseEntity<Set<GetSomuItemResponse>> response = somuItemResource.getAllCaseSomuItems(caseUUID);

        verify(somuItemService).getItemsByCaseUuid(caseUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).size()).isEqualTo(1);
        assertThat(response.getBody().contains(GetSomuItemResponse.from(somuItem))).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetSomuItem() {
        SomuItem somuItem = new SomuItem(uuid, caseUUID, somuUUID, "{}");

        when(somuItemService.getItemsByCaseUuidAndType(caseUUID, somuUUID)).thenReturn(Set.of(somuItem));

        ResponseEntity<Set<GetSomuItemResponse>> response = somuItemResource.getCaseSomuItemsBySomuType(caseUUID,
            somuUUID);

        verify(somuItemService).getItemsByCaseUuidAndType(caseUUID, somuUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(somuItemService);
    }

}
