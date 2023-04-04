package uk.gov.digital.ho.hocs.casework.reports.reports;

import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportColumnDto;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.ReportRow;
import uk.gov.digital.ho.hocs.casework.reports.api.dto.ReportMetadataDto;

import java.util.List;
import java.util.stream.Stream;

public interface Report<T extends ReportRow> {

    String getSlug();

    String getDisplayName();

    String getDescription();

    List<CaseType> getAvailableCaseTypes();

    List<ReportColumnDto> getColumnMetadata();

    String getIdColumnKey();

    Stream<T> getRows(CaseType caseType);

    default ReportMetadataDto getReportMetadata() {
        return new ReportMetadataDto(
            getSlug(),
            getDisplayName(),
            getDescription(),
            getAvailableCaseTypes(),
            getColumnMetadata(),
            getIdColumnKey()
        );
    }

}
