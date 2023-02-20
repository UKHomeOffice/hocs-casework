package uk.gov.digital.ho.hocs.casework.reports.factory.reports;

import org.springframework.stereotype.Component;

import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;
import uk.gov.digital.ho.hocs.casework.reports.domain.repository.OpenCasesRepository;

import java.util.stream.Stream;

@Component
public class OpenCasesReport implements Report<OpenCasesRow> {

    private final OpenCasesRepository openCasesRepository;

    public OpenCasesReport(OpenCasesRepository openCasesRepository) {this.openCasesRepository = openCasesRepository;}

    @Override
    public String getSlug() {
        return "open_cases";
    }

    @Override
    public Stream<OpenCasesRow> getReport() {
        return openCasesRepository.getReportRows();
    }

}
