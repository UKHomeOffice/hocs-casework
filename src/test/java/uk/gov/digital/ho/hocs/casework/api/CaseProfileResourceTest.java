package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.ProfileDto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseProfileResourceTest {

    private CaseProfileResource caseProfileResource;

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private InfoClient infoClient;

    @Before
    public void before(){
        caseProfileResource = new CaseProfileResource(caseDataService, infoClient);
    }

    @Test
    public void getProfileForCase(){

        UUID testUUID = UUID.randomUUID();
        String caseType = "caseTypeA";
        ProfileDto profileDto = new ProfileDto("profileName1", false, null);

        when(caseDataService.getCaseType(testUUID)).thenReturn(caseType);
        when(infoClient.getProfileByCaseType(caseType)).thenReturn(profileDto);

        ResponseEntity<ProfileDto> response = caseProfileResource.getProfileForCase(testUUID);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProfileName()).isEqualTo("profileName1");
        assertThat(response.getBody().isSummaryDeadlineEnabled()).isFalse();
        assertThat(response.getBody().getSearchFields()).isNull();

        verify(caseDataService).getCaseType(testUUID);
        verify(infoClient).getProfileByCaseType(caseType);
        verifyNoMoreInteractions(caseDataService, infoClient);

    }
}
