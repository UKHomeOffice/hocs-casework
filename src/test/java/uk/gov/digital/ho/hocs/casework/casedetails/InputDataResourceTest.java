package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InputDataResourceTest {

    @Mock
    private InputDataService inputDataService;

    private InputDataResource inputDataResource;

    @Before
    public void setUp()  { inputDataResource = new InputDataResource(inputDataService);
    }

    @Test
    public void shouldReturnTopicUUID(){
        UUID uuid = UUID.randomUUID();
        String data = "{\"Topics\":\"11111111-1111-1111-1111-111111111137\"}";
        InputData inputData = new InputData(1,data,uuid, LocalDateTime.now(),null);
        when(inputDataService.getInputData(uuid)).thenReturn(inputData);

        inputDataResource.getPrimaryTopicForCase(uuid);
        verify(inputDataService, times(1)).getInputData(uuid);
        verifyNoMoreInteractions(inputDataService);
    }
}