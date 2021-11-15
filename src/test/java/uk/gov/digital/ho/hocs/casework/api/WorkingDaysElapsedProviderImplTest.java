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
    InfoClient infoClient;

    private static final String CASE_TYPE = "CASE_TYPE_A";


     @Test
    public void getWorkingDaysSince(){
        LocalDate fromDate = LocalDate.parse("2020-05-11");
        when(infoClient.getWorkingDaysElapsedForCaseType(CASE_TYPE, fromDate)).thenReturn(25);

        Integer results = infoClient.getWorkingDaysElapsedForCaseType(CASE_TYPE, fromDate);
        assertThat(results).isEqualTo(25);

        verify(infoClient).getWorkingDaysElapsedForCaseType(CASE_TYPE, fromDate);
        verifyNoMoreInteractions(infoClient);
    }
}
