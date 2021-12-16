package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.BankHoliday;

import java.util.Set;

@Repository
public interface BankHolidayRepository extends CrudRepository<BankHoliday, Long> {
    Set<BankHoliday> findAll();
    Set<BankHoliday> findByRegionIn(Set<BankHoliday.BankHolidayRegion> regions);
}
