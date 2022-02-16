package uk.gov.digital.ho.hocs.casework.security.filters;

import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface AuthFilter {

    String getKey();

    Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt, Object[] collectionAsArray) throws Exception;

}
