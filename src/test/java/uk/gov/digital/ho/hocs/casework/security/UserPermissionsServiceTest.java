package uk.gov.digital.ho.hocs.casework.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionsServiceTest {

    @Mock
    private RequestData requestData;

    @Mock
    InfoClient infoClient;

    private UserPermissionsService service;

    private String uuid1 = Base64UUID.UUIDToBase64String(UUID.fromString("1325fe16-b864-42c7-85c2-7cab2863fe01"));
    private String uuid2 = Base64UUID.UUIDToBase64String(UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"));
    private String uuid3 = Base64UUID.UUIDToBase64String(UUID.fromString("1c1e2f17-d5d9-4ff6-a023-6c40d76e1e9d"));


    @Before
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        setupInfoClientMocks();
    }


    @Test
    public void shouldParseValidUserGroups() {

        String[] groups =
                ("/" + uuid2 + "," +
                        "/" + uuid3 + "," +
                        "/" +  uuid1).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        assertThat(service.getUserPermission().size()).isEqualTo(3);
    }


    @Test
    public void shouldIgnoreInvalidUserGroups() {

        String[] groups =
                ("/" + uuid2 + "," +
                        "INVALID_UUID," +
                        "/" + uuid3 + ","
                       ).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        assertThat(service.getUserPermission().size()).isEqualTo(2);

    }


    @Test
    public void shouldGetTeamsForUser() {
        String[] groups =
                ("/" + uuid3 + "," +
                        "/" + uuid1).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        Set<UUID> teams = service.getUserTeams();
        assertThat(teams).size().isEqualTo(2);
        assertThat(teams).contains(UUID.fromString("1c1e2f17-d5d9-4ff6-a023-6c40d76e1e9d"));
        assertThat(teams).doesNotContain(UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"));
        assertThat(teams).contains(UUID.fromString("1325fe16-b864-42c7-85c2-7cab2863fe01"));
    }

    @Test
    public void shouldGetCaseTypesForUser() {
        String[] groups =
                ("/" + uuid2 + "," +
                        "/" + uuid3 + "," +
                        "/" + uuid1).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        Set<String> caseTypes = service.getUserCaseTypes();
        assertThat(caseTypes.stream().anyMatch(c -> c.equals("TRO"))).isTrue();
        assertThat(caseTypes.stream().anyMatch(c -> c.equals("MIN"))).isTrue();
    }

    private void setupInfoClientMocks() {

        Set<Team> teams = new HashSet<>();
        Set<Permission> permissions1 = new HashSet<>();
        permissions1.add(new Permission("MIN", AccessLevel.READ));
        permissions1.add(new Permission("MIN", AccessLevel.OWNER));
        Team team1 = new Team("TEAM 1", UUID.fromString("1325fe16-b864-42c7-85c2-7cab2863fe01"), true, permissions1);
        teams.add(team1);

        Set<Permission> permissions2 = new HashSet<>();
        permissions2.add(new Permission("MIN", AccessLevel.READ));
        permissions2.add(new Permission("TRO",   AccessLevel.OWNER));
        Team team2 = new Team("TEAM 2", UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"), true, permissions2);
        teams.add(team2);

        Team team3 = new Team("TEAM 3", UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"), true, new HashSet<>());
        teams.add(team3);

        when(infoClient.getTeams()).thenReturn(teams);
    }
}