package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class SomuItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SomuItemRepository repository;

    private SomuItem somuItem = null;

    @Before
    public void setup() {
        somuItem = setUpCaseDataAndSomuItem(1L);
    }

    @Test
    public void shouldFindSomuItemByUuid() {
        var returnedSomuItem = repository.findByUuid(somuItem.getUuid());

        assertNotNull(returnedSomuItem);
        assertEquals(returnedSomuItem, somuItem);
    }

    @Test
    public void shouldFindSomuItemsByCaseUuid() {
        var secondSomuItem = setUpSomuItem(somuItem.getCaseUuid());

        Set<SomuItem> somuItems = repository.findAllByCaseUuid(somuItem.getCaseUuid());

        assertEquals(somuItems.size(), 2);
        assertTrue(somuItems.containsAll(Set.of(somuItem, secondSomuItem)));
    }

    @Test
    public void shouldFindSomuItemsByCaseUuidAndSomuUuid() {
        var somuItems = repository.findByCaseUuidAndSomuUuid(somuItem.getCaseUuid(), somuItem.getSomuUuid());

        assertNotNull(somuItems);
        assertEquals(somuItems.size(), 1);
    }

    @Test
    public void shouldFindSomuItemsByCaseUuids() {
        var secondSomuItem = setUpCaseDataAndSomuItem(2L);

        Set<SomuItem> somuItems = repository.findAllByCaseUuidIn(
            Set.of(somuItem.getCaseUuid(), secondSomuItem.getCaseUuid()));

        assertEquals(somuItems.size(), 2);
        assertTrue(somuItems.containsAll(Set.of(somuItem, secondSomuItem)));
    }

    private SomuItem setUpCaseDataAndSomuItem(long caseNumber) {
        CaseData caseData = new CaseData(CaseDataTypeFactory.from("TEST", "a1"), caseNumber,
            LocalDate.of(2000, 12, 31));
        caseData.setCaseDeadline(LocalDate.of(9999, 12, 31));
        this.entityManager.persist(caseData);

        return setUpSomuItem(caseData.getUuid());
    }

    private SomuItem setUpSomuItem(UUID caseUuid) {
        SomuItem somuItem = new SomuItem(UUID.randomUUID(), caseUuid, UUID.randomUUID(), "{}");
        return entityManager.persist(somuItem);
    }

}
