package uk.gov.digital.ho.hocs.casework.priority;

import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.List;

public interface StagePriorityPolicyProvider {

    List<StagePriorityPolicy> getPolicies(String type);

}
