package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserNameValueMapperTest {

    @Mock
    private InfoClient infoClient;

    private static final UUID USER_ID_ALL_DETAILS = UUID.fromString("b8a9d140-cda2-45d0-897c-7473ffde6ab8");
    private static final UUID USER_ID_ONLY_USERNAME = UUID.fromString("ee031ec8-b7e5-448f-bbe1-503d3f3d35e1");
    private static final UUID USER_ID_NOT_CACHED = UUID.fromString("57989139-5451-4aee-9815-319136bfa5a1");
    private static final UUID USER_ID_MISSING = UUID.fromString("050c2079-1efd-4860-951b-1f377666b59f");

    private static final UserDto TEST_USER_ALL_DETAILS = new UserDto(
        USER_ID_ALL_DETAILS.toString(),
        "username_full",
        "Firstname",
        "Surname",
        "email@example.org"
    );

    private static final UserDto TEST_USER_ONLY_USERNAME = new UserDto(
        USER_ID_ONLY_USERNAME.toString(),
        "username_only",
        null,
        null,
        null
    );

    private static final UserDto TEST_USER_NOT_CACHED = new UserDto(
        USER_ID_NOT_CACHED.toString(),
        "username_new",
        "New",
        "User",
        null
    );

    @Test
    public void whenMappingAKeyInTheCache_theCachedUserIsReturned() {
        givenInfoServiceUsers();

        UserNameValueMapper underTest = new UserNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(USER_ID_ALL_DETAILS);

        assertThat(mappedName).isEqualTo(Optional.of("Firstname Surname"));
        verify(infoClient, times(0)).getUser(any());
    }

    @Test
    public void whenMappingAUserWithoutADisplayName_theUsernameIsUsed() {
        givenInfoServiceUsers();

        UserNameValueMapper underTest = new UserNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(USER_ID_ONLY_USERNAME);

        assertThat(mappedName).isEqualTo(Optional.of("username_only"));
    }

    @Test
    public void whenMappingAnUncachedKey_theUserIsLoadedFromInfoService() {
        givenInfoServiceUsers();
        when(infoClient.getUser(USER_ID_NOT_CACHED)).thenReturn(TEST_USER_NOT_CACHED);

        UserNameValueMapper underTest = new UserNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(USER_ID_NOT_CACHED);

        assertThat(mappedName).isEqualTo(Optional.of("New User"));
    }

    @Test
    public void whenMappingAnMissingKey_thenEmptyIsReturned() {
        givenInfoServiceUsers();
        when(infoClient.getUser(USER_ID_MISSING)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserNameValueMapper underTest = new UserNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(USER_ID_MISSING);

        assertThat(mappedName).isEqualTo(Optional.of(USER_ID_MISSING.toString()));
        verify(infoClient).getUser(USER_ID_MISSING);
    }

    private void givenInfoServiceUsers() {
        when(infoClient.getAllUsers()).thenReturn(Set.of(TEST_USER_ALL_DETAILS, TEST_USER_ONLY_USERNAME));
    }

}
