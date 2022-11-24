package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import javax.persistence.QueryHint;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.QueryHints.HINT_PASS_DISTINCT_THROUGH;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;

@Repository
public interface WorkstackStageRepository extends JpaRepository<CaseData, UUID> {

    @Query(value = "SELECT distinct cd FROM WorkstackCaseData cd LEFT JOIN FETCH cd.activeStages sd LEFT JOIN FETCH cd.correspondents LEFT JOIN FETCH cd.primaryTopic LEFT JOIN FETCH cd.tag LEFT JOIN FETCH cd.somu_items WHERE sd.teamUUID = ?1 ORDER BY cd.uuid")
    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "1000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"),
        @QueryHint(name = HINT_READONLY, value = "true"),
        @QueryHint(name = HINT_PASS_DISTINCT_THROUGH, value = "false") })
    Stream<CaseData> findAllActiveByTeamUUID(UUID teamUUID);

    @Query(value = "SELECT distinct cd FROM WorkstackCaseData cd LEFT JOIN FETCH cd.activeStages sd LEFT JOIN FETCH cd.correspondents LEFT JOIN FETCH cd.primaryTopic LEFT JOIN FETCH cd.tag LEFT JOIN FETCH cd.somu_items WHERE sd.userUUID = ?1 AND sd.teamUUID in ?2 ORDER BY cd.uuid")
    @QueryHints(value = { @QueryHint(name = HINT_READONLY, value = "true"),
        @QueryHint(name = HINT_PASS_DISTINCT_THROUGH, value = "false") })
    Set<CaseData> findAllActiveByUserUuidAndTeamUuid(UUID userUuid, Set<UUID> teamUUID);

    @Query(value = "SELECT distinct cd FROM WorkstackCaseData cd LEFT JOIN FETCH cd.activeStages sd LEFT JOIN FETCH cd.correspondents LEFT JOIN FETCH cd.primaryTopic LEFT JOIN FETCH cd.tag LEFT JOIN FETCH cd.somu_items WHERE sd.teamUUID = ?1 AND sd.userUUID IS NULL ORDER BY cd.uuid")
    @QueryHints(value = { @QueryHint(name = HINT_READONLY, value = "true"),
        @QueryHint(name = HINT_PASS_DISTINCT_THROUGH, value = "false") })
    Set<CaseData> findAllUnassignedAndActiveByTeamUUID(UUID teamUUID);

    @Query(value = "SELECT distinct cd FROM WorkstackCaseData cd LEFT JOIN FETCH cd.activeStages sd LEFT JOIN FETCH cd.correspondents LEFT JOIN FETCH cd.primaryTopic LEFT JOIN FETCH cd.tag LEFT JOIN FETCH cd.somu_items WHERE sd.teamUUID in ?1 ORDER BY cd.uuid")
    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "1000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"),
        @QueryHint(name = HINT_READONLY, value = "true"),
        @QueryHint(name = HINT_PASS_DISTINCT_THROUGH, value = "false") })
    Stream<CaseData> findAllActiveByTeamUUID(Set<UUID> teamUUID);

}
