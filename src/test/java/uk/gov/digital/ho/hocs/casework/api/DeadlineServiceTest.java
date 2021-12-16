package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BankHoliday;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeadlineServiceTest {
    private SpringConfiguration configuration;
    private DeadlineService deadlineService;

    @Mock
    BankHolidayService mockBankHolidayService;

    @Mock
    InfoClient mockInfoService;

    private final Set<BankHoliday.BankHolidayRegion> bankHolidayRegions = Set.of(BankHoliday.BankHolidayRegion.ENGLAND_AND_WALES);
    private final List<String> bankHolidayRegionsAsString = List.of("ENGLAND_AND_WALES");

    private final CaseDataType caseType = CaseDataTypeFactory.from("A1", "a1");

    private final Set<LocalDate> englandAndWalesBankHolidays = Set.of(
            LocalDate.parse("2020-01-01"),
            LocalDate.parse("2020-04-10"),
            LocalDate.parse("2020-04-13"),
            LocalDate.parse("2020-05-08"),
            LocalDate.parse("2020-05-25"),
            LocalDate.parse("2020-08-31"),
            LocalDate.parse("2020-12-25"),
            LocalDate.parse("2020-12-28")
    );

    @Before
    public void setUp() {
        configuration = new SpringConfiguration();

//        when(mockInfoService.getCaseType(caseType.getDisplayCode())).thenReturn(caseType);
        when(mockInfoService.getBankHolidayRegionsByCaseType(any())).thenReturn(bankHolidayRegionsAsString);
        when(mockBankHolidayService.getBankHolidayDatesForRegions(eq(bankHolidayRegions)))
                .thenReturn(englandAndWalesBankHolidays);

        deadlineService = new DeadlineService(mockInfoService, mockBankHolidayService);
    }

    @Test
    public void shouldCalculateWorkingDaysForCase() {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate expectedDeadline = LocalDate.parse("2020-03-02");
//
//        when(mockInfoService.getCaseType(caseType.getDisplayCode()))
//                .thenReturn(caseType);

        // when
        final LocalDate result =
                deadlineService.calculateWorkingDaysForCaseType("A1", originalReceivedDate, caseType.getSla());

        // then
        assertThat(result).isEqualTo(expectedDeadline);
    }
}
