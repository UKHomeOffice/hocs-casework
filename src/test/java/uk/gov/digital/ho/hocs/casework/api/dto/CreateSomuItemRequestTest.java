package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateSomuItemRequestTest {
    
    @Test
    public void createCreatSomuItemRequest() {
        String data = "DATA";
        CreateSomuItemRequest CreateSomuItemRequest = new CreateSomuItemRequest(data);
        assertThat(CreateSomuItemRequest.getData()).isEqualTo(data);
    }
    
}
