package uk.gov.digital.ho.hocs.casework.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UserClientTest {

    private final String USER_SERVICE_BASE="http://localhost:1234";

    private UserClient client;

    @Mock
    RestHelper restHelper;

    @Before
    public void setUp() {
        client=new UserClient(restHelper,USER_SERVICE_BASE);
    }

    @Test
    public void shouldCallUserService() {
        when(restHelper.get(USER_SERVICE_BASE,
                "/user/userId/MIN/READ",UserPermissionResponse.class))
                .thenReturn(okResponse());

        boolean response = client.getUserAccess("userId", CaseType.MIN, "READ");
        assertThat(response).isTrue();

    }

    private ResponseEntity okResponse() {
        return new ResponseEntity<>(new UserPermissionResponse("userId", true), HttpStatus.OK);
    }


}