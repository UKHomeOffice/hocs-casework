package uk.gov.digital.ho.hocs.casework.domain.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface CorrespondentRepository extends CrudRepository<Correspondent, Long> {

    @Query(value = "SELECT c.* FROM correspondent c JOIN case_data cd ON c.case_uuid = cd.uuid WHERE c.case_uuid = ?1 AND c.uuid = ?2 AND NOT c.deleted AND NOT cd.deleted",
           nativeQuery = true)
    Correspondent findByUUID(UUID caseUUID, UUID correspondentUUID);

    @Query(value = "SELECT c.* FROM correspondent c JOIN case_data cd ON c.case_uuid = cd.uuid WHERE c.case_uuid = ?1 AND NOT c.deleted AND NOT cd.deleted",
           nativeQuery = true)
    Set<Correspondent> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT c.* FROM correspondent c JOIN case_data cd on c.case_uuid = cd.uuid AND NOT c.deleted AND NOT cd.deleted",
           nativeQuery = true)
    Set<Correspondent> findAllActive();

    @Query(value = "SELECT c.* FROM correspondent c JOIN case_data cd on c.case_uuid = cd.uuid AND NOT cd.deleted",
           nativeQuery = true)
    @NonNull
    Set<Correspondent> findAll();

    @Query(value = "SELECT CAST(json_build_object('uuid', CAST(c.uuid AS text), 'fullname', c.fullname) as text) FROM correspondent c JOIN case_data cd on c.case_uuid = cd.uuid AND NOT cd.deleted",
           nativeQuery = true)
    Stream<String> findAllUuidToNameMappingJson();

    @Query(value = "SELECT CAST(json_build_object('uuid', CAST(c.uuid AS text), 'fullname', c.fullname) as text) FROM correspondent c JOIN case_data cd on c.case_uuid = cd.uuid AND NOT cd.deleted AND NOT c.deleted",
           nativeQuery = true)
    Stream<String> findActiveUuidToNameMappingJson();

}
