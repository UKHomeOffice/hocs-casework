package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface StageWithCaseDataRepository extends CrudRepository<StageWithCaseData, Long> {

    @Query(value = "SELECT sd FROM StageWithCaseData sd LEFT JOIN FETCH sd.tag LEFT JOIN FETCH sd.somu_items WHERE sd.teamUUID = ?1")
    Set<StageWithCaseData> findAllActiveByTeamUUID(UUID teamUUID);

    @Query(value = "SELECT sd FROM StageWithCaseData sd WHERE sd.teamUUID in ?1")
    Set<StageWithCaseData> findAllActiveByTeamUUID(Set<UUID> teamUUID);

}
