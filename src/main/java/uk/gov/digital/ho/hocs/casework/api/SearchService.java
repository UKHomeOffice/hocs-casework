package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.searchclient.SearchClient;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SEARCH_STAGE_LIST_EMPTY;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SEARCH_STAGE_LIST_RETRIEVED;

@Slf4j
@Service
public class SearchService {

    private final StageService stageService;
    private final SearchClient searchClient;
    private final InfoClient infoClient;

    private static final Comparator<StageWithCaseData> CREATED_COMPARATOR = Comparator.comparing(StageWithCaseData::getCreated);

    @Autowired
    public SearchService(StageService stageService,
                         SearchClient searchClient,
                         InfoClient infoClient) {
        this.stageService = stageService;
        this.searchClient = searchClient;
        this.infoClient = infoClient;
    }

    public Set<StageWithCaseData> search(SearchRequest searchRequest) {
        log.debug("Getting Stages for Search Request");
        Set<UUID> caseUUIDs = searchClient.search(searchRequest);
        if (caseUUIDs.isEmpty()) {
            log.info("No cases - Returning 0 Stages", value(EVENT, SEARCH_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<StageWithCaseData> stages = stageService.getAllStagesByCaseUUIDIn(caseUUIDs);

        // done like this because the case relationship is in the info schema
        // get the case types with a previous case type and reduce to
        // Map<K, V>, - K is the previousCaseType, V is the caseType
        Map<String, String> caseTypes = infoClient.getAllCaseTypes()
                .stream()
                .filter( caseType -> Objects.nonNull(caseType.getPreviousCaseType()))
                .collect(Collectors.toMap(CaseDataType::getPreviousCaseType, CaseDataType::getDisplayCode));

        // map the previous case type on to the cases found
        // only stages with completed cases have the next caseType
        stages.stream()
                .filter(StageWithCaseData::getCompleted)
                .forEach(stage -> stage.setNextCaseType(caseTypes.get(stage.getCaseDataType())));

        log.info("Returning {} Stages", stages.size(), value(EVENT, SEARCH_STAGE_LIST_RETRIEVED));
        return groupByCaseUUID(stages);

    }

    public static Set<StageWithCaseData> reduceToMostActive(Set<StageWithCaseData> stages) {
        return reduceToMostActive(new ArrayList<>(stages)).collect(Collectors.toSet());
    }

    private static Set<StageWithCaseData> groupByCaseUUID(Set<? extends StageWithCaseData> stages) {

        // Group the stages by case UUID
        Map<UUID, List<StageWithCaseData>> groupedStages = stages.stream().collect(Collectors.groupingBy(StageWithCaseData::getCaseUUID));

        // for each of the entry sets, filter out none-active stages, unless there are no active stages then use the latest stage
        return groupedStages.entrySet().stream().flatMap(s -> reduceToMostActive(s.getValue())).collect(Collectors.toSet());
    }

    private static Stream<StageWithCaseData> reduceToMostActive(List<StageWithCaseData> stages) {
        Supplier<Stream<StageWithCaseData>> stageSupplier = stages::stream;

        // If any stages are active
        if (stageSupplier.get().anyMatch(StageWithCaseData::isActive)) {
            return stageSupplier.get().filter(StageWithCaseData::isActive);
        } else {
            // return the most recent stage.
            Optional<StageWithCaseData> maxDatedStage = stageSupplier.get().max(CREATED_COMPARATOR);
            return maxDatedStage.stream();
        }
    }
}
