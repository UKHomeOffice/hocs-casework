package uk.gov.digital.ho.hocs.casework.security.filters;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.TimelineItemDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimelineItemAuthFilterServiceTest {

    @Mock
    private UserPermissionsService userPermissionsService;

    private TimelineItemAuthFilterService timelineItemAuthFilterService;

    @Before
    public void setUp() throws Exception {
        timelineItemAuthFilterService = new TimelineItemAuthFilterService(userPermissionsService);
    }

    @Test(expected = SecurityExceptions.AuthFilterException.class)
    public void testShouldThrowExceptionIfFilterNotForObjectType() {

        // GIVEN
        GetCaseSummaryResponse testIncorrectResponse = GetCaseSummaryResponse.from(
                new CaseSummary(
                        null, null, null,
                        null, null, null,
                        null, null, null,
                        null, null, null, null)
        );
        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
                List.of(testIncorrectResponse));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        // WHEN
        timelineItemAuthFilterService.applyFilter(responseToFilter, userAccessLevel, new Object[]{testIncorrectResponse});

        // THEN - Expect Exception
    }

    @Test
    public void testShouldFilterOutNotesNotAuthoredByRestrictedUser() {

        // GIVEN
        UUID testUserUUID = UUID.randomUUID();
        TimelineItemDto note1 = TimelineItemDto.from(new TimelineItem(null, null, LocalDateTime.now(), UUID.randomUUID().toString(), null, "Message 1", null,null, null));
        TimelineItemDto note2 = TimelineItemDto.from(new TimelineItem(null, null, LocalDateTime.now(), testUserUUID.toString(), null, "Message 2", null,null, null));
        TimelineItemDto note3 = TimelineItemDto.from(new TimelineItem(null, null, LocalDateTime.now(), UUID.randomUUID().toString(), null, "Message 3", null,null, null));
        TimelineItemDto note4 = TimelineItemDto.from(new TimelineItem(null, null, LocalDateTime.now(), testUserUUID.toString(), null, "Message 4", null,null, null));

        TimelineItemDto[] collectionOfNotes = Arrays.array(note1, note2, note3, note4);
        ResponseEntity<?> responseToFilter = ResponseEntity.ok(collectionOfNotes);

        AccessLevel accessLevel = AccessLevel.RESTRICTED_OWNER;

        when(userPermissionsService.getUserId()).thenReturn(testUserUUID);

        // WHEN
        Object result = timelineItemAuthFilterService.applyFilter(responseToFilter, accessLevel, collectionOfNotes);

        // THEN
        assertThat(result).isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultAsResponseEnt =  (ResponseEntity<?>) result;
        assertThat(resultAsResponseEnt.getBody()).isNotNull();

        assertThat(resultAsResponseEnt.getBody()).isInstanceOf(Collection.class);
        List<?> resultAsCollection =  (List<?>) resultAsResponseEnt.getBody();

        assertThat(resultAsCollection.size()).isEqualTo(2);
        assertThat(resultAsCollection.toArray()[0]).isExactlyInstanceOf(TimelineItemDto.class);

        TimelineItemDto[] resultAsArray = resultAsCollection.toArray(TimelineItemDto[]::new);
        assertThat(resultAsArray).contains(note2, note4);
    }

}