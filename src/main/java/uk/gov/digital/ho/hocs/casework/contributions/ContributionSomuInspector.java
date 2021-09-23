package uk.gov.digital.ho.hocs.casework.contributions;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ContributionSomuInspector {
    private final ReadContext ctx;

    public ContributionSomuInspector(SomuItem somuItem) {
        ctx = JsonPath.parse(somuItem.getData());
    }

    public ContributionStatus getContributionStatus() {
        ContributionStatus contributionStatus;
        try {
            contributionStatus = ContributionStatus.getContributionStatus(ctx.read("$.contributionStatus"));
        } catch (PathNotFoundException e) {
            contributionStatus = ContributionStatus.NONE;
        }
        return contributionStatus;
    }

    public boolean hasContributionStatus() {
        return !getContributionStatus().equals(ContributionStatus.NONE);
    }

    public String getContributionDueDate() {
        return ctx.read("$.contributionDueDate");
    }

    public LocalDate getContributionDueLocalDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("y-M-d");
        String contributionDueDate = getContributionDueDate();
        return LocalDate.parse(contributionDueDate, dtf);
    }
}
