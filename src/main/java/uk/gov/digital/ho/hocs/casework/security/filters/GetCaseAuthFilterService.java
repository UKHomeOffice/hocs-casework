package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUTH_FILTER_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Slf4j
@Service
public class GetCaseAuthFilterService implements AuthFilter {

    private final UserPermissionsService userPermissionsService;

    @Autowired
    public GetCaseAuthFilterService(UserPermissionsService userPermissionsService) {
        this.userPermissionsService = userPermissionsService;
    }

    @Override
    public String getKey() {
        return GetCaseResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {

        GetCaseResponse getCaseResponse = verifyAndReturnAsObjectType(responseEntityToFilter,GetCaseResponse.class);

        if (getCaseResponse == null  || getCaseResponse.getType() == null) {
            return responseEntityToFilter;
        }

        log.debug("Filtering GetCaseResponse for request from userId: {}", userPermissionsService.getUserId());

        List<FieldDto> permittedFields = userPermissionsService.getFieldsByCaseTypeAndPermissionLevel(getCaseResponse.getType(), userAccessLevel);

        Map<String, String> replacementCaseResponseDataMap = new HashMap<>();
        permittedFields.forEach((FieldDto restrictedField) -> {
            if (getCaseResponse.getData().containsKey(restrictedField.getName())) {
                replacementCaseResponseDataMap.put(
                        restrictedField.getName(),
                        getCaseResponse.getData().get(restrictedField.getName())
                );
            }
        });

        SettableDataMapGetCaseResponse replacementCaseResponse = new SettableDataMapGetCaseResponse(getCaseResponse);
        replacementCaseResponse.setDataMap(replacementCaseResponseDataMap);

        log.info("Issuing filtered GetCaseResponse for userId: {}", userPermissionsService.getUserId(), value(EVENT, AUTH_FILTER_SUCCESS));
        return new ResponseEntity<GetCaseResponse>(replacementCaseResponse, responseEntityToFilter.getStatusCode());
    }

    public static class SettableDataMapGetCaseResponse extends GetCaseResponse {

        public SettableDataMapGetCaseResponse(GetCaseResponse response) {
            super(
                    response.getUuid(),
                    response.getCreated(),
                    response.getType(),
                    response.getReference(),
                    response.getData(),
                    response.getPrimaryTopicUUID(),
                    response.getPrimaryTopic(),
                    response.getPrimaryCorrespondentUUID(),
                    response.getPrimaryCorrespondent(),
                    response.getCaseDeadline(),
                    response.getCaseDeadlineWarning(),
                    response.getDateReceived(),
                    response.getStages(),
                    response.getCompleted()
            );
        }

        public void setDataMap(Map<String, String> dataMap) {
            this.replaceDataMap(dataMap);
        }
    }
}
