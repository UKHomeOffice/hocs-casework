package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.api.utils.DateUtils;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

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

    @Autowired
    public DeadlineService(final InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    public LocalDate calculateWorkingDaysForStage(final String caseType,
                                                  final LocalDate received,
                                                  final LocalDate caseDeadline,
                                                  final int workingDays) {
        // -2 is a magic number denoting that the case deadline should be used for the stage, rather than calculating
        // an individual deadline for the stage
        if (workingDays==-2) {
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

    private Set<LocalDate> getBankHolidayDatesForCase(String caseType) {
        return infoClient.getExemptionDatesForType(caseType);
    }

    Map<String, LocalDate> getAllStageDeadlinesForCaseType(String type, LocalDate receivedDate) {
        log.info("Getting all stage deadlines for caseType {} with received date of {} ", type, receivedDate);

        final Set<StageTypeDto> allStagesForCaseType = infoClient.getAllStagesForCaseType(type);
        final Set<LocalDate> bankHolidayDatesForCase = getBankHolidayDatesForCase(type);

        Map<String, LocalDate> deadlines = allStagesForCaseType.stream().filter(st -> st.getSla() >= 0).sorted(
            Comparator.comparingInt(StageTypeDto::getSortOrder)).collect(Collectors.toMap(StageTypeDto::getType,
            stageType -> DateUtils.addWorkingDays(receivedDate, stageType.getSla(), bankHolidayDatesForCase),
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        log.info("Got {} deadlines for caseType {} with received date of {} ", deadlines.size(), type, receivedDate);
        return deadlines;
    }

    public int calculateWorkingDaysElapsedForCaseType(String caseType, LocalDate fromDate, LocalDate today) {
        final Set<LocalDate> bankHolidayDatesForCase = getBankHolidayDatesForCase(caseType);

        if (fromDate==null || today.isBefore(fromDate) || today.isEqual(fromDate)) {
            return 0;
        }

        return DateUtils.calculateWorkingDaysElapsedSinceDate(fromDate, today, bankHolidayDatesForCase);
    }

    /**
     * Returns the number of days until the deadline warning should be displayed.
     * <p>
     * When an SLA is manually overridden, ie when a deadline is overridden to an arbitrary date and number of days,
     * such as in extensions, this method is necessary to deduce how many days before the deadline
     * to place the deadline warning.
     *
     * @param extendByNumberOfDays
     * @param caseType
     *
     * @return days until deadline
     */
    public int daysUntilDeadline(int extendByNumberOfDays, CaseDataType caseType) {
        return extendByNumberOfDays - (caseType.getSla() - caseType.getDeadLineWarning());
    }

}
