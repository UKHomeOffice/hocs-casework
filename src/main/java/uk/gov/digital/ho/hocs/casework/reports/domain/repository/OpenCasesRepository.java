package uk.gov.digital.ho.hocs.casework.reports.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesData;

import javax.persistence.QueryHint;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

public interface OpenCasesRepository extends JpaRepository<OpenCasesData, UUID> {

    @QueryHints(value = {
        @QueryHint(name = HINT_FETCH_SIZE, value = "50000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"),
        @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query(
        value = "SELECT * FROM report_open_cases",
        nativeQuery = true
    )
    Stream<OpenCasesData> getReportData();

}
