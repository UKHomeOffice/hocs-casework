package uk.gov.digital.ho.hocs.casework.reports.factory.reports;

import org.springframework.stereotype.Component;

import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;
import uk.gov.digital.ho.hocs.casework.reports.domain.repository.OpenCasesRepository;

import javax.persistence.EntityManager;
import java.util.stream.Stream;

@Component
public class OpenCasesReport implements Report<OpenCasesRow> {

    private final OpenCasesRepository openCasesRepository;
    private final EntityManager entityManager;

    public OpenCasesReport(OpenCasesRepository openCasesRepository, EntityManager entityManager) {
        this.openCasesRepository = openCasesRepository;
        this.entityManager = entityManager;
    }

    @Override
    public String getSlug() {
        return "open_cases";
    }

    @Override
    public Stream<OpenCasesRow> getRows() {
        return openCasesRepository.getReportRows().peek(entityManager::detach);
    }

}
