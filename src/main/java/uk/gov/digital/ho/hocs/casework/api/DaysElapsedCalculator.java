package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.priority.policy.WorkingDaysElapsedPolicy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class DaysElapsedCalculator {

    private ObjectMapper objectMapper;

    private static final String DATE_RECEIVED_FIELD_NAME = "DateReceived";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    String SYSTEM_DAYS_ELAPSED_FIELD_NAME = "systemDaysElapsed";

    @Autowired
    public DaysElapsedCalculator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void updateDaysElapsed(Stage stage, Set<LocalDate> exemptions) {
        log.info("Updating days elapsed for stage : {}", stage.getCaseUUID());
        Map<String, String> data = new HashMap<>(stage.getDataMap(objectMapper));
        int daysElapsed = 0;
        String dateString = data.get(DATE_RECEIVED_FIELD_NAME);
        if (StringUtils.hasText(dateString)) {
            LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
            daysElapsed = WorkingDaysElapsedPolicy.calculateWorkingDaysElapsedForCaseType(dateToCheck, exemptions);
        }

        stage.update(Map.of(SYSTEM_DAYS_ELAPSED_FIELD_NAME, String.valueOf(daysElapsed)), objectMapper);
    }
}
