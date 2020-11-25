package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetSomuItemResponseTest {

    private final UUID SOMU_ITEM_UUID = UUID.randomUUID();
    private final UUID SOMU_ITEM_CASE_UUID = UUID.randomUUID();
    private final UUID SOMU_ITEM_TYPE_UUID = UUID.randomUUID();
    
    @Test
    public void getSomuItemResponseTest() {
        SomuItem somuItem = new SomuItem(SOMU_ITEM_UUID, SOMU_ITEM_CASE_UUID, SOMU_ITEM_TYPE_UUID, "");
        
        GetSomuItemResponse getSomuItemResponse = GetSomuItemResponse.from(somuItem);

        assertThat(getSomuItemResponse.getUuid()).isEqualTo(somuItem.getUuid());
        assertThat(getSomuItemResponse.getCaseUuid()).isEqualTo(somuItem.getCaseUuid());
        assertThat(getSomuItemResponse.getSomuUuid()).isEqualTo(somuItem.getSomuUuid());
        assertThat(getSomuItemResponse.getData()).isEqualTo(somuItem.getData());
        assertThat(getSomuItemResponse.isDeleted()).isEqualTo(somuItem.isDeleted());
    }

    @Test
    public void getSomuItemResponseTest_NullDataIsDeleted() {
        SomuItem somuItem = new SomuItem(SOMU_ITEM_UUID, SOMU_ITEM_CASE_UUID, SOMU_ITEM_TYPE_UUID, null);

        GetSomuItemResponse getSomuItemResponse = GetSomuItemResponse.from(somuItem);

        assertThat(getSomuItemResponse.getUuid()).isEqualTo(somuItem.getUuid());
        assertThat(getSomuItemResponse.getCaseUuid()).isEqualTo(somuItem.getCaseUuid());
        assertThat(getSomuItemResponse.getSomuUuid()).isEqualTo(SOMU_ITEM_TYPE_UUID);
        assertThat(getSomuItemResponse.getData()).isEqualTo(somuItem.getData());
        assertThat(getSomuItemResponse.isDeleted()).isEqualTo(somuItem.isDeleted());
    }
    
}
