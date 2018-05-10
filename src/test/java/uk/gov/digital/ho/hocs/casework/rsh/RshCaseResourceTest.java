package uk.gov.digital.ho.hocs.casework.rsh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.Silent.class)
public class RshCaseResourceTest {

    private static final RshCaseDetails CASE_DETAILS = new RshCaseDetails();

    @Mock
    private RshCaseService mockService;

    private RshCaseResource resource;

    @Before
    public void setUp() {
        resource = new RshCaseResource(mockService);
    }

    @Test
    public void shouldCallCreateCaseOnce() {
        ResponseEntity httpResponse = resource.rshCreateCase(CASE_DETAILS);
        when(mockService.createRSHCase("type",CASE_DETAILS)).thenReturn("TYPE/1000010/18");

        verify(mockService, times(1)).createRSHCase("type",CASE_DETAILS);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnOKWhenAbleCreate()  {

        ResponseEntity httpResponse = resource.rshCreateCase(CASE_DETAILS);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockService).createRSHCase("type",CASE_DETAILS);
    }
}