package uk.gov.digital.ho.hocs.casework.reports.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.reports.reports.Report;

import java.util.List;
import java.util.Objects;

@Service
public class ReportFactory {

    private final List<Report<?>> reports;

    @Autowired
    public ReportFactory(@Lazy List<Report<?>> reports) {this.reports = reports;}

    public Report<?> getReportForSlug(String slug) {
        return reports.stream()
                      .filter(r -> Objects.equals(r.getSlug(), slug))
                      .findFirst()
                      .orElseThrow();
    }

}
