package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class DaysElapsedCalculator {

    private final InfoClient infoClient;

    private static final String SYSTEM_DAYS_ELAPSED_FIELD_NAME = "systemDaysElapsed";

    private static final String DATE_RECEIVED_FIELD_NAME = "DateReceived";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    public DaysElapsedCalculator(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    public void updateDaysElapsed(Map<String, String> data, String caseType) {
        int daysElapsed = 0;
        String dateString = data.get(DATE_RECEIVED_FIELD_NAME);
        if (StringUtils.hasText(dateString)) {
            LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
            daysElapsed = infoClient.getWorkingDaysElapsedForCaseType(caseType, dateToCheck);
        }
       data.put(SYSTEM_DAYS_ELAPSED_FIELD_NAME, String.valueOf(daysElapsed));
    }
}
