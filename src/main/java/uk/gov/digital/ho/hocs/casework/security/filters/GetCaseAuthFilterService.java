package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt) throws Exception {
        // todo: maybe look to instantiate the list once and then forget... likely that this will not change during a
        //  day regardless. However list is cached on the infoClient call.

        if (responseEntityToFilter.getBody().getClass() != GetCaseResponse.class) {
            throw new Exception("There is something wrong with the GetCaseResponse Auth Filter");
        }

        GetCaseResponse getCaseResponse  = (GetCaseResponse) responseEntityToFilter.getBody();
        Map<String, String> replacementCaseResponseDataMap = new HashMap<>();

        log.info("GetCaseResponse AccessLevel Filter activated.");

        // fixme 1: problem with the current value based Access Level hierarchy means that we can't cater very well for
        //  a role that needs some higher level capabilities and some lower level capabilities at the same time. Therefore
        //  this mechanism in a way violates the Authorisation principle. We should probably evaluate how well this
        //  performs and how flexible it is, then decide if it is worth investing time in developing
        //  a proper RBAC solution.
        // e.g. of the problem... a team with READ permissions, which is lower level than RESTRICTED_READ,
        // as RESTRICTED_READ requires ability to create cases, would still be able to view all data, whilst RESTRICTED_READ cannot.

        // fixme 2: So, because the case data is stored as String key, value pairs, with no reference to the field uuid,
        //  field level validation requires String matching on the fields. For most work streams this won't be a problem
        //  as the teams will not be given the RESTRICTED_READ level, however in this solution, care must be taken for
        //  naming of fields in relation to permissions.

        List<FieldDto> restrictedFields = userPermissionsService.getRestrictedFieldNames();
        restrictedFields.forEach((FieldDto restrictedField) -> {
            if (userAccessLevelAsInt == restrictedField.getAccessLevel().getLevel() && getCaseResponse.getData().containsKey(restrictedField.getName())) {
                replacementCaseResponseDataMap.put(
                        restrictedField.getName(),
                        ((GetCaseResponse) responseEntityToFilter.getBody()).getData().get(restrictedField.getName())
                );
            }
        });

        if (replacementCaseResponseDataMap.isEmpty()) {
            return responseEntityToFilter;
        }

        // fixme: not sure this is the best way, but don't want to influence the top GetCaseResponse class too much,
        //  but need the protected method on it to have the extended class capable of updating the map independently.
        //  That might need to be extended to include other fields than the data field only. Possibly rework into a specific method

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
