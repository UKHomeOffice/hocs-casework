package uk.gov.digital.ho.hocs.casework.security.filters;

import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface AuthFilter {

    String getKey();

    Object applyFilter(ResponseEntity<?> responseEntityToFilter,
                       AccessLevel userAccessLevel,
                       Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException;

    default <T> T verifyAndReturnAsObjectType(ResponseEntity<?> responseEntityToFilter, Class<T> expectedObjectType) {
        Object obj = responseEntityToFilter.getBody();

        if (obj==null) {
            return null;
        }

        if (obj.getClass()!=expectedObjectType) {
            String msg = String.format("The wrong filter has been selected for class %s",
                obj.getClass().getSimpleName());
            throw new SecurityExceptions.AuthFilterException(msg, LogEvent.AUTH_FILTER_FAILURE);
        }
        return expectedObjectType.cast(obj);
    }

    default <T> List<T> verifyAndReturnAsObjectCollectionType(Object[] objectCollection, Class<T> expectedObjectType) {
        if (objectCollection==null || objectCollection.length < 1) {
            return new ArrayList<>();
        }

        Object obj = objectCollection[0];

        if (obj.getClass()!=expectedObjectType) {
            String msg = String.format("The wrong filter has been selected for array element class %s",
                obj.getClass().getSimpleName());
            throw new SecurityExceptions.AuthFilterException(msg, LogEvent.AUTH_FILTER_FAILURE);
        }
        return Arrays.stream(objectCollection).map(expectedObjectType::cast).collect(Collectors.toList());
    }

}
