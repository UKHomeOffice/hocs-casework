package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;

import java.util.UUID;

@Slf4j
@Service
public class GetDocumentsAuthFilterService implements AuthFilter {

    @Override
    public String getKey() {
        return GetDocumentsResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt, UUID userUUID, Object[] collectionAsArray) throws Exception {
        return null;
    }

}
