package uk.gov.digital.ho.hocs.casework.contributions;

public enum MPAMContributionStages {
    // Requested stages
    MPAM_TRIAGE_REQUESTED_CONTRIBUTION,
    MPAM_TRIAGE_ESCALATED_REQUESTED_CONTRIBUTION,
    MPAM_DRAFT_REQUESTED_CONTRIBUTION,
    MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION,
    // Received stages
    MPAM_TRIAGE,
    MPAM_TRIAGE_ESCALATE,
    MPAM_DRAFT,
    MPAM_DRAFT_ESCALATE;

    public static boolean contains(String stage) {
        for (MPAMContributionStages c : MPAMContributionStages.values()) {
            if (c.name().equals(stage)) {
                return true;
            }
        }
        return false;
    }
}
