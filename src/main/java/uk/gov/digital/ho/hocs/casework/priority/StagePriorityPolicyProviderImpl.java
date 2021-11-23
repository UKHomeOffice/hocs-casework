package uk.gov.digital.ho.hocs.casework.priority;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.WorkingDaysElapsedProvider;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PriorityPolicyDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.priority.policy.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class StagePriorityPolicyProviderImpl implements StagePriorityPolicyProvider {
    private static final String POLICY_TYPE_SIMPLE_STRING = "SimpleStringPropertyPolicy";
    private static final String POLICY_TYPE_JOINED_STRING = "JoinedStringPropertyPolicy";
    private static final String POLICY_TYPE_DAYS_ELAPSED = "DaysElapsedPolicy";
    private static final String POLICY_TYPE_WORKING_DAYS_ELAPSED = "WorkingDaysElapsedPolicy";

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

    @Autowired
    private InfoClient infoClient;

    @Autowired
    private WorkingDaysElapsedProvider workingDaysElapsedProvider;

    @Cacheable(value = "StagePriorityPolicyProviderImpl_getPolicies")
    @Override
    public List<StagePriorityPolicy> getPolicies(String caseType) {
        List<PriorityPolicyDto> policies = infoClient.getPriorityPoliciesForCaseType(caseType);
        return policies.stream().map(this::convert).collect(Collectors.toList());
    }


    private StagePriorityPolicy convert(PriorityPolicyDto policyDto) {
        Map<String, String> data = policyDto.getConfig();
        switch (policyDto.getPolicyType()) {
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
            default:
                throw new ApplicationExceptions.InvalidPriorityTypeException("Cannot map %s priority policy type", policyDto.getPolicyType());
        }
    }
}
