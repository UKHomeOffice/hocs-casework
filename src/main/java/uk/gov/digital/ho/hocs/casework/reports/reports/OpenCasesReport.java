package uk.gov.digital.ho.hocs.casework.reports.reports;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import uk.gov.digital.ho.hocs.casework.reports.api.dto.FilterType;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;
import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.OpenCasesDataMapper;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;
import uk.gov.digital.ho.hocs.casework.reports.domain.repository.OpenCasesRepository;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ColumnType;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportColumnDto;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;

@Component
@Profile("reports")
public class OpenCasesReport implements Report<OpenCasesRow> {

    private final OpenCasesRepository openCasesRepository;

    private final EntityManager entityManager;

    private final OpenCasesDataMapper openCasesDataMapper;

    public OpenCasesReport(
        OpenCasesRepository openCasesRepository, EntityManager entityManager, OpenCasesDataMapper openCasesDataMapper
                          ) {
        this.openCasesRepository = openCasesRepository;
        this.entityManager = entityManager;
        this.openCasesDataMapper = openCasesDataMapper;
    }

    @Override
    public String getSlug() {
        return "open-cases";
    }

    @Override
    public String getDisplayName() {
        return "Open cases report";
    }

    @Override
    public String getDescription() {
        return "Cases that have not been completed.";
    }

    @Override
    public List<CaseType> getAvailableCaseTypes() {
        return List.of(CaseType.COMP, CaseType.COMP2, CaseType.MPAM);
    }

    @Override
    public List<ReportColumnDto> getColumnMetadata() {
        return List.of(
            new ReportColumnDto("case_uuid", "Case UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("case_reference", "Reference", ColumnType.LINK, true, true)
                .withAdditionalField("link_pattern", "/case/${case_uuid}/stage/${stage_uuid}")
                .withFilter(FilterType.CONTAINS_TEXT),
            new ReportColumnDto("business_area", "Business area", ColumnType.STRING, true, true),
            new ReportColumnDto("date_created", "Created", ColumnType.DATE, true, true),
            new ReportColumnDto("age", "Age", ColumnType.NUMBER, true, true),
            new ReportColumnDto("case_deadline", "Deadline", ColumnType.DATE, true, true)
                .withFilter(FilterType.DATE_RANGE),
            new ReportColumnDto("stage_uuid", "Stage UUID", ColumnType.STRING, false, false),
            new ReportColumnDto("stage_type", "Stage Type", ColumnType.STRING, false, true),
            new ReportColumnDto("assigned_user_uuid", "Assigned user UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("assigned_team_uuid", "Assigned team UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("outside_service_standard", "Outside service standard", ColumnType.BOOLEAN, true, true)
                .withFilter(FilterType.BOOLEAN),
            new ReportColumnDto("user_name", "User", ColumnType.STRING, false, true),
            new ReportColumnDto("team_name", "Team", ColumnType.STRING, false, true),
            new ReportColumnDto("stage_name", "Stage", ColumnType.STRING, true, true)
                .withFilter(FilterType.SELECT)
        );
    }

    @Override
    public String getIdColumnKey() {
        return "case_uuid";
    }

    @Override
    public Stream<OpenCasesRow> getRows(CaseType caseType) {
        return openCasesRepository.getReportData(caseType).peek(entityManager::detach).map(openCasesDataMapper::mapDataToRow);
    }

}
