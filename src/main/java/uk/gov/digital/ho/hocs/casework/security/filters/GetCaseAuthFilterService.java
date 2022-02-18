package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.value;

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

        if (responseEntityToFilter.getBody().getClass() != GetCaseResponse.class) {
            String msg = String.format("The wrong filter has been selected for class %s", responseEntityToFilter.getBody().getClass().getSimpleName());
            log.error(msg, value(LogEvent.EXCEPTION, LogEvent.AUTH_FILTER_FAILURE));
            throw new SecurityExceptions.AuthFilterException(msg, LogEvent.AUTH_FILTER_FAILURE);
        }

        GetCaseResponse getCaseResponse  = (GetCaseResponse) responseEntityToFilter.getBody();
        Map<String, String> replacementCaseResponseDataMap = new HashMap<>();

        List<FieldDto> restrictedFields = userPermissionsService.getFieldsByPermissionLevel(userAccessLevel);
        restrictedFields.forEach((FieldDto restrictedField) -> {
            if (getCaseResponse.getData().containsKey(restrictedField.getName())) {
                replacementCaseResponseDataMap.put(
                        restrictedField.getName(),
                        ((GetCaseResponse) responseEntityToFilter.getBody()).getData().get(restrictedField.getName())
                );
            }
        });

        if (replacementCaseResponseDataMap.isEmpty()) {
            return responseEntityToFilter;
        }

        SettableDataMapGetCaseResponse replacementCaseResponse = new SettableDataMapGetCaseResponse((GetCaseResponse) responseEntityToFilter.getBody());
        replacementCaseResponse.setDataMap(replacementCaseResponseDataMap);

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
