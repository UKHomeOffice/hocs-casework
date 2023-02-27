package uk.gov.digital.ho.hocs.casework.reports.reports;

import org.springframework.stereotype.Component;

import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.OpenCasesDataMapper;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;
import uk.gov.digital.ho.hocs.casework.reports.domain.repository.OpenCasesRepository;

import javax.persistence.EntityManager;
import java.util.stream.Stream;

@Component
public class OpenCasesReport implements Report<OpenCasesRow> {

    private final OpenCasesRepository openCasesRepository;

    private final EntityManager entityManager;

    private final OpenCasesDataMapper openCasesDataMapper;

    public OpenCasesReport(OpenCasesRepository openCasesRepository,
                           EntityManager entityManager,
                           OpenCasesDataMapper openCasesDataMapper) {
        this.openCasesRepository = openCasesRepository;
        this.entityManager = entityManager;
        this.openCasesDataMapper = openCasesDataMapper;
    }

    @Override
    public String getSlug() {
        return "open_cases";
    }

    @Override
    public Stream<OpenCasesRow> getRows() {
        return openCasesRepository.getReportData().peek(entityManager::detach).map(openCasesDataMapper::mapDataToRow);
    }

}
