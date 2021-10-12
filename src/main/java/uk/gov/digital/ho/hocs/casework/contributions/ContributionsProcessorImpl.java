package uk.gov.digital.ho.hocs.casework.contributions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.SomuItemService;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (MPAMContributionStages.contains(stage.getStageType())
                || FOIContributionStages.contains(stage.getStageType())
                || COMPLIANT_CASE_TYPE.equals(stage.getCaseDataType())) {

            Set<SomuItem> contributionSomuItems = somuItemService.getCaseSomuItemsBySomuType(stage.getCaseUUID(), false);

            contributionSomuItems = filterContributions(contributionSomuItems);

            calculateDueContributionDate(contributionSomuItems)
                    .ifPresent(ld -> {
                        log.info("Setting contribution date {}, for caseId {}", ld, stage.getCaseUUID());
                        stage.setDueContribution(ld.toString());
                    });

            highestContributionStatus(contributionSomuItems)
                    .ifPresent(cs -> {
                        log.info("Setting contribution status {}, for caseId {}", cs.getDisplayedStatus(), stage.getCaseUUID());
                        stage.setContributions(cs.getDisplayedStatus());
                    });
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

    Set<SomuItem> filterContributions(Set<SomuItem> items) {
        return items.stream()
                .filter(item -> new ContributionSomuInspector(item).isContribution())
                .collect(Collectors.toSet());
    }
}
