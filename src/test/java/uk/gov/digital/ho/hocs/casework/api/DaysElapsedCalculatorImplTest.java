package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DaysElapsedCalculatorImplTest {

    private static final String SYSTEM_DAYS_ELAPSED_FIELD_NAME = "systemDaysElapsed";

    private static final String DATE_RECEIVED_FIELD_NAME = "DateReceived";

    @Mock
    WorkingDaysElapsedProvider workingDaysElapsedProvider;

    private DaysElapsedCalculatorImpl daysElapsedCalculator;

    @Before
    public void before() {
        daysElapsedCalculator = new DaysElapsedCalculatorImpl(workingDaysElapsedProvider);
    }

    @Test
    public void updateDaysElapsed_nullDateReceived() {
        var data = new HashMap<String, String>(0);

        daysElapsedCalculator.updateDaysElapsed(data, "AnyType");

        assertTrue(data.containsKey(SYSTEM_DAYS_ELAPSED_FIELD_NAME));
        assertEquals("0", data.get(SYSTEM_DAYS_ELAPSED_FIELD_NAME));

    }

    @Test
    public void updateDaysElapsed_blankDateReceived() {
        var data = new HashMap<String, String>(0);
        data.put(DATE_RECEIVED_FIELD_NAME, "");

        daysElapsedCalculator.updateDaysElapsed(data, "AnyType");

        assertTrue(data.containsKey(SYSTEM_DAYS_ELAPSED_FIELD_NAME));
        assertEquals("0", data.get(SYSTEM_DAYS_ELAPSED_FIELD_NAME));
    }

    @Test
    public void updateDaysElapsed_validDateReceived() {

        String dummyCaseType = "TypeA";
        var data = new HashMap<String, String>(0);
        data.put(DATE_RECEIVED_FIELD_NAME, "2020-06-14");

        LocalDate localDate = LocalDate.of(2020, 6, 14);
        when(workingDaysElapsedProvider.getWorkingDaysSince(dummyCaseType, localDate)).thenReturn(35);

        daysElapsedCalculator.updateDaysElapsed(data, dummyCaseType);

        assertTrue(data.containsKey(SYSTEM_DAYS_ELAPSED_FIELD_NAME));
        assertEquals("35", data.get(SYSTEM_DAYS_ELAPSED_FIELD_NAME));

        verify(workingDaysElapsedProvider).getWorkingDaysSince(dummyCaseType, localDate);
        checkNoMoreInteractions();

    }

    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(workingDaysElapsedProvider);
    }

}
