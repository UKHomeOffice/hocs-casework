package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DaysElapsedCalculatorImplTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Stage stage;

    private DaysElapsedCalculator daysElapsedCalculator;

    private static final String SYSTEM_DAYS_ELAPSED_FIELD_NAME = "systemDaysElapsed";
    private static final String DATE_RECEIVED_FIELD_NAME = "DateReceived";

    @Before
    public void before() {
        daysElapsedCalculator = new DaysElapsedCalculator(objectMapper);
    }

    @Test
    public void updateDaysElapsed_validDateReceived() {

        String dummyCaseType = "TypeA";
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(DATE_RECEIVED_FIELD_NAME, "2020-06-14"));
        when(stage.getCaseDataType()).thenReturn(dummyCaseType);

        LocalDate localDate = LocalDate.of(2020, 6, 14);
        when(infoClient.getWorkingDaysElapsedForCaseType(dummyCaseType, localDate)).thenReturn(35);
        daysElapsedCalculator.updateDaysElapsed(stage);

        verify(stage).getCaseDataType();
        verify(stage).getDataMap(objectMapper);
        verify(stage).update(Map.of(SYSTEM_DAYS_ELAPSED_FIELD_NAME, "35"), objectMapper);
        verify(stage).getCaseUUID();
        verify(infoClient).getWorkingDaysElapsedForCaseType(dummyCaseType, localDate);
        checkNoMoreInteractions();

    }

    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(infoClient, objectMapper, stage);
    }
}
