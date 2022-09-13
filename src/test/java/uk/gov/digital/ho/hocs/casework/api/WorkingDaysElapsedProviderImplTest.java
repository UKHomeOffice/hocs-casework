package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkingDaysElapsedProviderImplTest {

    @Mock
    DeadlineService deadlineService;

    private WorkingDaysElapsedProviderImpl workingDaysElapsedProvider;

    private static final String CASE_TYPE = "CASE_TYPE_A";

    @Before
    public void before() {
        workingDaysElapsedProvider = new WorkingDaysElapsedProviderImpl(deadlineService);
    }

    @Test
    public void getWorkingDaysSince() {
        LocalDate fromDate = LocalDate.parse("2020-05-11");
        LocalDate today = LocalDate.now();
        when(deadlineService.calculateWorkingDaysElapsedForCaseType(CASE_TYPE, fromDate, today)).thenReturn(25);

        Integer results = workingDaysElapsedProvider.getWorkingDaysSince(CASE_TYPE, fromDate);
        assertThat(results).isEqualTo(25);

        verify(deadlineService).calculateWorkingDaysElapsedForCaseType(CASE_TYPE, fromDate, today);
        verifyNoMoreInteractions(deadlineService);
    }

}
