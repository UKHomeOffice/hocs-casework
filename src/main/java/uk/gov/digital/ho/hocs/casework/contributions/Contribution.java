package uk.gov.digital.ho.hocs.casework.contributions;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Setter
@AllArgsConstructor
public class Contribution {

    @JsonProperty("contributionDueDate")
    @JsonAlias({ "approvalRequestDueDate" })
    @Getter
    LocalDate dueDate;

    @JsonProperty("contributionStatus")
    @JsonAlias({ "approvalRequestStatus" })
    String status;

    public ContributionStatus getStatus() {
        return ContributionStatus.getContributionStatus(status);
    }

    public boolean isContribution() {
        return !(dueDate==null && status==null);
    }

    enum ContributionStatus {
        // The order of these values is important
        // They are used for sorting by highest priority
        NONE(List.of(""), ""), // lowest
        CONTRIBUTION_RECEIVED(List.of("contributionReceived", "approvalRequestResponseReceived"), "Received"),
        CONTRIBUTION_CANCELLED(List.of("contributionCancelled", "approvalRequestCancelled"), "Cancelled"),
        CONTRIBUTION_DUE(List.of("contributionDue"), "Due"),
        CONTRIBUTION_OVERDUE(List.of("contributionOverdue"), "Overdue"); // highest

        private final List<String> status;

        private final String displayedStatus;

        ContributionStatus(List<String> status, String displayedStatus) {
            this.status = status;
            this.displayedStatus = displayedStatus;
        }

        public static ContributionStatus getContributionStatus(String status) {
            if (status==null) {
                return NONE;
            }

            return Arrays.stream(values()).filter(cs -> cs.status.contains(status)).findFirst().orElse(NONE);
        }

        public String getDisplayedStatus() {
            return displayedStatus;
        }
    }

}
