package uk.gov.digital.ho.hocs.casework.security.filters;

import org.springframework.http.ResponseEntity;

public interface AuthFilter {

    String getKey();

    Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt) throws Exception;
}
