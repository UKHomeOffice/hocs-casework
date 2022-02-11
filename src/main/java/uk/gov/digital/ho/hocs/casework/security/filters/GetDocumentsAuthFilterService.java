package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;

@Slf4j
@Service
public class GetDocumentsAuthFilterService implements AuthFilter {

    @Override
    public String getKey() {
        return GetDocumentsResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt) throws Exception {
        return responseEntityToFilter;
    }
}
