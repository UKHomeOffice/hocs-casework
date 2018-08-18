package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateInputDataRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InputDataResourceTest {

    @Mock
    private InputDataService inputDataService;

    private InputDataResource inputDataResource;

    @Before
    public void setUp() {
        inputDataResource = new InputDataResource(inputDataService);
    }

    @Test
    public void shouldCreateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        UpdateInputDataRequest request = new UpdateInputDataRequest(data);

        doNothing().when(inputDataService).updateInputData(uuid, data);

        ResponseEntity<Void> response = inputDataResource.updateInputData(uuid, request);

        verify(inputDataService, times(1)).updateInputData(uuid, data);

        verifyNoMoreInteractions(inputDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}