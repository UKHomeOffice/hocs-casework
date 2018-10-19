package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InputDataServiceTest {

    @Mock
    private InputDataRepository inputDataRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ObjectMapper objectMapper;

    private InputDataService inputDataService;

    @Before
    public void setUp() {
        this.inputDataService = new InputDataService(
                inputDataRepository,
                auditService,
                objectMapper);
    }

    @Test
    public void shouldUpdateInputDataWithValidParams() {

        UUID uuid = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();
        InputData inputData = new InputData();

        when(inputDataRepository.findByCaseUUID(uuid)).thenReturn(inputData);

        inputDataService.setInputData(uuid, data);

        verify(inputDataRepository, times(1)).findByCaseUUID(uuid);
        verify(inputDataRepository, times(1)).save(inputData);
        verify(auditService, times(1)).updateInputDataEvent(inputData);

        verifyNoMoreInteractions(inputDataRepository);
        verifyNoMoreInteractions(auditService);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingUUIDException() {

        Map<String, String> data = new HashMap<>();

        inputDataService.setInputData(null, data);
    }

    @Test()
    public void shouldNotCreateStageMissingUUID() {

        Map<String, String> data = new HashMap<>();

        try {
            inputDataService.setInputData(null, data);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(inputDataRepository, times(1)).findByCaseUUID(null);

        verifyNoMoreInteractions(inputDataRepository);
        verifyZeroInteractions(auditService);
    }

    //@Test(expected = EntityNotFoundException.class)
    //public void shouldNotCreateStageMissingDataException() {

    //    UUID uuid = UUID.randomUUID();

    //     inputDataService.setInputData(uuid, null);
    // }

    //@Test()
    //public void shouldNotCreateStageMissingData() {

    //    UUID uuid = UUID.randomUUID();

    //    try {
    //       inputDataService.setInputData(uuid, null);
    //   } catch (EntityNotFoundException e) {
            // Do nothing.
    //   }

    //   verify(inputDataRepository, times(1)).findByCaseUUID(uuid);

    //    verifyNoMoreInteractions(inputDataRepository);
    //   verifyZeroInteractions(auditService);
    //}

}
