package uk.gov.digital.ho.hocs.casework.contributions;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.SomuItemService;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static uk.gov.digital.ho.hocs.casework.contributions.ContributionStatus.*;

@Service
@Slf4j
public class ContributionsProcessorImpl implements ContributionsProcessor {

    public static final String COMPLIANT_CASE_TYPE = "COMP";
    private final SomuItemService somuItemService;

    @Autowired
    public ContributionsProcessorImpl(SomuItemService somuItemService) {
        this.somuItemService = somuItemService;
    }

    @Override
    public void processContributionsForStage(Stage stage) {
        Set<SomuItem> contributionSomuItems = somuItemService.getCaseSomuItemsBySomuType(stage.getCaseUUID());

        if (ContributionRequestedStages.contains(stage.getStageType()) ||
                ContributionReceivedStages.contains(stage.getStageType())) {
            calculateDueContributionDate(contributionSomuItems)
                    .ifPresent(ld -> stage.setDueContribution(ld.toString()));
        } 
        
        if (COMPLIANT_CASE_TYPE.equals(stage.getCaseDataType())) {
            calculateDueContributionDate(contributionSomuItems)
                    .ifPresent(ld -> stage.setDueContribution(ld.toString()));
            
            highestContributionStatus(contributionSomuItems)
                    .ifPresent(ld -> stage.setContributions(ld.getDisplayedStatus()));
        } 
    }

    Optional<LocalDate> calculateDueContributionDate(Set<SomuItem> contributionSomuItems) {
        return contributionSomuItems
                .stream()
                .filter(csi -> !(new ContributionSomuInspector(csi).hasContributionStatus()))
                .map(csi -> new ContributionSomuInspector(csi).getContributionDueLocalDate())
                .sorted()
                .findFirst();
    }
    
    Optional<ContributionStatus> highestContributionStatus(Set<SomuItem> contributionSomuItems) {
        return highestContributionStatus(contributionSomuItems, LocalDate.now());
    }
    Optional<ContributionStatus> highestContributionStatus(Set<SomuItem> contributionSomuItems, LocalDate now) {
        return contributionSomuItems
                .stream()
                .map(csi -> {
                    ContributionStatus contributionStatus = new ContributionSomuInspector(csi).getContributionStatus();
                    if (contributionStatus.equals(CONTRIBUTION_RECEIVED) || contributionStatus.equals(CONTRIBUTION_CANCELLED)) {
                        return contributionStatus;
                    } else {
                        LocalDate contributionDueDate = new ContributionSomuInspector(csi).getContributionDueLocalDate();
                        if (contributionDueDate.plus(1, DAYS).isBefore(now)) {
                            return CONTRIBUTION_OVERDUE;
                        } else {
                            return CONTRIBUTION_DUE;
                        }
                    }
                })
                .max(Comparator.naturalOrder());
    }
}
