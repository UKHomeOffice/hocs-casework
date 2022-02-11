package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.TimelineItemDto;

@Slf4j
@Service
public class TimelineItemAuthFilterService implements AuthFilter {
    @Override
    public String getKey() {
        return TimelineItemDto.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt) throws Exception {
        return responseEntityToFilter;
    }
}
