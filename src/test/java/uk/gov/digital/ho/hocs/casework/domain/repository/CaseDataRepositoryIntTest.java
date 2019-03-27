package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CaseDataRepositoryIntTest {


    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CaseDataRepository repository;

    private UUID caseUUID;
    private CaseData newCase;

    @Before
    public void setup() {
        newCase = new CaseData(new CaseDataType("TEST", "a1"), 101l, LocalDate.of(2018, 1, 1));
        newCase.setCaseDeadline(LocalDate.of(2018,1,29));
        this.entityManager.persist(newCase);
        caseUUID = newCase.getUuid();
    }

    @Test()
    public void shouldInsertCaseDataWithUUID() {
        CaseData caseData = repository.findByUuid(caseUUID);
        assertThat(caseData.getType()).isEqualTo("TEST");
        assertThat(caseData.getCreated().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(caseData.getReference()).isEqualTo("TEST/0000101/" + String.format("%ty", caseData.getCreated()));
        assertThat(caseData.isPriority()).isFalse();
        assertThat(caseData.getPrimaryTopicUUID()).isNull();
        assertThat(caseData.getPrimaryCorrespondentUUID()).isNull();
        assertThat(caseData.getCaseDeadline()).isEqualTo(LocalDate.of(2018, 1, 29));
        assertThat(caseData.getDateReceived()).isEqualTo(LocalDate.of(2018, 1, 1));
    }

    @Test(expected = PersistenceException.class)
    public void shouldThrowExceptionWhenDuplicateReferenceNumber() {
        CaseData newCaseDuplicateReference = new CaseData(new CaseDataType("TEST", "a1"), 101l, LocalDate.of(2018, 01, 01));
        this.entityManager.persist(newCaseDuplicateReference);
    }


    @Test
    public void shouldGetNextSequenceValueForNumbering(){

        Long firstCall = repository.getNextSeriesId();
        Long secondCall = repository.getNextSeriesId();

        assertThat(secondCall).isEqualTo(firstCall + 1L);


    }
}

