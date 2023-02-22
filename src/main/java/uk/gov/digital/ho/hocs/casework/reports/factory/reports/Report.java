package uk.gov.digital.ho.hocs.casework.reports.factory.reports;

import uk.gov.digital.ho.hocs.casework.reports.dto.ReportRow;

import java.util.stream.Stream;


public interface Report<T extends ReportRow> {
    String getSlug();
    Stream<T> getRows();
}
