package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SomuItemTest {

    private final UUID SOMU_ITEM_UUID = UUID.randomUUID();
    private final UUID SOMU_ITEM_CASE_UUID = UUID.randomUUID();
    private final UUID SOMU_ITEM_TYPE_UUID = UUID.randomUUID();

    @Test
    public void getSomuItem() {
        SomuItem somuItem = new SomuItem(SOMU_ITEM_UUID, SOMU_ITEM_CASE_UUID, SOMU_ITEM_TYPE_UUID, "");

        assertThat(somuItem.getUuid()).isEqualTo(SOMU_ITEM_UUID);
        assertThat(somuItem.getCaseUuid()).isEqualTo(SOMU_ITEM_CASE_UUID);
        assertThat(somuItem.getSomuUuid()).isEqualTo(SOMU_ITEM_TYPE_UUID);
        assertThat(somuItem.getData()).isEqualTo("");
        assertThat(somuItem.isDeleted()).isFalse();
    }
    
    @Test
    public void getSomuItem_NullDataIsDeleted() {
        SomuItem somuItem = new SomuItem(SOMU_ITEM_UUID, SOMU_ITEM_CASE_UUID, SOMU_ITEM_TYPE_UUID, null);

        assertThat(somuItem.getUuid()).isEqualTo(SOMU_ITEM_UUID);
        assertThat(somuItem.getCaseUuid()).isEqualTo(SOMU_ITEM_CASE_UUID);
        assertThat(somuItem.getSomuUuid()).isEqualTo(SOMU_ITEM_TYPE_UUID);
        assertThat(somuItem.getData()).isEqualTo(null);
        assertThat(somuItem.isDeleted()).isTrue();
    }

}
