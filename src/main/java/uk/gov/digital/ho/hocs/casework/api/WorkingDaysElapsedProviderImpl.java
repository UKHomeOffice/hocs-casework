package uk.gov.digital.ho.hocs.casework.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class WorkingDaysElapsedProviderImpl implements WorkingDaysElapsedProvider {

    @Autowired
    private DeadlineService deadlineService;

    @Override
    public Integer getWorkingDaysSince(String caseType, LocalDate fromDate) {
        return deadlineService.calculateWorkingDaysElapsedForCaseType(caseType, fromDate, LocalDate.now());
    }
}
