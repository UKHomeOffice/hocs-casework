package uk.gov.digital.ho.hocs.casework.reports.reports;

import org.springframework.stereotype.Component;

import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.OpenCasesDataMapper;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;
import uk.gov.digital.ho.hocs.casework.reports.domain.repository.OpenCasesRepository;
import uk.gov.digital.ho.hocs.casework.reports.dto.ColumnType;
import uk.gov.digital.ho.hocs.casework.reports.dto.ReportColumnDto;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
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
        return "Open Cases Report";
    }

    @Override
    public Stream<OpenCasesRow> getRows() {
        return openCasesRepository.getReportData().peek(entityManager::detach).map(openCasesDataMapper::mapDataToRow);
    }

    @Override
    public List<ReportColumnDto> getColumnMetadata() {
        return List.of(
            new ReportColumnDto("case_uuid", "Case UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("case_reference", "Case Reference", ColumnType.LINK, true, true, Map.of("link_pattern", "/case/${case_uuid}/stage/${stage_uuid}")),
            new ReportColumnDto("business_area", "Business Area", ColumnType.STRING, true, true),
            new ReportColumnDto("age", "Age", ColumnType.NUMBER, true, true),
            new ReportColumnDto("case_deadline", "Case Deadline", ColumnType.DATE, true, true),
            new ReportColumnDto("stage_uuid", "Stage UUID", ColumnType.STRING, false, false),
            new ReportColumnDto("stage_type", "Stage Type", ColumnType.STRING, false, true),
            new ReportColumnDto("assigned_user_uuid", "Assigned User UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("assigned_team_uuid", "Assigned Team UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("user_group", "User Group", ColumnType.STRING, true, true),
            new ReportColumnDto("outside_service_standard", "Outside Service Standard", ColumnType.BOOLEAN, true, true),
            new ReportColumnDto("user_name", "User", ColumnType.STRING, false, true),
            new ReportColumnDto("team_name", "Team", ColumnType.STRING, false, true),
            new ReportColumnDto("stage_name", "Stage", ColumnType.STRING, false, true)
                      );
    }

    @Override
    public String getIdColumnKey() {
        return "case_uuid";
    }

}
