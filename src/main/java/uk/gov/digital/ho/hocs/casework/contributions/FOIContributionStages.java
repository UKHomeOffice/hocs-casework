package uk.gov.digital.ho.hocs.casework.contributions;

public enum FOIContributionStages {
    // Requested stages
    FOI_APPROVAL,
    FOI_DRAFT;

    public static boolean contains(String stage) {
        for (FOIContributionStages c : FOIContributionStages.values()) {
            if (c.name().equals(stage)) {
                return true;
            }
        }
        return false;
    }
}
