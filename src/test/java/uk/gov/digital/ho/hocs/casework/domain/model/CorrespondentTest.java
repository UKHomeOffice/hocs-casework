package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CorrespondentTest {

    @Test
    public void getCorrespondent() {

        UUID caseUUID = UUID.randomUUID();
        CorrespondentType type = CorrespondentType.CORRESPONDENT;
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";

        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, address, phone, email, reference);

        assertThat(correspondent.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(correspondent.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(correspondent.getCorrespondentType()).isEqualTo(type);
        assertThat(correspondent.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(correspondent.getFullName()).isEqualTo(fullName);
        assertThat(correspondent.getPostcode()).isEqualTo(address.getPostcode());
        assertThat(correspondent.getAddress1()).isEqualTo(address.getAddress1());
        assertThat(correspondent.getAddress2()).isEqualTo(address.getAddress2());
        assertThat(correspondent.getAddress3()).isEqualTo(address.getAddress3());
        assertThat(correspondent.getCountry()).isEqualTo(address.getCountry());
        assertThat(correspondent.getTelephone()).isEqualTo(phone);
        assertThat(correspondent.getEmail()).isEqualTo(email);
        assertThat(correspondent.getReference()).isEqualTo(reference);

    }


    @Test(expected = EntityCreationException.class)
    public void getCorrespondentNullCaseUUID() {

        CorrespondentType type = CorrespondentType.CORRESPONDENT;
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";

        new Correspondent(null, type, fullName, address, phone, email, reference);

    }

    @Test(expected = EntityCreationException.class)
    public void getCorrespondentNullType() {

        UUID caseUUID = UUID.randomUUID();
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";

        new Correspondent(caseUUID, null, fullName, address, phone, email, reference);

    }

}