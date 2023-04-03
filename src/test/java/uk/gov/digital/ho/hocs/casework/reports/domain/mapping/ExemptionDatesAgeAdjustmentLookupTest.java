package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExemptionDatesAgeAdjustmentLookupTest {

    @Mock
    private InfoClient infoClient;

    private static final Clock clock = Clock.fixed(
        LocalDate.parse("2023-03-08").atStartOfDay().toInstant(ZoneOffset.UTC),
        ZoneOffset.UTC
    );

    public static final String COMP = "COMP";

    public static final String OTHER = "OTHER";

    public static final String UNCACHED = "UNCACHED";

    private static final Map<String, Set<LocalDate>> EXAMPLE_DATES = Map.of(
        COMP, Set.of(
            LocalDate.of(2022, 12, 26),
            LocalDate.of(2022, 12, 27),
            LocalDate.of(2023, 1, 2),
            LocalDate.of(2024, 1, 2)
        ),
        OTHER,
        Set.of(
            LocalDate.of(2022, 12, 26),
            LocalDate.of(2023, 1, 2)
        ),
        UNCACHED,
        Set.of(
            LocalDate.of(2023, 1, 2),
            LocalDate.of(2023, 1, 3)
        )
    );

    @Test
    public void whenQueryingExemptionDatesSinceADate_thenTheExpectedCountForTheCaseTypeIsReturned() {
        givenExampleExemptionDates();
        ExemptionDatesAgeAdjustmentLookup underTest = new ExemptionDatesAgeAdjustmentLookup(infoClient, clock);
        underTest.refreshCache();

        assertThat(underTest.getExemptionDatesForCaseTypeSince(COMP, LocalDate.of(2022, 12, 1))).isEqualTo(3);
        assertThat(underTest.getExemptionDatesForCaseTypeSince(OTHER, LocalDate.of(2022, 12, 1))).isEqualTo( 2);

        assertThat(underTest.getExemptionDatesForCaseTypeSince(COMP, LocalDate.of(2022, 12, 27))).isEqualTo( 2);

        assertThat(underTest.getExemptionDatesForCaseTypeSince(OTHER, LocalDate.of(2023, 2, 1))).isEqualTo( 0);
    }

    @Test
    public void whenQueryingExemptionDatesInARange_thenTheExpectedCountForTheCaseTypeIsReturned() {
        givenExampleExemptionDates();
        ExemptionDatesAgeAdjustmentLookup underTest = new ExemptionDatesAgeAdjustmentLookup(infoClient, clock);
        underTest.refreshCache();

        assertThat(underTest.getExemptionDatesForCaseTypeBetween(
            COMP, LocalDate.of(2022, 12, 1), LocalDate.of(2023, 1, 31))).isEqualTo(3);
        assertThat(underTest.getExemptionDatesForCaseTypeBetween(
            OTHER, LocalDate.of(2022, 12, 1), LocalDate.of(2022, 12, 31))).isEqualTo( 1);

        assertThat(underTest.getExemptionDatesForCaseTypeBetween(
            COMP, LocalDate.of(2022, 12, 27), LocalDate.of(2023, 1, 2))).isEqualTo( 2);

        assertThat(underTest.getExemptionDatesForCaseTypeBetween(
            OTHER, LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 9))).isEqualTo( 0);

        assertThat(underTest.getExemptionDatesForCaseTypeBetween(
            OTHER, LocalDate.of(2023, 1, 2), LocalDate.of(2023, 1, 2))).isEqualTo( 1);
    }

    @Test
    public void whenQueryingAnUncachedCaseType_theDatesAreLookedUpAndCached() {
        givenExampleExemptionDates();
        ExemptionDatesAgeAdjustmentLookup underTest = new ExemptionDatesAgeAdjustmentLookup(infoClient, clock);
        underTest.refreshCache();

        verify(infoClient, times(0)).getExemptionDatesForType(UNCACHED);

        assertThat(underTest.getExemptionDatesForCaseTypeSince(UNCACHED, LocalDate.of(2023, 1, 1))).isEqualTo(2);
        assertThat(underTest.getExemptionDatesForCaseTypeSince(UNCACHED, LocalDate.of(2023, 1, 3))).isEqualTo(1);

        verify(infoClient, times(1)).getExemptionDatesForType(UNCACHED);
    }

    private void givenExampleExemptionDates() {
        when(infoClient.getAllCaseTypes()).thenReturn(List.of(mockCaseType(COMP), mockCaseType(OTHER)));

        when(infoClient.getExemptionDatesForType(COMP)).thenReturn(EXAMPLE_DATES.get(COMP));
        when(infoClient.getExemptionDatesForType(OTHER)).thenReturn(EXAMPLE_DATES.get(OTHER));
        when(infoClient.getExemptionDatesForType(UNCACHED)).thenReturn(EXAMPLE_DATES.get(UNCACHED));
    }

    private CaseDataType mockCaseType(String type) {
        return new CaseDataType(null, null, type, null, 0, 0);
    }

}
