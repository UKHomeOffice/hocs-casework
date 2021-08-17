package uk.gov.digital.ho.hocs.casework.contributions;

public enum ContributionRequestedStages {
    MPAM_TRIAGE_REQUESTED_CONTRIBUTION,
    MPAM_TRIAGE_ESCALATED_REQUESTED_CONTRIBUTION,
    MPAM_DRAFT_REQUESTED_CONTRIBUTION,
    MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION;

    public static boolean contains(String stage) {
        for (ContributionRequestedStages c : ContributionRequestedStages.values()) {
            if (c.name().equals(stage)) {
                return true;
            }
        }
        return false;
    }
}
