package uk.gov.digital.ho.hocs.casework.contributions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import java.time.LocalDate;
import java.util.Comparator;
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

    private final ObjectMapper objectMapper;

    private final InfoClient infoClient;

    public ContributionsProcessorImpl(ObjectMapper objectMapper,
                                      InfoClient infoClient) {
        this.objectMapper = objectMapper;
        this.infoClient = infoClient;
    }

    @Override
    public void processContributionsForCase(CaseData caseData) {

        for (ActiveStage stage : caseData.getActiveStages().stream().collect(Collectors.toSet())) {
            if (Boolean.TRUE.equals(infoClient.getStageContributions(stage.getStageType()))) {
                Set<Contribution> contributions = stage.getCaseData().getSomu_items().stream().map(somuItem -> {
                    try {
                        return objectMapper.readValue(somuItem.getData(), Contribution.class);
                    } catch (JsonProcessingException e) {
                        log.error(String.format("Failed to process somu item %s for reason: %s", somuItem.getUuid(),
                            e.getMessage()), e);
                    }
                    return null;
                }).filter(Objects::nonNull).filter(Contribution::isContribution).collect(Collectors.toSet());

                if (contributions.size()==0) {
                    continue;
                }

                calculateDueContributionDate(contributions).ifPresent(ld -> {
                    log.debug("Setting contribution date {}, for caseId {}", ld, stage.getCaseUUID());
                    stage.setDueContribution(ld.toString());
                });

                highestContributionStatus(contributions).ifPresent(cs -> {
                    log.debug("Setting contribution status {}, for caseId {}", cs.getDisplayedStatus(),
                        stage.getCaseUUID());
                    stage.setContributions(cs.getDisplayedStatus());
                });
            }
        }
    }

    Optional<LocalDate> calculateDueContributionDate(Set<Contribution> contributionSomuItems) {
        return contributionSomuItems.stream().filter(contribution -> contribution.getStatus()==NONE).map(
            Contribution::getDueDate).sorted().findFirst();
    }

    Optional<Contribution.ContributionStatus> highestContributionStatus(Set<Contribution> contributions) {
        return highestContributionStatus(contributions, LocalDate.now());
    }

    Optional<Contribution.ContributionStatus> highestContributionStatus(Set<Contribution> contributionSomuItems,
                                                                        LocalDate now) {
        return contributionSomuItems.stream().map(csi -> {
            Contribution.ContributionStatus contributionStatus = csi.getStatus();
            if (contributionStatus.equals(CONTRIBUTION_RECEIVED) || contributionStatus.equals(CONTRIBUTION_CANCELLED)) {
                return contributionStatus;
            }

            LocalDate contributionDueDate = csi.getDueDate();
            if (contributionDueDate.isBefore(now)) {
                return CONTRIBUTION_OVERDUE;
            }

            return CONTRIBUTION_DUE;
        }).max(Comparator.naturalOrder());
    }

}
