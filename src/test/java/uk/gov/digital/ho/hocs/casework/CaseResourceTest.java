package uk.gov.digital.ho.hocs.casework;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.Silent.class)
public class CaseResourceTest {

    private static final CaseDetails CASE_DETAILS = new CaseDetails();

    @Mock
    private CaseService mockService;

    private CaseResource resource;

    @Before
    public void setUp() {
        resource = new CaseResource(mockService);
    }

    @Test
    public void shouldCallCreateCaseOnce() {
        ResponseEntity httpResponse = resource.createCase(CASE_DETAILS);
        when(mockService.create(CASE_DETAILS)).thenReturn("TYPE/1000010/18");

        verify(mockService, times(1)).create(CASE_DETAILS);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnOKWhenAbleCreate()  {

        ResponseEntity httpResponse = resource.createCase(CASE_DETAILS);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockService).create(CASE_DETAILS);
    }
}