package uk.gov.digital.ho.hocs.casework.reports.api;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.digital.ho.hocs.casework.api.utils.JsonResponseStreamer;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportMetadataDto;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;
import uk.gov.digital.ho.hocs.casework.reports.factory.ReportFactory;
import uk.gov.digital.ho.hocs.casework.reports.reports.Report;

import java.util.List;
import java.util.Map;

@RestController
@Profile("reporting")
public class ReportResource {

    private final ReportFactory reportFactory;
    private final JsonResponseStreamer jsonResponseStreamer;

    public ReportResource(ReportFactory reportFactory, JsonResponseStreamer jsonResponseStreamer)
    {
        this.reportFactory = reportFactory;
        this.jsonResponseStreamer = jsonResponseStreamer;
    }

    @GetMapping("/report")
    List<ReportMetadataDto> getAvailableReports() {
        return reportFactory.listAvailableReports();
    }

    @GetMapping("/report/{caseType}/{slug}")
    ResponseEntity<StreamingResponseBody> getReport(@PathVariable CaseType caseType, @PathVariable String slug) {
        Report<?> report = reportFactory.getReportForSlug(slug);

        if(!report.getAvailableCaseTypes().contains(caseType)) {
            throw new ApplicationExceptions.ReportCaseTypeNotSupportedException(
                "The \"%s\" report does not support the \"%s\" case type",
                LogEvent.REPORT_RESOURCE_UNSUPPORTED_CASE_TYPE,
                report.getDisplayName(),
                caseType
            );
        }

        return jsonResponseStreamer.jsonWrappedTransactionalStreamingResponseBody(
            "data",
            () -> report.getRows(caseType),
            Map.of(
                "metadata", report.getReportMetadata(),
                "case_type", caseType.toString()
            )
        );
    }
}
