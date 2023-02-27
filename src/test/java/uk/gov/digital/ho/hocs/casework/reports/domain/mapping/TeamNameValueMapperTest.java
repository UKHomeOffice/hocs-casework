package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeamNameValueMapperTest {

    @Mock
    private InfoClient infoClient;

    private static final UUID TEAM_ID_IN_CACHE = UUID.fromString("e11f798a-8502-4419-90f9-98b17d9c4cdd");
    private static final UUID TEAM_ID_NOT_CACHED = UUID.fromString("de3e495e-606a-4aba-917c-f8c4ab8e19e7");
    private static final UUID TEAM_ID_MISSING = UUID.fromString("43aec912-61ed-41d1-b13c-f926b4a04301");

    private static final TeamDto TEST_TEAM_IN_CACHE = new TeamDto(
        "In Cache",
        TEAM_ID_IN_CACHE,
        true,
        Collections.emptySet()
    );

    private static final TeamDto TEST_TEAM_NOT_CACHED = new TeamDto(
        "Not from Cache",
        TEAM_ID_NOT_CACHED,
        true,
        Collections.emptySet()
    );

    @Test
    public void whenMappingAKeyInTheCache_theCachedTeamIsReturned() {
        givenInfoServiceTeams();

        TeamNameValueMapper underTest = new TeamNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(TEAM_ID_IN_CACHE);

        assertThat(mappedName).isEqualTo(Optional.of("In Cache"));
        verify(infoClient, times(0)).getTeamByUUID(any());
    }

    @Test
    public void whenMappingAnUncachedKey_theTeamIsLoadedFromInfoService() {
        givenInfoServiceTeams();
        when(infoClient.getTeamByUUID(TEAM_ID_NOT_CACHED)).thenReturn(TEST_TEAM_NOT_CACHED);

        TeamNameValueMapper underTest = new TeamNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(TEAM_ID_NOT_CACHED);

        assertThat(mappedName).isEqualTo(Optional.of("Not from Cache"));
    }

    @Test
    public void whenMappingAnMissingKey_thenEmptyIsReturned() {
        givenInfoServiceTeams();
        when(infoClient.getTeamByUUID(TEAM_ID_MISSING)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        TeamNameValueMapper underTest = new TeamNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(TEAM_ID_MISSING);

        assertThat(mappedName).isEqualTo(Optional.empty());
        verify(infoClient).getTeamByUUID(TEAM_ID_MISSING);
    }

    private void givenInfoServiceTeams() {
        when(infoClient.getTeams()).thenReturn(Set.of(TEST_TEAM_IN_CACHE));
    }

}
