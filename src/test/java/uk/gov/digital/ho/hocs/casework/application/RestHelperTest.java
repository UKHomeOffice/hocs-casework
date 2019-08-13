package uk.gov.digital.ho.hocs.casework.application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RestHelperTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RequestData requestData;
    private String basicAuth = "auth";

    private RestHelper restHelper;

    @Before
    public void setup() {
        restHelper = new RestHelper(restTemplate, basicAuth, requestData);
    }

    @Test
    public void getFile(){
        String root = "localhost:8080";
        String url = "/getFile";
        String fullUrl = root + url;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.parse("attachment;filename=dock1.doc"));
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ResponseEntity<ByteArrayResource> response = mock(ResponseEntity.class);
        when(response.getBody()).thenReturn(new ByteArrayResource(new byte[10]));
        when(response.getHeaders()).thenReturn(httpHeaders);
        when(restTemplate.exchange(eq(fullUrl), eq(HttpMethod.GET), any(), eq(ByteArrayResource.class))).thenReturn(response);

        S3Document document = restHelper.getFile(root, url);

        verify(response).getBody();
        verify(response, times(3)).getHeaders();
        verify(restTemplate).exchange(eq(fullUrl), eq(HttpMethod.GET), any(), eq(ByteArrayResource.class));
        verifyNoMoreInteractions(response, restTemplate);
        assertThat(document).isNotNull();
        assertThat(document.getFilename()).isEqualTo("dock1.doc");
        assertThat(document.getOriginalFilename()).isEqualTo("dock1.doc");
        assertThat(document.getFileType()).isEqualTo("doc");
        assertThat(document.getMimeType()).isEqualTo("application/octet-stream");
        assertThat(document.getData().length).isEqualTo(10);


    }

}
