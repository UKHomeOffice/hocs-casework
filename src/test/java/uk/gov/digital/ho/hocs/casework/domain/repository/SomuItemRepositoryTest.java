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
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class SomuItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private SomuItemRepository repository;

    private final UUID SOMU_ITEM_UUID = UUID.randomUUID();
    private final UUID SOMU_ITEM_TYPE_UUID = UUID.randomUUID();

    private UUID caseUuid = null;
    private SomuItem somuItem = null;
    
    @Before
    public void setup() {
        CaseData caseData = new CaseData(new CaseDataType("TEST", "a1"), 101L, LocalDate.of(2000, 12, 31));
        caseData.setCaseDeadline(LocalDate.of(9999,12,31));
        caseUuid = caseData.getUuid();
        this.entityManager.persist(caseData);
        
        somuItem = new SomuItem(SOMU_ITEM_UUID, caseUuid, SOMU_ITEM_TYPE_UUID, "{}");
        
        this.entityManager.persist(somuItem);
    }

    @Test()
    public void shouldFindSomuItemByUuid() {
        SomuItem somuItem = repository.findByUuid(SOMU_ITEM_UUID);

        assertThat(somuItem).isNotNull();
        assertThat(somuItem).isEqualTo(this.somuItem);
    }

    @Test()
    public void shouldFindSomuItemsByCaseUuid() {
        UUID somuItemUuid = UUID.randomUUID();
        SomuItem somuItem = new SomuItem(somuItemUuid, caseUuid, UUID.randomUUID(), "{}");
        this.entityManager.persist(somuItem);

        Set<SomuItem> somuItems = repository.findAllByCaseUuid(caseUuid);

        assertThat(somuItems.size()).isEqualTo(2);
        assertThat(somuItems.contains(this.somuItem)).isTrue();
        assertThat(somuItems.contains(somuItem)).isTrue();
    }

    @Test()
    public void shouldFindSomuItemsByCaseUuidAndSomuUuid() {
        SomuItem somuItem = repository.findByCaseUuidAndSomuUuid(caseUuid, SOMU_ITEM_TYPE_UUID);

        assertThat(somuItem).isNotNull();
        assertThat(somuItem).isEqualTo(this.somuItem);
    }
    
}
