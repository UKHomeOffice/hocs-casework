package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.DeadlineDataDto;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDeadlinesRequest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeadlineDataResourceTest {

    @Mock
    private DeadlineDataService deadlineDataService;

    private DeadlineDataResource deadlineDataResource;

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        deadlineDataResource = new DeadlineDataResource(deadlineDataService);
    }

    @Test
    public void shouldStoreDeadlines() {

        DeadlineDataDto deadlineDataDto = new DeadlineDataDto("Test",LocalDate.now());
        Set<DeadlineDataDto> deadlines = new HashSet<>();
        deadlines.add(deadlineDataDto);
        UpdateDeadlinesRequest updateDeadlinesRequest = new UpdateDeadlinesRequest(deadlines);

        ResponseEntity response = deadlineDataResource.storeDeadlines(updateDeadlinesRequest, uuid);

        verify(deadlineDataService, times(1)).updateDeadlines(uuid, deadlines);

        verifyNoMoreInteractions(deadlineDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}