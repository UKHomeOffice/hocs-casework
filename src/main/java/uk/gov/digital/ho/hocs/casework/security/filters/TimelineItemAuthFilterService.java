package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.TimelineItemDto;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUTH_FILTER_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

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

        List<TimelineItemDto> currentTimelineDtos = verifyAndReturnAsObjectCollectionType(collectionAsArray, TimelineItemDto.class);

        String userId = userPermissionsService.getUserId().toString();
        log.debug("Filtering response by userId {} for Timeline events.", userId);

        List<TimelineItemDto> returnableDtos = currentTimelineDtos.stream()
                .filter(dto -> dto.getUserName().equals(userId))
                .collect(Collectors.toList());

        log.info("Issuing filtered Timeline events for userId: {}", userId, value(EVENT, AUTH_FILTER_SUCCESS));
        return new ResponseEntity<>(returnableDtos, responseEntityToFilter.getStatusCode());
    }


}
