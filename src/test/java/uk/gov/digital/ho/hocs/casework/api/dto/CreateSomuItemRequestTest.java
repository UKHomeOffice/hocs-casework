package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateSomuItemRequestTest {

    @Test
    public void createCreatSomuItemRequest() {
        UUID uuid = UUID.randomUUID();
        String data = "DATA";
        CreateSomuItemRequest CreateSomuItemRequest = new CreateSomuItemRequest(uuid, data);

        assertThat(CreateSomuItemRequest.getUuid()).isEqualTo(uuid);
        assertThat(CreateSomuItemRequest.getData()).isEqualTo(data);
    }

}
