package uk.gov.digital.ho.hocs.casework.reports.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import uk.gov.digital.ho.hocs.casework.reports.domain.CaseType;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.WorkInProgressData;

import javax.persistence.QueryHint;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

public interface WorkInProgressRepository extends JpaRepository<WorkInProgressData, UUID> {

    @QueryHints(value = {
        @QueryHint(name = HINT_FETCH_SIZE, value = "50000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"),
        @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query(
        value = "SELECT * FROM report_work_in_progress WHERE case_type = :#{#caseType?.name()}",
        nativeQuery = true
    )
    Stream<WorkInProgressData> getReportData(@Param("caseType") CaseType caseType);

}
