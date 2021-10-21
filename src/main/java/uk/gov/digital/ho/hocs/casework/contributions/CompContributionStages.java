package uk.gov.digital.ho.hocs.casework.contributions;

public enum CompContributionStages {
    // Requested stages
    COMP_SERVICE_TRIAGE,
    COMP2_SERVICE_TRIAGE,
    COMP_SERVICE_ESCALATE,
    COMP2_SERVICE_ESCALATE,
    COMP_EXGRATIA_TRIAGE,
    COMP_MINORMISCONDUCT_TRIAGE;

    public static boolean contains(String stage) {
        for (CompContributionStages c : CompContributionStages.values()) {
            if (c.name().equals(stage)) {
                return true;
            }
        }
        return false;
    }
}
