package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.BankHolidaysByRegionDto;
import uk.gov.digital.ho.hocs.casework.client.bankHolidayClient.BankHolidayClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BankHoliday;
import uk.gov.digital.ho.hocs.casework.domain.repository.BankHolidayRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BankHolidayServiceTest {

    private BankHolidayService bankHolidayService;

    @Mock
    private BankHolidayClient mockBankHolidayClient;

    @Mock
    private BankHolidayRepository mockBankHolidayRepository;

    @Captor
    ArgumentCaptor<List<BankHoliday>> savedBankHolidaysCaptor;

    Map<String, BankHolidaysByRegionDto> bankHolidayResponse =
            Map.of("northern-ireland", new BankHolidaysByRegionDto(List.of(
                    new BankHolidaysByRegionDto.Event("2017-01-02"),
                    new BankHolidaysByRegionDto.Event("2017-03-17"),
                    new BankHolidaysByRegionDto.Event("2017-04-14")
            )));

    @Before
    public void setUp() {
        this.bankHolidayService = new BankHolidayService(mockBankHolidayRepository, mockBankHolidayClient);
    }

    @Test
    public void shouldSaveBankHolidays() {
        // given
        when(mockBankHolidayClient.getBankHolidays()).thenReturn(bankHolidayResponse);

        // when
        bankHolidayService.refreshBankHolidayTable();

        // then
        verify(mockBankHolidayClient, times(1)).getBankHolidays();
        verify(mockBankHolidayRepository, times(1)).findAll();
        verify(mockBankHolidayRepository, times(1)).saveAll(savedBankHolidaysCaptor.capture());


        final List<BankHoliday> savedBankHolidays = savedBankHolidaysCaptor.getValue();
        assertThat(savedBankHolidays).isNotNull();
        assertThat(savedBankHolidays.size()).isEqualTo(3);

        assertThat((int) savedBankHolidays.stream()
                .filter(bankHoliday -> bankHoliday.equals(
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 1, 2))))
                .count()).isEqualTo(1);

        assertThat((int) savedBankHolidays.stream()
                .filter(bankHoliday -> bankHoliday.equals(
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 3, 17))))
                .count()).isEqualTo(1);


        assertThat((int) savedBankHolidays.stream()
                .filter(bankHoliday -> bankHoliday.equals(
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 4, 14))))
                .count()).isEqualTo(1);

        verifyNoMoreInteractions(mockBankHolidayClient, mockBankHolidayRepository);

    }

    @Test
    public void shouldSaveOnlyUniqueBankHolidays() {
        // given
        when(mockBankHolidayClient.getBankHolidays())
                .thenReturn(bankHolidayResponse);

        when(mockBankHolidayRepository.findAll()).thenReturn(Set.of(
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 1, 2)),
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 3, 17))
                )
        );

        // when
        bankHolidayService.refreshBankHolidayTable();

        // then
        verify(mockBankHolidayClient, times(1)).getBankHolidays();
        verify(mockBankHolidayRepository, times(1)).findAll();
        verify(mockBankHolidayRepository, times(1)).saveAll(savedBankHolidaysCaptor.capture());

        final List<BankHoliday> savedBankHolidays = savedBankHolidaysCaptor.getValue();

        assertThat(savedBankHolidays).isNotNull();
        assertThat(savedBankHolidays.size()).isEqualTo(1);

        assertThat((int) savedBankHolidays.stream()
                .filter(bankHoliday -> bankHoliday.equals(
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 1, 2))))
                .count()).isEqualTo(0); // should not be added as it already exists

        assertThat((int) savedBankHolidays.stream()
                .filter(bankHoliday -> bankHoliday.equals(
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 3, 17))))
                .count()).isEqualTo(0); // should not be added as it already exists

        assertThat((int) savedBankHolidays.stream()
                .filter(bankHoliday -> bankHoliday.equals(
                        new BankHoliday("northern-ireland", LocalDate.of(2017, 4, 14))))
                .count()).isEqualTo(1);

        verifyNoMoreInteractions(mockBankHolidayClient, mockBankHolidayRepository);
    }

    @Test
    public void shouldGetBankHolidaysForASetOfRegions() {
        // given
        when(mockBankHolidayRepository.findByRegionIn(any())).thenReturn(Set.of(
                        new BankHoliday("england-and-wales", LocalDate.of(2022, 1, 3)),
                        new BankHoliday("england-and-wales", LocalDate.of(2022, 4, 18)),
                        new BankHoliday("scotland", LocalDate.of(2022, 1, 3)),
                        new BankHoliday("scotland", LocalDate.of(2022, 11, 30))
                )
        );

        // whens
        final Set<LocalDate> bankHolidayDatesForRegions = bankHolidayService.getBankHolidayDatesForRegions(
                Set.of(BankHoliday.BankHolidayRegion.SCOTLAND, BankHoliday.BankHolidayRegion.ENGLAND_AND_WALES));

        // should have bank holidays for Scotland, and England and Wales, with duplicates removed
        assertThat(bankHolidayDatesForRegions.size()).isEqualTo(3);
    }
}
