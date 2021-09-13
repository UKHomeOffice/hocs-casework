package uk.gov.digital.ho.hocs.casework.contributions;

import java.util.Arrays;

public enum ContributionStatus {
    // The order of these values is important
    // They are used for sorting by highest priority
    NONE("", ""), // lowest
    CONTRIBUTION_RECEIVED("contributionReceived", "Received"),
    CONTRIBUTION_CANCELLED("contributionCancelled", "Cancelled"),
    CONTRIBUTION_DUE("contributionDue", "Due"),
    CONTRIBUTION_OVERDUE("contributionOverdue", "Overdue"); // highest

    private final String status;
    private final String displayedStatus;

    ContributionStatus(String status, String displayedStatus) {
        this.status = status;
        this.displayedStatus = displayedStatus;
    }

    public static ContributionStatus getContributionStatus(String status) {
        return Arrays.stream(values()).filter(cs -> cs.status.equals(status)).findFirst().orElseThrow();
    }

    public String getDisplayedStatus() {
        return displayedStatus;
    }
}
