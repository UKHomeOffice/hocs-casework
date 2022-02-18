package uk.gov.digital.ho.hocs.casework.security.filters;

import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;

import java.util.UUID;

public interface AuthFilter {

    String getKey();

    Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException;

}
