package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.RestrictedFieldService;

import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;

@Slf4j
@Service
public class GetCaseSummaryAuthFilterService implements AuthFilter {

    private final RestrictedFieldService restrictedFieldService;

    @Autowired
    public GetCaseSummaryAuthFilterService(RestrictedFieldService restrictedFieldService) {
        this.restrictedFieldService = restrictedFieldService;
    }

    @Override
    public String getKey() {
        return GetCaseSummaryResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter,
                              AccessLevel userAccessLevel,
                              Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {
        // todo: expand to filter out Case Action data at later date. See HOCS-4596, not applicable for initial use case SMC caseType

        GetCaseSummaryResponse response = verifyAndReturnAsObjectType(responseEntityToFilter,
            GetCaseSummaryResponse.class);

        if (response==null || response.getAdditionalFields()==null) {
            return responseEntityToFilter;
        }

        restrictedFieldService.removeRestrictedFieldsFromAdditionalFields(response.getType(), userAccessLevel,
            response.getAdditionalFields());
        response.getActiveStages().clear();
        response.getActions().getCaseActionData().clear();

        return new ResponseEntity<>(response, responseEntityToFilter.getStatusCode());
    }

}
