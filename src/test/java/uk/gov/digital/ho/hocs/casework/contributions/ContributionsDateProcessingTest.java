package uk.gov.digital.ho.hocs.casework.contributions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContributionsDateProcessingTest {

    @Mock
    ContributionSomuInspector contributionSomuInspector;

    @Before
    public void setup(){
        when(contributionSomuInspector.getContributionDueLocalDate()).thenCallRealMethod();
    }

    @Test
    public void testGetContributionDateWithNiceFormatting(){
        when(contributionSomuInspector.getContributionDueDate()).thenReturn("2021-09-27");
        LocalDate localDate = LocalDate.of(2021, 9, 27);
        assertEquals(localDate, contributionSomuInspector.getContributionDueLocalDate());
    }

    @Test
    public void testGetContributionDateWithLeadingZeros(){
        when(contributionSomuInspector.getContributionDueDate()).thenReturn("02021-00009-0000027");
        LocalDate localDate = LocalDate.of(2021, 9, 27);
        assertEquals(localDate, contributionSomuInspector.getContributionDueLocalDate());
    }

    @Test
    public void testGetContributionDateNoZeros(){
        when(contributionSomuInspector.getContributionDueDate()).thenReturn("2021-9-7");
        LocalDate localDate = LocalDate.of(2021, 9, 7);
        assertEquals(localDate, contributionSomuInspector.getContributionDueLocalDate());
    }

}
