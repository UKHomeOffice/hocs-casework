package uk.gov.digital.ho.hocs.casework.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.HocsCaseApplication;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsCaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CaseDataAuditResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;


    private UUID caseUUID = UUID.randomUUID();
    private UUID stageUUID = UUID.randomUUID();

    @Before
    public void setUp() throws Exception {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();
        HttpEntity<?> caseHttpEntity = new HttpEntity<Object>(body, requestHeaders);
        restTemplate.postForEntity(
                "/case",
                caseHttpEntity,
                CreateCaseResponse.class);
        Map<String, Object> stageBody = buildCreateStageBody();
        HttpEntity<?> stageHttpEntity = new HttpEntity<Object>(stageBody, requestHeaders);
        restTemplate.postForEntity(
                "/case/" + caseUUID + "/stage",
                stageHttpEntity,
                Void.class);

    }

    @Test
    public void getReportCurrent() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Map<String, String>> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/report/RSH/current",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("text/csv;charset=UTF-8"));
        assertThat(responseEntity.getBody().toString()).contains("Case_Type", "RSH");
        assertThat(responseEntity.getBody().toString()).contains("Case_Type,Case_Reference,Case_UUID,Case_Timestamp,Stage_legacy-reference,Stage_Name,Stage_UUID,Stage_SchemaVersion,Stage_Timestamp,Stage_who-calling,Stage_rep-first-name,Stage_rep-last-name,Stage_rep-org,Stage_rep-relationship,Stage_rep-calledfrom,Stage_contact-method-helpline,Stage_contact-method-method-mp,Stage_contact-method-media,Stage_contact-method-ie,Stage_contact-method-email,Stage_contact-method-als,Stage_contact-method-internal,Stage_contact-method-external,Stage_call-regarding-citizenship,Stage_call-regarding-settled,Stage_call-regarding-compensation,Stage_call-regarding-other,Stage_first-name,Stage_middle-name,Stage_last-name,Stage_date-of-birth,Stage_nationality-birth,Stage_nationality-current,Stage_address-1,Stage_address-2,Stage_address-town,Stage_post-code,Stage_dependents,Stage_dependents-how-many,Stage_high-profile,Stage_safeguarding,Stage_share-data,Stage_landing-date-day,Stage_landing-date-month,Stage_landing-date-year,Stage_cohort,Stage_date-left,Stage_country-based,Stage_date-last-travelled,Stage_nino,Stage_employment,Stage_education,Stage_tax,Stage_health,Stage_id-docs,Stage_travel-to-psc,Stage_psc-location,Stage_psc-date,Stage_psc-outcome,Stage_psc-followup,Stage_mp,Stage_media,Stage_outcome,Stage_notify-email");
        assertThat(responseEntity.getBody().toString()).contains(caseUUID.toString());
    }

    @Test
    public void getReportCurrentJson() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Map<String, String>> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/report/RSH/current/json",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(responseEntity.getBody().toString()).contains("Case_Type", "RSH");
        assertThat(responseEntity.getBody().toString()).contains(caseUUID.toString());

    }


    @Test
    public void getReportCutoff() {
    }

    @Test
    public void getReportCutoffJson() {
    }


    private Map<String, Object> buildCreateStageBody() {
        Map<String, String> stageData = new HashMap<>();
        stageData.put("A","A1");
        stageData.put("B","B1");
        Map<String, Object> body = new HashMap<>();
        body.put("stageType","stage");
        body.put("stageData",stageData);
        return body;
    }

    private Map<String, String> buildCreateCaseBody() {
        Map<String, String> body = new HashMap<>();
        body.put("caseType", "RSH");
        body.put("caseUUID", caseUUID.toString());
        return body;
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("X-Auth-Userid", "1");
        requestHeaders.set("X-Auth-Username", "bob");
        requestHeaders.set("x-correlation-id", "12");
        return requestHeaders;
    }
}