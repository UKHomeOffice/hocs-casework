package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.BankHolidaysByRegionDto;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.client.bankHolidayClient.BankHolidayClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BankHoliday;
import uk.gov.digital.ho.hocs.casework.domain.repository.BankHolidayRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class BankHolidayService {
    private final BankHolidayRepository bankHolidayRepository;
    private final BankHolidayClient bankHolidayClient;

    private final InfoClient infoClient;

    @Autowired
    public BankHolidayService(final BankHolidayRepository bankHolidayRepository,
                              final BankHolidayClient bankHolidayClient,
                              final InfoClient infoClient) {
        this.bankHolidayRepository = bankHolidayRepository;
        this.bankHolidayClient = bankHolidayClient;
        this.infoClient = infoClient;
    }

    public void refreshBankHolidayTable() {
        log.info("Refreshing bank holidays");

        final Map<String, BankHolidaysByRegionDto> bankHolidaysByRegion =
                bankHolidayClient.getBankHolidays();
        final Set<BankHoliday> currentBankHolidays = bankHolidayRepository.findAll();

        final List<BankHoliday> newBankHolidays = filterOutExistingBankHolidays(bankHolidaysByRegion, currentBankHolidays);

        bankHolidayRepository.saveAll(newBankHolidays);
        log.info("Saved {} new bank holidays", newBankHolidays.size());
    }

    private List<BankHoliday> filterOutExistingBankHolidays(
            final Map<String, BankHolidaysByRegionDto> bankHolidayRegions,
            final Set<BankHoliday> currentBankHolidays
    ) {
        return bankHolidayRegions.entrySet().stream().flatMap(
                this::createBankHolidayEntitiesFromRegionInDto
        ).filter(bankHoliday -> {
            if (currentBankHolidays.contains(bankHoliday)) {
                log.info("Bank Holiday with date {} for {} already exists",
                        bankHoliday.getDate(), bankHoliday.getRegion());
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private Stream<BankHoliday> createBankHolidayEntitiesFromRegionInDto(
            final Map.Entry<String, BankHolidaysByRegionDto> region) {
        final String regionName = region.getKey();
        final List<BankHolidaysByRegionDto.Event> events = region.getValue().getEvents();

        return events.stream().map(event ->
                new BankHoliday(regionName, LocalDate.parse(event.getDate())));
    }

    public Set<LocalDate> getBankHolidayDatesForRegions(final Set<BankHoliday.BankHolidayRegion> regions) {
        final Set<BankHoliday> bankHolidaysInRegions = bankHolidayRepository.findByRegionIn(regions);

        return getBankHolidaysDatesAsSet(bankHolidaysInRegions);
    }

    public Set<LocalDate> getBankHolidayDatesForCaseType(final String caseType) {
        final Set<BankHoliday.BankHolidayRegion> regions =
                infoClient.getBankHolidayRegionsByCaseType(caseType)
                        .stream()
                        .map(BankHoliday.BankHolidayRegion::valueOf)
                        .collect(Collectors.toSet());

        final Set<BankHoliday> bankHolidaysInRegions = bankHolidayRepository.findByRegionIn(regions);

        return getBankHolidaysDatesAsSet(bankHolidaysInRegions);
    }

    private Set<LocalDate> getBankHolidaysDatesAsSet(Set<BankHoliday> bankHolidaysInRegions) {
        // extract dates from BankHolidays and remove duplicates by putting them in a set (to enforce uniqueness)
        return bankHolidaysInRegions.stream().map(BankHoliday::getDate).collect(Collectors.toSet());
    }
}
