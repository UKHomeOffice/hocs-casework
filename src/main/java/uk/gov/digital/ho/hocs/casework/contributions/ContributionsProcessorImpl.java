package uk.gov.digital.ho.hocs.casework.contributions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.SomuItemService;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_CANCELLED;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_DUE;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_OVERDUE;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.CONTRIBUTION_RECEIVED;
import static uk.gov.digital.ho.hocs.casework.contributions.Contribution.ContributionStatus.NONE;

@Service
@Slf4j
public class ContributionsProcessorImpl implements ContributionsProcessor {

    private final SomuItemService somuItemService;

    private final ObjectMapper objectMapper;

    private final InfoClient infoClient;

    public ContributionsProcessorImpl(ObjectMapper objectMapper,
                                      SomuItemService somuItemService,
                                      InfoClient infoClient) {
        this.somuItemService = somuItemService;
        this.objectMapper = objectMapper;
        this.infoClient = infoClient;
    }

    @Override
    public void processContributionsForStages(Collection<StageWithCaseData> stages) {
        log.info("Grouping Contributions");
        Map<UUID, List<SomuItem>> allSomuItems = somuItemService.getCaseItemsByCaseUuids(
                stages.stream().map(BaseStage::getCaseUUID).collect(Collectors.toList()))
            .stream().collect(Collectors.groupingBy(SomuItem::getCaseUuid, Collectors.toList()));
        log.info("Finished grouping Contributions");
        if (allSomuItems.size()==0) {
            return;
        }
        log.info("Processing Contributions mapsize:{}", allSomuItems.size());
        for (StageWithCaseData stage : stages) {
            if (infoClient.getStageContributions(stage.getStageType())) {
                List<Contribution> contributions = allSomuItems.getOrDefault(stage.getCaseUUID(), List.of()).stream().map(somuItem -> {
                    try {
                        return objectMapper.readValue(somuItem.getData(), Contribution.class);
                    } catch (JsonProcessingException e) {
                        log.error(String.format("Failed to process somu item %s for reason: %s", somuItem.getUuid(),
                            e.getMessage()), e);
                    }
                    return null;
                }).filter(it -> it !=null && it.isContribution()).collect(Collectors.toList());

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
        log.info("Finished Processing Contributions");

    }

    Optional<LocalDate> calculateDueContributionDate(Collection<Contribution> contributionSomuItems) {
        return contributionSomuItems.stream().filter(contribution -> contribution.getStatus()==NONE).map(
            Contribution::getDueDate).sorted().findFirst();
    }

    Optional<Contribution.ContributionStatus> highestContributionStatus(Collection<Contribution> contributions) {
        return highestContributionStatus(contributions, LocalDate.now());
    }

    Optional<Contribution.ContributionStatus> highestContributionStatus(Collection<Contribution> contributionSomuItems,
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
