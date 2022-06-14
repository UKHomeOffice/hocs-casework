package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStagesResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.defer;
import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUTH_FILTER_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Slf4j
@Service
public class GetStagesResponseFilterService implements AuthFilter {

    private final UserPermissionsService userPermissionsService;

    @Autowired
    public GetStagesResponseFilterService(UserPermissionsService userPermissionsService) {
        this.userPermissionsService = userPermissionsService;
    }

    @Override
    public String getKey() {
        return GetStagesResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {

        GetStagesResponse getStagesResponse = verifyAndReturnAsObjectType(responseEntityToFilter, GetStagesResponse.class);

        if (getStagesResponse == null) {
            return responseEntityToFilter;
        }

        log.debug("Filtering GetStagesResponse for request from userId: {}", userPermissionsService.getUserId());

        Set<StageWithCaseData> replacementStages = getStagesResponse.getStages().stream().map((stageWithCaseData) -> {
            AccessLevel accessLevel = userPermissionsService.getMaxAccessLevel(stageWithCaseData.getCaseDataType());
            accessLevel = accessLevel.getLevel() > AccessLevel.READ.getLevel() ? AccessLevel.READ : accessLevel;
            List<FieldDto> permittedFields = userPermissionsService.getFieldsByCaseTypeAndPermissionLevel(stageWithCaseData.getCaseDataType(), accessLevel);

            Map<String, String> replacementStageResponseDataMap = new HashMap<>();
            permittedFields.forEach((FieldDto restrictedField) -> {
                if (stageWithCaseData.getData().containsKey(restrictedField.getName())) {
                    replacementStageResponseDataMap.put(
                            restrictedField.getName(),
                            stageWithCaseData.getData().get(restrictedField.getName())
                    );
                }
            });

            StageWithCaseData replacementStageWithCaseData = new StageWithCaseData(
                    stageWithCaseData.getUuid(),
                    stageWithCaseData.getTransitionNoteUUID(),
                    stageWithCaseData.getCaseReference(),
                    stageWithCaseData.getCaseDataType(),
                    stageWithCaseData.getCaseCreated(),
                    stageWithCaseData.getCorrespondents(),
                    stageWithCaseData.getAssignedTopic(),
                    stageWithCaseData.getSomu(),
                    stageWithCaseData.getTag(),
                    stageWithCaseData.getDueContribution(),
                    stageWithCaseData.getContributions(),
                    stageWithCaseData.getNextCaseType(),
                    stageWithCaseData.getNextCaseReference(),
                    stageWithCaseData.getNextCaseUUID(),
                    false
            );

            replacementStageWithCaseData.putAllData(replacementStageResponseDataMap);
            return replacementStageWithCaseData;
        }).collect(Collectors.toSet());

        GetStagesResponse replacementGetStagesResponse = GetStagesResponse.from(replacementStages);

        log.info("Issuing filtered GetCaseResponse for userId: {}", userPermissionsService.getUserId(), value(EVENT, AUTH_FILTER_SUCCESS));
        return new ResponseEntity<GetStagesResponse>(replacementGetStagesResponse, responseEntityToFilter.getStatusCode());
    }
}
