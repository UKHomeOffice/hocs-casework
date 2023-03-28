package uk.gov.digital.ho.hocs.casework.reports.api;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportMetadataDto;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;
import uk.gov.digital.ho.hocs.casework.reports.factory.ReportFactory;
import uk.gov.digital.ho.hocs.casework.reports.reports.Report;

import java.io.IOException;
import java.util.List;

@RestController
@Profile("reports")
public class ReportResource {

    private final ReportFactory reportFactory;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public ReportResource(ReportFactory reportFactory,
                          ObjectMapper objectMapper,
                          TransactionTemplate transactionTemplate) {
        this.reportFactory = reportFactory;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
    }

    @GetMapping("/report")
    List<ReportMetadataDto> getAvailableReports() {
        return reportFactory.listAvailableReports();
    }

    @GetMapping("/report/{caseType}/{slug}")
    ResponseEntity<StreamingResponseBody> getReport(@PathVariable CaseType caseType, @PathVariable String slug) {
        Report<?> report = reportFactory.getReportForSlug(slug);

        StreamingResponseBody body = outputStream -> {
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(outputStream, JsonEncoding.UTF8);
            generator.setCodec(objectMapper);

            generator.writeStartObject();
            generator.writeObjectField("metadata", report.getReportMetadata());
            generator.writeStringField("case_type", caseType.toString());
            generator.writeArrayFieldStart("data");

            transactionTemplate.execute(status -> {
                report.getRows(caseType).forEach(row -> {
                    try {
                        generator.writeObject(row);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                return null;
            });

            generator.writeEndArray();
            generator.writeEndObject();
            generator.close();
        };

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        return new ResponseEntity<>(
            body,
            responseHeaders,
            HttpStatus.OK
        );
    }
}
