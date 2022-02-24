package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActiveStageDto;
import uk.gov.digital.ho.hocs.casework.api.dto.AdditionalFieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.value;

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

        if (responseEntityToFilter.getBody().getClass() != GetCaseSummaryResponse.class) {
            String msg = String.format("The wrong filter has been selected for class %s", responseEntityToFilter.getBody().getClass().getSimpleName());
            log.error(msg, value(LogEvent.EXCEPTION, LogEvent.AUTH_FILTER_FAILURE));
            throw new SecurityExceptions.AuthFilterException(msg, LogEvent.AUTH_FILTER_FAILURE);
        }

        GetCaseSummaryResponse getCaseSummaryResponse  = (GetCaseSummaryResponse) responseEntityToFilter.getBody();

        if (getCaseSummaryResponse == null  || getCaseSummaryResponse.getAdditionalFields() == null) {
            return responseEntityToFilter;
        }

        log.debug("Filtering GetCaseSummaryResponse");

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
