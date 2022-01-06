package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.api.utils.DateUtils;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BankHoliday;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This service handles deadlines. These have been factored into their own service to avoid circular dependencies.
 */
@Slf4j
@Service
public class DeadlineService {

    private final InfoClient infoClient;
    private final BankHolidayService bankHolidayService;

    @Autowired
    public DeadlineService(final InfoClient infoClient,
                           final BankHolidayService bankHolidayService) {
        this.infoClient = infoClient;
        this.bankHolidayService = bankHolidayService;
    }

    public LocalDate calculateWorkingDaysForStage(final String caseType,
                                                  final LocalDate received,
                                                  final LocalDate caseDeadline,
                                                  final int workingDays) {
        // -2 is a magic number denoting that the case deadline should be used for the stage, rather than calculating
        // an individual deadline for the stage
        if(workingDays == -2) {
            return caseDeadline;
        }

        return calculateWorkingDaysForCaseType(caseType, received, workingDays);
    }

    public LocalDate calculateWorkingDaysForCaseType(String caseType, LocalDate receivedDate, int workingDays) {
        final Set<LocalDate> bankHolidayDatesForCase = getBankHolidayDatesForCase(caseType);

        return DateUtils.addWorkingDays(receivedDate, workingDays, bankHolidayDatesForCase);
    }

    public int calculateRemainingWorkingDaysForCaseType(String caseType, LocalDate receivedDate, LocalDate todaysDate) {
        final Set<LocalDate> bankHolidayDatesForCase = getBankHolidayDatesForCase(caseType);

        return DateUtils.calculateRemainingWorkingDays(todaysDate, receivedDate, bankHolidayDatesForCase);
    }

    /** todo: get final confirmation - should this method get the bank holidays from the Casework service,
    or from the Info service? **/
    private Set<LocalDate> getBankHolidayDatesForCase(String caseType) {
        // get bank holiday dates
        Set<BankHoliday.BankHolidayRegion> bankHolidayRegionsForCase =
                infoClient.getBankHolidayRegionsByCaseType(caseType)
                        .stream()
                        .map(BankHoliday.BankHolidayRegion::valueOf)
                        .collect(Collectors.toSet());

        final Set<LocalDate> bankHolidayDatesForCase =
                bankHolidayService.getBankHolidayDatesForRegions(bankHolidayRegionsForCase);
        return bankHolidayDatesForCase;
    }

    Map<String, LocalDate> getAllStageDeadlinesForCaseType(String type, LocalDate receivedDate) {
        log.debug("Getting all stage deadlines for caseType {} with received date of {} ", type, receivedDate);

        final Set<StageTypeDto> allStagesForCaseType = infoClient.getAllStagesForCaseType(type);
        final Set<LocalDate> bankHolidayDatesForCase = bankHolidayService.getBankHolidayDatesForCaseType(type);

        Map<String, LocalDate> deadlines = allStagesForCaseType.stream().filter(st -> st.getSla() >= 0)
                .sorted(Comparator.comparingInt(StageTypeDto::getSortOrder))
                .collect(Collectors.toMap(StageTypeDto::getType,
                        stageType -> DateUtils.addWorkingDays(receivedDate, stageType.getSla(), bankHolidayDatesForCase),
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        log.info("Got {} deadlines for caseType {} with received date of {} ", deadlines.size(), type, receivedDate);
        return deadlines;
    }

    public int calculateWorkingDaysElapsedForCaseType(String caseType, LocalDate fromDate, LocalDate today) {
        final Set<LocalDate> bankHolidayDatesForCase = bankHolidayService.getBankHolidayDatesForCaseType(caseType);

        if (fromDate == null || today.isBefore(fromDate) || today.isEqual(fromDate)) {
            return 0;
        }

        return DateUtils.calculateWorkingDaysElapsedSinceDate(fromDate, today, bankHolidayDatesForCase);
    }
}
