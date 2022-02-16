package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.TimelineItemDto;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt, Object[] collectionAsArray) throws Exception {
        // fixme: CaseNotes all have a "Type" this could be used to allow users to view "more" if necessary.
        //  I would however prefer to then set up a database table of case note types that we currently do not have,
        //  this way we could attach permissions to the note type.



        Set<TimelineItemDto> returnableDtos = new HashSet<>();
        if (userAccessLevelAsInt != AccessLevel.RESTRICTED_READ.getLevel() || collectionAsArray.length < 1) {
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
