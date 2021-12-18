package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.ProfileDto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseProfileResourceTest {

    private CaseProfileResource caseProfileResource;

    @Mock
    private CaseDataTypeService caseDataTypeService;
    @Mock
    private InfoClient infoClient;

    @Before
    public void before(){
        caseProfileResource = new CaseProfileResource(caseDataTypeService, infoClient);
    }

    @Test
    public void getProfileForCase(){

        UUID testUUID = UUID.randomUUID();
        CaseDataType caseType = CaseDataTypeFactory.from("caseTypeA", "01");
        ProfileDto profileDto = new ProfileDto("profileName1", false, null);

        when(caseDataTypeService.getCaseDataType(testUUID)).thenReturn(caseType);
        when(infoClient.getProfileByCaseType(caseType.getDisplayCode())).thenReturn(profileDto);

        ResponseEntity<ProfileDto> response = caseProfileResource.getProfileForCase(testUUID);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProfileName()).isEqualTo("profileName1");
        assertThat(response.getBody().isSummaryDeadlineEnabled()).isFalse();
        assertThat(response.getBody().getSearchFields()).isNull();

        verify(caseDataTypeService).getCaseDataType(testUUID);
        verify(infoClient).getProfileByCaseType(caseType.getDisplayCode());
        verifyNoMoreInteractions(caseDataTypeService, infoClient);

    }
}
