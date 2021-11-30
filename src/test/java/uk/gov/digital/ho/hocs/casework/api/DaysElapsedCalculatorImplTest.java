package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DaysElapsedCalculatorImplTest {

    @Mock
    WorkingDaysElapsedProvider workingDaysElapsedProvider;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    StageWithCaseData stage;

    private DaysElapsedCalculatorImpl daysElapsedCalculator;

    private static final String SYSTEM_DAYS_ELAPSED_FIELD_NAME = "systemDaysElapsed";
    private static final String DATE_RECEIVED_FIELD_NAME = "DateReceived";

    @Before
    public void before() {
        daysElapsedCalculator = new DaysElapsedCalculatorImpl(workingDaysElapsedProvider, objectMapper);
    }

    @Test
    public void updateDaysElapsed_nullDateReceived() {

        daysElapsedCalculator.updateDaysElapsed(stage);

        verify(stage).getDataMap(objectMapper);
        verify(stage).update(Map.of(SYSTEM_DAYS_ELAPSED_FIELD_NAME, "0"), objectMapper);
        checkNoMoreInteractions();

    }

    @Test
    public void updateDaysElapsed_blankDateReceived() {

        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(DATE_RECEIVED_FIELD_NAME, ""));

        daysElapsedCalculator.updateDaysElapsed(stage);

        verify(stage).getDataMap(objectMapper);
        verify(stage).update(Map.of(SYSTEM_DAYS_ELAPSED_FIELD_NAME, "0"), objectMapper);
        checkNoMoreInteractions();

    }

    @Test
    public void updateDaysElapsed_validDateReceived() {

        String dummyCaseType = "TypeA";
        when(stage.getDataMap(objectMapper)).thenReturn(Map.of(DATE_RECEIVED_FIELD_NAME, "2020-06-14"));
        when(stage.getCaseDataType()).thenReturn(dummyCaseType);

        LocalDate localDate = LocalDate.of(2020, 6, 14);
        when(workingDaysElapsedProvider.getWorkingDaysSince(dummyCaseType, localDate)).thenReturn(35);
        daysElapsedCalculator.updateDaysElapsed(stage);

        verify(stage).getCaseDataType();
        verify(stage).getDataMap(objectMapper);
        verify(stage).update(Map.of(SYSTEM_DAYS_ELAPSED_FIELD_NAME, "35"), objectMapper);
        verify(workingDaysElapsedProvider).getWorkingDaysSince(dummyCaseType, localDate);
        checkNoMoreInteractions();

    }

    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(workingDaysElapsedProvider, objectMapper, stage);
    }
}
