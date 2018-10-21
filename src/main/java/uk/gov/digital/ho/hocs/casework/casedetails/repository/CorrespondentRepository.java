package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Correspondent;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CorrespondentRepository extends CrudRepository<Correspondent, String> {

    @Query(value = "SELECT c.* FROM correspondent c join case_data cd on c.case_uuid = cd.uuid WHERE cd.uuid = ?1 AND cd.deleted = FALSE AND c.deleted = FALSE", nativeQuery = true)
    Set<Correspondent> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT c.* FROM correspondent c join case_data cd on c.case_uuid = cd.uuid WHERE cd.uuid = ?1 AND cd.deleted = FALSE AND c.uuid = ?2 AND c.deleted = FALSE", nativeQuery = true)
    Correspondent findByUUID(UUID caseUUID, UUID correspondentUUID);

    @Modifying
    @Query(value = "UPDATE correspondent c SET c.deleted = TRUE WHERE uuid = ?1", nativeQuery = true)
    int delete(UUID correspondentUUID);
}
