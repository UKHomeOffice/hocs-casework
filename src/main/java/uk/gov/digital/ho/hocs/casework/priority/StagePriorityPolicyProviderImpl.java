package uk.gov.digital.ho.hocs.casework.priority;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.WorkingDaysElapsedProvider;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.PriorityPolicies;
import uk.gov.digital.ho.hocs.casework.domain.repository.PriorityPolicyRepository;
import uk.gov.digital.ho.hocs.casework.priority.policy.*;

import java.util.List;
import java.util.Map;

@Service
public class StagePriorityPolicyProviderImpl implements StagePriorityPolicyProvider {
    private static final String POLICY_TYPE_SIMPLE_STRING = "SimpleStringPropertyPolicy";
    private static final String POLICY_TYPE_JOINED_STRING = "JoinedStringPropertyPolicy";
    private static final String POLICY_TYPE_DAYS_ELAPSED = "DaysElapsedPolicy";
    private static final String POLICY_TYPE_WORKING_DAYS_ELAPSED = "WorkingDaysElapsedPolicy";
    private static final String POLICY_TYPE_DEADLINE = "DeadlinePolicy";

    private static final String PROPERTY_NAME = "propertyName";
    private static final String PROPERTY_VALUE = "propertyValue";
    private static final String PROPERTY_NAME_2 = "propertyName2";
    private static final String PROPERTY_VALUE_2 = "propertyValue2";
    private static final String PROPERTY_POINTS_TO_AWARD = "pointsToAward";
    private static final String PROPERTY_DATE_FIELD_NAME = "dateFieldName";
    private static final String PROPERTY_DATE_FIELD_FORMAT = "dateFormat";
    private static final String PROPERTY_CAP_NUMBER_OF_DAYS = "capNumberOfDays";
    private static final String PROPERTY_CAP_POINTS_TO_AWARD = "capPointsToAward";
    private static final String PROPERTY_POINTS_TO_AWARD_PER_DAY = "pointsToAwardPerDay";

    private final PriorityPolicyRepository priorityPolicyRepository;
    private final WorkingDaysElapsedProvider workingDaysElapsedProvider;

    public StagePriorityPolicyProviderImpl(PriorityPolicyRepository priorityPolicyRepository,
                                           WorkingDaysElapsedProvider workingDaysElapsedProvider) {
        this.priorityPolicyRepository = priorityPolicyRepository;
        this.workingDaysElapsedProvider = workingDaysElapsedProvider;
    }

    @Override
    public List<StagePriorityPolicy> getPolicies(String caseType) {
        return priorityPolicyRepository.getByCaseType(caseType)
                .stream().map(this::convert).toList();
    }

    private StagePriorityPolicy convert(PriorityPolicies.PriorityPolicy policyDto) {
        Map<String, String> data = policyDto.getConfig();

        switch (policyDto.getType()) {
            case POLICY_TYPE_SIMPLE_STRING:
                return new SimpleStringPropertyPolicy(data.get(PROPERTY_NAME), data.get(PROPERTY_VALUE),
                        Double.parseDouble(data.get(PROPERTY_POINTS_TO_AWARD)));
            case POLICY_TYPE_JOINED_STRING:
                return new JoinedStringPropertyPolicy(data.get(PROPERTY_NAME), data.get(PROPERTY_VALUE),
                        data.get(PROPERTY_NAME_2), data.get(PROPERTY_VALUE_2),
                        Double.parseDouble(data.get(PROPERTY_POINTS_TO_AWARD)));
            case POLICY_TYPE_DAYS_ELAPSED:
                return new DaysElapsedPolicy(data.get(PROPERTY_NAME), data.get(PROPERTY_VALUE),
                        data.get(PROPERTY_DATE_FIELD_NAME), data.get(PROPERTY_DATE_FIELD_FORMAT),
                        Integer.parseInt(data.get(PROPERTY_CAP_NUMBER_OF_DAYS)),
                        Double.parseDouble(data.get(PROPERTY_CAP_POINTS_TO_AWARD)),
                        Double.parseDouble(data.get(PROPERTY_POINTS_TO_AWARD_PER_DAY)));
            case POLICY_TYPE_WORKING_DAYS_ELAPSED:
                return new WorkingDaysElapsedPolicy(workingDaysElapsedProvider, data.get(PROPERTY_NAME), data.get(PROPERTY_VALUE),
                        data.get(PROPERTY_DATE_FIELD_NAME), data.get(PROPERTY_DATE_FIELD_FORMAT),
                        Integer.parseInt(data.get(PROPERTY_CAP_NUMBER_OF_DAYS)),
                        Double.parseDouble(data.get(PROPERTY_CAP_POINTS_TO_AWARD)),
                        Double.parseDouble(data.get(PROPERTY_POINTS_TO_AWARD_PER_DAY)));
            case POLICY_TYPE_DEADLINE:
                return new DeadlinePolicy();
            default:
                throw new ApplicationExceptions.InvalidPriorityTypeException("Cannot map %s priority policy type", policyDto.getType());
        }
    }
}
