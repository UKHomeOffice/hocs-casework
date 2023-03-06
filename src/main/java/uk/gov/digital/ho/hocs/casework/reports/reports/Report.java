package uk.gov.digital.ho.hocs.casework.reports.reports;

import uk.gov.digital.ho.hocs.casework.reports.dto.ReportColumnDto;
import uk.gov.digital.ho.hocs.casework.reports.dto.ReportRow;
import uk.gov.digital.ho.hocs.casework.reports.dto.ReportMetadataDto;

import java.util.List;
import java.util.stream.Stream;

public interface Report<T extends ReportRow> {

    String getSlug();

    String getDisplayName();

    Stream<T> getRows();

    List<ReportColumnDto> getColumnMetadata();

    String getIdColumnKey();

    default ReportMetadataDto getReportMetadata() {
        return new ReportMetadataDto(
            getSlug(),
            getDisplayName(),
            getColumnMetadata(),
            getIdColumnKey()
        );
    }

}
