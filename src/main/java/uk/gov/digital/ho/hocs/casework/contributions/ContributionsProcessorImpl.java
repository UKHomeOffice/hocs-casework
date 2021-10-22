package uk.gov.digital.ho.hocs.casework.contributions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.SomuItemService;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_CANCELLED;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_DUE;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_OVERDUE;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_RECEIVED;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.NONE;

@Service
@Slf4j
public class ContributionsProcessorImpl implements ContributionsProcessor {

    private static final List<String> COMPLIANT_CASE_TYPES = List.of("COMP", "COMP2");
    private final SomuItemService somuItemService;
    private final ObjectMapper objectMapper;

    public ContributionsProcessorImpl(ObjectMapper objectMapper, SomuItemService somuItemService) {
        this.somuItemService = somuItemService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processContributionsForStages(Set<Stage> stages) {
        Set<SomuItem> contributionSomuItems =
                somuItemService.getCaseItemsByCaseUuids(stages.stream().map(Stage::getCaseUUID).collect(Collectors.toSet()));

        if (contributionSomuItems.size() == 0) {
            return;
        }

        for (Stage stage :
                stages) {
            if (MPAMContributionStages.contains(stage.getStageType())
                    || FOIContributionStages.contains(stage.getStageType())
                    || CompContributionStages.contains(stage.getStageType())) {
                Set<Contribution> contributions =
                        contributionSomuItems.stream()
                                .filter(somuItem -> somuItem.getCaseUuid().equals(stage.getCaseUUID()))
                                .map(somuItem -> {
                                    try {
                                        return objectMapper.readValue(somuItem.getData(), Contribution.class);
                                    } catch (JsonProcessingException e) {
                                        log.error(
                                                String.format("Failed to process somu item %s for reason: %s",
                                                    somuItem.getUuid(), e.getMessage()),
                                                e);
                                    }
                                    return null;
                                })
                                .filter(Objects::nonNull)
                                .filter(Contribution::isContribution).collect(Collectors.toSet());

                if (contributions.size() == 0) {
                    continue;
                }

                calculateDueContributionDate(contributions)
                        .ifPresent(ld -> {
                            log.info("Setting contribution date {}, for caseId {}", ld, stage.getCaseUUID());
                            stage.setDueContribution(ld.toString());
                        });

                highestContributionStatus(contributions)
                        .ifPresent(cs -> {
                            log.info("Setting contribution status {}, for caseId {}", cs.getDisplayedStatus(), stage.getCaseUUID());
                            stage.setContributions(cs.getDisplayedStatus());
                        });
            }
        }
    }

    Optional<LocalDate> calculateDueContributionDate(Set<Contribution> contributionSomuItems) {
        return contributionSomuItems
                .stream()
                .filter(contribution -> contribution.getStatus() == NONE)
                .map(Contribution::getDueDate)
                .sorted()
                .findFirst();
    }

    Optional<Contribution.ContributionStatus> highestContributionStatus(Set<Contribution> contributions) {
        return highestContributionStatus(contributions, LocalDate.now());
    }

    Optional<Contribution.ContributionStatus> highestContributionStatus(Set<Contribution> contributionSomuItems, LocalDate now) {
        return contributionSomuItems
                .stream()
                .map(csi -> {
                    Contribution.ContributionStatus contributionStatus = csi.getStatus();
                    if (contributionStatus.equals(CONTRIBUTION_RECEIVED) ||
                        contributionStatus.equals(CONTRIBUTION_CANCELLED)) {
                        return contributionStatus;
                    }

                    LocalDate contributionDueDate = csi.getDueDate();
                    if (contributionDueDate.isBefore(now)) {
                        return CONTRIBUTION_OVERDUE;
                    }

                    return CONTRIBUTION_DUE;
                })
                .max(Comparator.naturalOrder());
    }



}
