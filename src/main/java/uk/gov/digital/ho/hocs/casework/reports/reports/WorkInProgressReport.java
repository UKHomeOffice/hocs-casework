package uk.gov.digital.ho.hocs.casework.reports.reports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ColumnType;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.FilterType;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportColumnDto;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;
import uk.gov.digital.ho.hocs.casework.reports.domain.mapping.WorkInProgressDataMapper;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.WorkInProgressRow;
import uk.gov.digital.ho.hocs.casework.reports.domain.repository.WorkInProgressRepository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;

@Component
@Profile("reporting")
public class WorkInProgressReport implements Report<WorkInProgressRow> {

    private final WorkInProgressRepository workInProgressRepository;

    private final EntityManager entityManager;

    private final WorkInProgressDataMapper workInProgressDataMapper;

    private final ObjectMapper objectMapper;

    public WorkInProgressReport(
        WorkInProgressRepository workInProgressRepository,
        EntityManager entityManager,
        WorkInProgressDataMapper workInProgressDataMapper,
        ObjectMapper objectMapper
                               ) {
        this.workInProgressRepository = workInProgressRepository;
        this.entityManager = entityManager;
        this.workInProgressDataMapper = workInProgressDataMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSlug() {
        return "work-in-progress";
    }

    @Override
    public String getDisplayName() {
        return "Work in progress report";
    }

    @Override
    public String getDescription() {
        return "A report of in progress cases to allow allocation to appropriate case workers.";
    }

    @Override
    public List<CaseType> getAvailableCaseTypes() {
        return List.of(CaseType.COMP, CaseType.COMP2);
    }

    @Override
    public List<ReportColumnDto> getColumnMetadata() {
        return List.of(new ReportColumnDto("case_uuid", "Case UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("case_reference", "Reference", ColumnType.LINK, true, true).withAdditionalField(
                "link_pattern", "/case/${case_uuid}/stage/${stage_uuid}"),
            new ReportColumnDto("stage_uuid", "Stage UUID", ColumnType.STRING, false, false),
            new ReportColumnDto("stage_name", "Stage", ColumnType.STRING, true, true).withFilter(FilterType.SELECT),
            new ReportColumnDto("case_summary", "Case summary", ColumnType.STRING, true, true).withFilter(
                FilterType.CONTAINS_TEXT),
            new ReportColumnDto("allocation_status", "Allocation status", ColumnType.BOOLEAN, true, true)
                .withAdditionalField("label_if_true", "Allocated")
                .withAdditionalField("label_if_false", "Unallocated")
                .withFilter(FilterType.BOOLEAN),
            new ReportColumnDto("allocated_to", "Allocated to", ColumnType.STRING, true, true).withFilter(
                FilterType.SELECT), new ReportColumnDto("case_deadline", "Deadline", ColumnType.DATE, true, true),
            new ReportColumnDto("due_week", "Due week", ColumnType.STRING, false, false)
                .withAdditionalField("filter_values", getDueDateFilterValues())
                .withFilter(FilterType.SELECT),
            new ReportColumnDto("severity", "Severity", ColumnType.STRING, true, true).withFilter(FilterType.SELECT),
            new ReportColumnDto("comp_type", "Complaint type", ColumnType.STRING, false, true),
            new ReportColumnDto("date_created", "Created", ColumnType.DATE, false, true),
            new ReportColumnDto("date_received", "Received", ColumnType.DATE, false, true),
            new ReportColumnDto("owning_csu", "Owning CSU", ColumnType.STRING, false, true),
            new ReportColumnDto("directorate", "Directorate", ColumnType.STRING, false, true),
            new ReportColumnDto("business_area_based_on_directorate", "Business area based on directorate",
                ColumnType.STRING, false, true
            ), new ReportColumnDto("enquiry_reason", "Enquiry reason", ColumnType.STRING, false, true),
            new ReportColumnDto("primary_correspondent_name", "Primary correspondent name", ColumnType.STRING, false,
                true
            ), new ReportColumnDto("stage_type", "Stage Type", ColumnType.STRING, false, true),
            new ReportColumnDto("assigned_user_uuid", "Assigned user UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("assigned_team_uuid", "Assigned team UUID", ColumnType.STRING, false, true),
            new ReportColumnDto("user_name", "User", ColumnType.STRING, false, true),
            new ReportColumnDto("team_name", "Team", ColumnType.STRING, false, true)
                      );
    }

    private String getDueDateFilterValues() {
        try {
            return objectMapper.writeValueAsString(
                List.of("Due this week", "Due next week", "Due week 3", "Due week 4", "Due week 5",
                    "Outside service standard"
                       ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialise hardcoded Map to JSON object", e);
        }
    }

    @Override
    public String getIdColumnKey() {
        return "case_uuid";
    }

    @Override
    public Stream<WorkInProgressRow> getRows(CaseType caseType) {
        return workInProgressRepository
            .getReportData(caseType)
            .peek(entityManager::detach)
            .map(workInProgressDataMapper::mapDataToRow);
    }

}
