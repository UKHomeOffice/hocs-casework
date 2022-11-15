package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataDetailsDto;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataDetailsGroupsRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataDetailsStagesRepository;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CaseDataDetailsService {

    private final CaseDataService caseDataService;

    private final StageService stageService;

    private final CaseDataDetailsGroupsRepository caseDataDetailsGroupsRepository;

    private final CaseDataDetailsStagesRepository caseDataDetailsStagesRepository;

    private final InfoClient infoClient;

    public CaseDataDetailsService(CaseDataService caseDataService,
                                  StageService stageService,
                                  CaseDataDetailsGroupsRepository caseDataDetailsGroupsRepository,
                                  CaseDataDetailsStagesRepository caseDataDetailsStagesRepository,
                                  InfoClient infoClient) {
        this.caseDataService = caseDataService;
        this.stageService = stageService;
        this.caseDataDetailsGroupsRepository = caseDataDetailsGroupsRepository;
        this.caseDataDetailsStagesRepository = caseDataDetailsStagesRepository;
        this.infoClient = infoClient;
    }

    public CaseDataDetailsDto getCaseDataDetails(UUID caseUuid) {
        var caseData = caseDataService.getCaseData(caseUuid);

        var caseDataGroupFields = caseDataDetailsGroupsRepository.getGroupsDetailsFieldsByType(caseData.getType());

        if (!caseDataGroupFields.isEmpty()) {
            return new CaseDataDetailsDto(caseData.getType(), caseData.getReference(), caseDataGroupFields,
                caseData.getDataMap());
        }

        var caseDataStageFields = caseDataDetailsStagesRepository.getStagesDetailsFieldsByType(caseData.getType());

        // Early escape to remove call to info if empty map
        if (caseDataStageFields.isEmpty()) {
            return new CaseDataDetailsDto(caseData.getType(), caseData.getReference(), caseDataStageFields,
                caseData.getDataMap());
        }

        var caseStages = stageService.getAllStagesByCaseUUID(caseUuid).stream().map(BaseStage::getStageType).toList();

        /*
         * TODO: Remove this call by bringing stage types into casework or reworking existing case type implementation
         *  to run in the group format.
         */
        var caseTypeStages = infoClient.getAllStagesForCaseType(caseData.getType()).stream().collect(
            Collectors.toMap(StageTypeDto::getType, StageTypeDto::getDisplayName));

        var filteredCaseStages = caseDataStageFields.entrySet().stream().filter(
            caseDataStage -> caseStages.contains(caseDataStage.getKey())).collect(
            Collectors.toMap(stage -> caseTypeStages.getOrDefault(stage.getKey(), stage.getKey()),
                stage -> caseDataStageFields.get(stage.getKey()), (u, v) -> u, LinkedHashMap::new));

        return new CaseDataDetailsDto(caseData.getType(), caseData.getReference(), filteredCaseStages,
            caseData.getDataMap());
    }

}
