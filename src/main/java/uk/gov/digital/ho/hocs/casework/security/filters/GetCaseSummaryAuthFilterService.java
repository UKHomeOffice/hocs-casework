package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.AdditionalFieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUTH_FILTER_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Slf4j
@Service
public class GetCaseSummaryAuthFilterService implements AuthFilter {

    private final UserPermissionsService userPermissionsService;

    @Autowired
    public GetCaseSummaryAuthFilterService(UserPermissionsService userPermissionsService) {
        this.userPermissionsService = userPermissionsService;
    }

    @Override
    public String getKey() {
        return GetCaseSummaryResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {
        // todo: expand to filter out Case Action data at later date. See HOCS-4596, not applicable for initial use case SMC caseType

        GetCaseSummaryResponse getCaseSummaryResponse = verifyAndReturnAsObjectType(responseEntityToFilter, GetCaseSummaryResponse.class);

        if (getCaseSummaryResponse == null  || getCaseSummaryResponse.getAdditionalFields() == null) {
            return responseEntityToFilter;
        }

        UUID userId = userPermissionsService.getUserId();
        log.debug("Filtering GetCaseSummaryResponse for request by userId: {}", userId);

        Map<String, FieldDto> permittedFields = new HashMap<>();
        userPermissionsService.getFieldsByCaseTypeAndPermissionLevel(getCaseSummaryResponse.getType(), userAccessLevel)
                .forEach(fieldDto -> permittedFields.put(fieldDto.getName(),fieldDto));

        List<AdditionalFieldDto> replacementList = new ArrayList<>();
        getCaseSummaryResponse.getAdditionalFields()
                .forEach((AdditionalFieldDto additionalFieldDto) -> {
                    if (permittedFields.containsKey(additionalFieldDto.getName())) {
                        replacementList.add(additionalFieldDto);
                    }
                });

        replacementList.sort(Comparator.comparing(AdditionalFieldDto::getLabel));

        SettableAdditionalFieldsGetCaseSummaryResponse response = new SettableAdditionalFieldsGetCaseSummaryResponse(getCaseSummaryResponse);
        response.setAdditionalFields(replacementList);
        response.hideActiveStageInfo();

        log.info("Issuing filtered GetCaseSummaryResponse for userId: {}", userId, value(EVENT, AUTH_FILTER_SUCCESS));
        return new ResponseEntity<GetCaseSummaryResponse>(response, responseEntityToFilter.getStatusCode());
    }

    public static class SettableAdditionalFieldsGetCaseSummaryResponse extends GetCaseSummaryResponse {

        public SettableAdditionalFieldsGetCaseSummaryResponse(GetCaseSummaryResponse response) {
            super(
                    response.getType(),
                    response.getCaseCreated(),
                    response.getCaseDeadline(),
                    response.getStageDeadlines(),
                    response.getAdditionalFields(),
                    response.getPrimaryCorrespondent(),
                    response.getPrimaryTopic(),
                    response.getActiveStages(),
                    response.getPreviousCase(),
                    response.getActions()
            );
        }

        public void setAdditionalFields(List<AdditionalFieldDto> additionalFieldDtos) {
            this.replaceAdditionalFields(additionalFieldDtos);
        }

        public void hideActiveStageInfo() {
            this.replaceActiveStages(new HashSet<>());
        }

    }
}
