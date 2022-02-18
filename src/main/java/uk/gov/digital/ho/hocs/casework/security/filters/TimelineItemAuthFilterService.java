package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.TimelineItemDto;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashSet;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
public class TimelineItemAuthFilterService implements AuthFilter {

    private final UserPermissionsService userPermissionsService;

    @Autowired
    public TimelineItemAuthFilterService(UserPermissionsService userPermissionsService) {
        this.userPermissionsService = userPermissionsService;
    }



    @Override
    public String getKey() {
        return TimelineItemDto.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {
        // fixme: CaseNotes all have a "Type" this could be used to allow users to view "more" if necessary.
        //  I would however prefer to then set up a database table of case note types that we currently do not have,
        //  this way we could attach permissions to the note type.

        if (collectionAsArray != null && collectionAsArray[0].getClass() != TimelineItemDto.class) {
            String msg = String.format("The wrong filter has been selected for class %s", collectionAsArray[0].getClass().getClass().getSimpleName());
            log.error(msg, value(LogEvent.EXCEPTION, LogEvent.AUTH_FILTER_FAILURE));
            throw new SecurityExceptions.AuthFilterException(msg, LogEvent.AUTH_FILTER_FAILURE);
        }

        Set<TimelineItemDto> returnableDtos = new HashSet<>();
        if (userAccessLevel != AccessLevel.RESTRICTED_OWNER || collectionAsArray.length < 1) {
            return responseEntityToFilter;
        }

        for (Object o : collectionAsArray) {
            TimelineItemDto dto = (TimelineItemDto) o;
            if (dto.getUserName().equals(userPermissionsService.getUserId().toString())) {
                returnableDtos.add(dto);
            }
        }

        return new ResponseEntity<>(returnableDtos, responseEntityToFilter.getStatusCode());
    }


}
