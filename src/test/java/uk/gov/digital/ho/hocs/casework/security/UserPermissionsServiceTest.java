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
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionsServiceTest {

    @Mock
    private RequestData requestData;


    private UserPermissionsService service;

    @Before
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }


    @Test
    public void shouldParseValidUserGroupList() {
        String groups =
                "/DCU/team1/TRO/READ," +
                        "/DCU/team2/TRO/WRITE," +
                        "/DCU/team3/MIN/WRITE," +
                        "/ABC/team1/MIN/WRITE," +
                        "/ABC/team2/MIN/OWNER";

        when(requestData.groups()).thenReturn(groups);
        service = new UserPermissionsService(requestData);
        Map<String, Map<String, Map<CaseType,Set<AccessLevel>>>> permissions = service.getUserPermission();
        assertThat(permissions.size()).isEqualTo(2);
    }


    @Test
    public void shouldIgnoreInvalidUserGroupItems() {
        String groups =
                "/DCU/team1/TRO/READ," +
                        "/DCU/team2/TRO/," +
                        "/DCU/team3," +
                        "/ABC/," +
                        "/ABC/team2/MIN/OWNER";

        when(requestData.groups()).thenReturn(groups);
        service = new UserPermissionsService(requestData);
        Map<String, Map<String, Map<CaseType,Set<AccessLevel>>>> permissions = service.getUserPermission();
        assertThat(permissions.size()).isEqualTo(2);
    }



    @Test
    public void shouldGetPermissionsForCaseType() {
        String groups =
                "/DCU/team1/TRO/READ," +
                        "/DCU/team2/TRO/WRITE," +
                        "/ABC/team1/MIN/WRITE," +
                        "/ABC/team2/MIN/OWNER";

        when(requestData.groups()).thenReturn(groups);
        service = new UserPermissionsService(requestData);
        Set<AccessLevel> userAccessLevels = service.getUserAccessLevels(CaseType.MIN);
        assertThat(userAccessLevels.size()).isEqualTo(2);
        assertThat(userAccessLevels).contains(AccessLevel.WRITE);
        assertThat(userAccessLevels).contains(AccessLevel.OWNER);

    }

    @Test
    public void shouldGetUnitsForUser() {
        String groups =
                "/DCU/team1/TRO/READ," +
                        "/DCU/team2/TRO/WRITE," +
                        "/DCU/team3/MIN/WRITE," +
                        "/ABC/team1/MIN/WRITE," +
                        "/ABC/team2/MIN/OWNER";

        when(requestData.groups()).thenReturn(groups);
        service = new UserPermissionsService(requestData);
        Set<String> units = service.getUserUnits();
        assertThat(units).contains("DCU");
        assertThat(units).contains("ABC");

    }

    @Test
    public void shouldGetTeamsForUser() {
        String groups =
                "/DCU/team1/TRO/READ," +
                        "/DCU/team2/TRO/WRITE," +
                        "/DCU/team3/MIN/WRITE," +
                        "/ABC/team1/MIN/WRITE," +
                        "/ABC/team2/MIN/OWNER";

        when(requestData.groups()).thenReturn(groups);
        service = new UserPermissionsService(requestData);
        Set<String> teams = service.getUserTeams();
        assertThat(teams).contains("team1");
        assertThat(teams).contains("team2");
        assertThat(teams).contains("team3");
    }

    @Test
    public void shouldGetCaseTypesForUser() {
        String groups =
                "/DCU/team1/TRO/READ," +
                        "/DCU/team2/TRO/WRITE," +
                        "/DCU/team3/MIN/WRITE," +
                        "/ABC/team1/MIN/WRITE," +
                        "/ABC/team2/MIN/OWNER";

        when(requestData.groups()).thenReturn(groups);
        service = new UserPermissionsService(requestData);
        Set<CaseType> caseTypes = service.getUserCaseTypes();
        assertThat(caseTypes).contains(CaseType.TRO);
        assertThat(caseTypes).contains(CaseType.MIN);
    }
}