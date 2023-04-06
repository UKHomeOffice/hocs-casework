package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import java.util.Optional;

public interface ReportValueMapper<S, T> {
    void refreshCache();
    Optional<T> map(S source);
}
