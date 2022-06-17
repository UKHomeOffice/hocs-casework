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
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CorrespondentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CorrespondentRepository repository;

    private UUID caseUUID = UUID.randomUUID();

    private UUID correspondentUUID;


    private Address address;

    @Before
    public void setup() {
        CaseData newCase = new CaseData(CaseDataTypeFactory.from("TEST", "a1"), 101l, LocalDate.of(2018, 1, 1));
        newCase.setCaseDeadline(LocalDate.of(2018, 1, 29));
        caseUUID = newCase.getUuid();
        this.entityManager.persist(newCase);

        address = new Address("some_postcode", "line1", "line2", "line3", "country");

        Correspondent correspondent = new Correspondent(
                caseUUID,
                "some_type",
                "full name",
                "organisation",
                address,
                "01923478393",
                "email@test.com",
                "ref",
                "key"
        );

        this.entityManager.persist(correspondent);
        correspondentUUID = correspondent.getUuid();
    }

    @Test
    public void shouldFindCorrespondentByUUID() {
        Correspondent correspondent = repository.findByUUID(caseUUID, correspondentUUID);
        assertThat(correspondent.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(correspondent.getFullName()).isEqualTo("full name");
        assertThat(correspondent.getOrganisation()).isEqualTo("organisation");
        assertThat(correspondent.getEmail()).isEqualTo("email@test.com");
        assertThat(correspondent.getReference()).isEqualTo("ref");
        assertThat(correspondent.getCorrespondentType()).isEqualTo("some_type");
        assertThat(correspondent.getPostcode()).isEqualTo("some_postcode");
    }

    @Test
    public void shouldFindAllByCaseUUID() {
        Set<Correspondent> correspondents = repository.findAllByCaseUUID(caseUUID);
        assertThat(correspondents.stream().findFirst().get().getEmail()).isEqualTo("email@test.com");
        assertThat(correspondents.stream().findFirst().get().getCaseUUID()).isEqualTo(caseUUID);
        assertThat(correspondents.stream().findFirst().get().getEmail()).isEqualTo("email@test.com");
        assertThat(correspondents.stream().findFirst().get().getReference()).isEqualTo("ref");
        assertThat(correspondents.stream().findFirst().get().getCorrespondentType()).isEqualTo("some_type");
        assertThat(correspondents.stream().findFirst().get().getPostcode()).isEqualTo("some_postcode");
    }

    @Test
    public void shouldFindAllActive() {
        Set<Correspondent> correspondents = repository.findAllActive();
        assertThat(correspondents.size()).isPositive();
    }

}
