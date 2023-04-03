package uk.gov.digital.ho.hocs.casework.reports.factory;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportMetadataDto;
import uk.gov.digital.ho.hocs.casework.reports.reports.Report;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Profile("reports")
public class ReportFactory {

    private final List<Report<?>> reports;

    public ReportFactory(@Lazy List<Report<?>> reports) {this.reports = reports;}

    public Report<?> getReportForSlug(String slug) {
        return reports.stream().filter(r -> Objects.equals(r.getSlug(), slug)).findFirst().orElseThrow();
    }

    public List<ReportMetadataDto> listAvailableReports() {
        return reports
            .stream()
            .map(Report::getReportMetadata)
            .collect(Collectors.toList());
    }

}
