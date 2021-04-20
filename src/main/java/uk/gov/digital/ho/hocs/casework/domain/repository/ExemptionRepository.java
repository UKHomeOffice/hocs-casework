package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Exemption;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ExemptionRepository extends CrudRepository<Exemption, Long> {


    Set<Exemption> findAllByCaseUUID(UUID caseUUID);

}
