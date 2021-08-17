package uk.gov.digital.ho.hocs.casework.contributions;

public enum ContributionReceivedStages {
    MPAM_TRIAGE,
    MPAM_TRIAGE_ESCALATE,
    MPAM_DRAFT,
    MPAM_DRAFT_ESCALATE;

    public static boolean contains(String stage) {
        for (ContributionReceivedStages c : ContributionReceivedStages.values()) {
            if (c.name().equals(stage)) {
                return true;
            }
        }
        return false;
    }
}
