package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.RestrictedFieldService;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;

@Slf4j
@Service
public class GetCaseAuthFilterService implements AuthFilter {

    private final RestrictedFieldService restrictedFieldService;

    @Autowired
    public GetCaseAuthFilterService(RestrictedFieldService restrictedFieldService) {
        this.restrictedFieldService = restrictedFieldService;
    }

    @Override
    public String getKey() {
        return GetCaseResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {
        GetCaseResponse response = verifyAndReturnAsObjectType(responseEntityToFilter, GetCaseResponse.class);

        if (response == null  || response.getType() == null) {
            return responseEntityToFilter;
        }

        restrictedFieldService
                .removeRestrictedFieldsFromCaseData(response.getType(), userAccessLevel, response.getData());

        return new ResponseEntity<>(response, responseEntityToFilter.getStatusCode());
    }

}
