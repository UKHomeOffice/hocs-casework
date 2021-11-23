package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class DaysElapsedCalculatorImpl implements DaysElapsedCalculator {

    private WorkingDaysElapsedProvider workingDaysElapsedProvider;
    private ObjectMapper objectMapper;

    private static final String DATE_RECEIVED_FIELD_NAME = "DateReceived";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    public DaysElapsedCalculatorImpl(WorkingDaysElapsedProvider workingDaysElapsedProvider, ObjectMapper objectMapper) {
        this.workingDaysElapsedProvider = workingDaysElapsedProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void updateDaysElapsed(Stage stage) {
        Map<String, String> data = new HashMap<>(stage.getDataMap(objectMapper));
        int daysElapsed = 0;
        String dateString = data.get(DATE_RECEIVED_FIELD_NAME);
        if (StringUtils.hasText(dateString)) {
            LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT));

            daysElapsed = workingDaysElapsedProvider.getWorkingDaysSince(stage.getCaseDataType(), dateToCheck);
        }

        stage.update(Map.of(SYSTEM_DAYS_ELAPSED_FIELD_NAME, String.valueOf(daysElapsed)), objectMapper);
    }
}
