package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.AdditionalFieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt, Object[] collectionAsArray) throws Exception {

        // collectionsAsArray - not used
        // userUUID - not used

        if (responseEntityToFilter.getBody() != null && responseEntityToFilter.getBody().getClass() != GetCaseSummaryResponse.class) {
            throw new Exception("There is something wrong with the GetCaseSummaryResponse Auth Filter");
        }

        GetCaseSummaryResponse getCaseSummaryResponse  = (GetCaseSummaryResponse) responseEntityToFilter.getBody();

        log.debug("Filtering GetCaseSummaryResponse");

        Map<FieldDto, String> restrictedFields = new HashMap<>();
        userPermissionsService.getRestrictedFieldNames()
                .forEach(fieldDto -> restrictedFields.put(fieldDto, fieldDto.getName()));

        if (getCaseSummaryResponse == null  || getCaseSummaryResponse.getAdditionalFields() == null) {
            return responseEntityToFilter;
        }

        Map<String, AdditionalFieldDto> additionalFieldDtoStringMap = new HashMap<>();
        getCaseSummaryResponse.getAdditionalFields()
                .forEach(additionalFieldDto -> additionalFieldDtoStringMap.put(additionalFieldDto.getName(),additionalFieldDto));


        List<AdditionalFieldDto> replacementList = new ArrayList<>();
        restrictedFields.forEach((FieldDto key, String val) -> {
            if (userAccessLevelAsInt == key.getAccessLevel().getLevel() && additionalFieldDtoStringMap.containsKey(val)) {
                replacementList.add(additionalFieldDtoStringMap.get(val));
            }
        });

        if (replacementList.isEmpty()) {
            return responseEntityToFilter;
        }

        // todo: filter out Action data as well?

        replacementList.sort(Comparator.comparing(AdditionalFieldDto::getLabel));

        SettableAdditionalFieldsGetCaseSummaryResponse response = new SettableAdditionalFieldsGetCaseSummaryResponse(getCaseSummaryResponse);
        response.setAdditionalFields(replacementList);

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

    }
}
