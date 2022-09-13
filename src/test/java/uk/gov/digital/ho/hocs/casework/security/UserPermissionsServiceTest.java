package uk.gov.digital.ho.hocs.casework.security;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionsServiceTest {

    @Mock
    private RequestData requestData;

    @Mock
    private InfoClient infoClient;

    private UserPermissionsService service;

    private static final List<String> teamBase64 = new ArrayList<>();

    private static final List<TeamDto> teamDtos = new ArrayList<>();

    @BeforeClass
    public static void setupData() {
        UUID teamUuid = UUID.randomUUID();
        teamBase64.add("/" + Base64UUID.uuidToBase64String(teamUuid));
        teamDtos.add(new TeamDto("Test", teamUuid, true, Set.of(new PermissionDto("TEST1", AccessLevel.CASE_ADMIN))));

        teamUuid = UUID.randomUUID();
        teamBase64.add("/" + Base64UUID.uuidToBase64String(teamUuid));
        teamDtos.add(new TeamDto("Test", teamUuid, true, Set.of(new PermissionDto("TEST1", AccessLevel.OWNER))));

        teamUuid = UUID.randomUUID();
        teamBase64.add("/" + Base64UUID.uuidToBase64String(teamUuid));
        teamDtos.add(new TeamDto("Test", teamUuid, true, Set.of(new PermissionDto("TEST2", AccessLevel.CASE_ADMIN))));
    }

    @Before
    public void setup() {
        when(infoClient.getTeams()).thenReturn(new HashSet<>(teamDtos));
    }

    @Test
    public void shouldParseValidUserGroups() {
        String[] groups = teamBase64.toArray(String[]::new);

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getUserTeams()).containsExactlyInAnyOrderElementsOf(
            teamDtos.stream().map(TeamDto::getUuid).collect(Collectors.toList()));
    }

    @Test
    public void shouldIgnoreInvalidUserGroups() {
        List<String> groups = new ArrayList<>(teamBase64);
        groups.set(2, "INVALID_UUID");

        when(requestData.groupsArray()).thenReturn(groups.toArray(String[]::new));
        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getUserTeams()).containsExactlyInAnyOrder(teamDtos.get(0).getUuid(),
            teamDtos.get(1).getUuid());
    }

    @Test
    public void shouldReturnUnsetAccessLevelWhenUserHasNoGroup() {
        when(requestData.groupsArray()).thenReturn(new String[0]);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getMaxAccessLevel("UNDEFINED")).isEqualTo(AccessLevel.UNSET);
    }

    @Test
    public void shouldReturnUnsetWhenNoPermissionsFoundForCaseType() {
        String[] groups = teamBase64.toArray(String[]::new);
        when(requestData.groupsArray()).thenReturn(groups);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getMaxAccessLevel("UNDEFINED")).isEqualTo(AccessLevel.UNSET);
    }

    @Test
    public void shouldReturnHighestWhenTwoGroupsHaveCaseType() {
        String[] groups = teamBase64.toArray(String[]::new);
        when(requestData.groupsArray()).thenReturn(groups);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getMaxAccessLevel("TEST1")).isEqualTo(AccessLevel.CASE_ADMIN);
    }

    @Test
    public void shouldGetAllCaseTypeTeamsIfCaseAdmin() {
        String[] groups = teamBase64.stream().limit(1).toArray(String[]::new);

        when(requestData.groupsArray()).thenReturn(groups);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getExpandedUserTeams()).containsExactlyInAnyOrderElementsOf(
            teamDtos.stream().limit(2).map(TeamDto::getUuid).collect(Collectors.toList()));
    }

    @Test
    public void shouldGetAllCaseTypeTeamsIfOnTeamCaseAdmin() {
        String[] groups = new String[] { teamBase64.get(0), teamBase64.get(2) };

        when(requestData.groupsArray()).thenReturn(groups);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getExpandedUserTeams()).containsExactlyInAnyOrderElementsOf(
            teamDtos.stream().map(TeamDto::getUuid).collect(Collectors.toList()));
    }

    @Test
    public void shouldGetCaseTypeWhenCaseAdmin() {
        String[] groups = teamBase64.stream().limit(1).toArray(String[]::new);

        when(requestData.groupsArray()).thenReturn(groups);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getCaseTypesIfUserTeamIsCaseTypeAdmin()).contains("TEST1");
    }

    @Test
    public void shouldGetCaseTypeWhenNotCaseAdmin() {
        String[] groups = teamBase64.stream().skip(1).limit(1).toArray(String[]::new);

        when(requestData.groupsArray()).thenReturn(groups);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.getCaseTypesIfUserTeamIsCaseTypeAdmin()).isEmpty();
    }

    @Test
    public void shouldReturnTrueIfUserCaseAdmin() {
        String[] groups = teamBase64.stream().limit(1).toArray(String[]::new);

        when(requestData.groupsArray()).thenReturn(groups);

        service = new UserPermissionsService(requestData, infoClient);

        assertThat(service.isUserInTeam(teamDtos.get(1).getUuid())).isTrue();
        assertThat(service.isUserInTeam(teamDtos.get(2).getUuid())).isFalse();
    }

}
