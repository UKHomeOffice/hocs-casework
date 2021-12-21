package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class DaysElapsedCalculatorImpl implements DaysElapsedCalculator {

    private final WorkingDaysElapsedProvider workingDaysElapsedProvider;

    private static final String DATE_RECEIVED_FIELD_NAME = "DateReceived";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    public DaysElapsedCalculatorImpl(WorkingDaysElapsedProvider workingDaysElapsedProvider) {
        this.workingDaysElapsedProvider = workingDaysElapsedProvider;
    }

    @Override
    public void updateDaysElapsed(Map<String, String> data, String caseType) {
        int daysElapsed = 0;
        String dateString = data.get(DATE_RECEIVED_FIELD_NAME);
        if (StringUtils.hasText(dateString)) {
            LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
            daysElapsed = workingDaysElapsedProvider.getWorkingDaysSince(caseType, dateToCheck);
        }
       data.put(SYSTEM_DAYS_ELAPSED_FIELD_NAME, String.valueOf(daysElapsed));
    }
}
