package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CorrespondentTest {

    @Test
    public void getCorrespondent() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        String organisation = "anyOrganisation";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";

        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, organisation, address, phone, email, reference, externalKey);

        assertThat(correspondent.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(correspondent.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(correspondent.getCorrespondentType()).isEqualTo(type);
        assertThat(correspondent.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(correspondent.getFullName()).isEqualTo(fullName);
        assertThat(correspondent.getOrganisation()).isEqualTo(organisation);
        assertThat(correspondent.getPostcode()).isEqualTo(address.getPostcode());
        assertThat(correspondent.getAddress1()).isEqualTo(address.getAddress1());
        assertThat(correspondent.getAddress2()).isEqualTo(address.getAddress2());
        assertThat(correspondent.getAddress3()).isEqualTo(address.getAddress3());
        assertThat(correspondent.getCountry()).isEqualTo(address.getCountry());
        assertThat(correspondent.getTelephone()).isEqualTo(phone);
        assertThat(correspondent.getEmail()).isEqualTo(email);
        assertThat(correspondent.getReference()).isEqualTo(reference);
        assertThat(correspondent.isDeleted()).isFalse();

    }

    @Test
    public void SetCorrespondentDeleted() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        String organisation = "anyOrganisation";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";

        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, organisation, address, phone, email, reference, externalKey);

        assertThat(correspondent.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(correspondent.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(correspondent.getCorrespondentType()).isEqualTo(type);
        assertThat(correspondent.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(correspondent.getFullName()).isEqualTo(fullName);
        assertThat(correspondent.getOrganisation()).isEqualTo(organisation);
        assertThat(correspondent.getPostcode()).isEqualTo(address.getPostcode());
        assertThat(correspondent.getAddress1()).isEqualTo(address.getAddress1());
        assertThat(correspondent.getAddress2()).isEqualTo(address.getAddress2());
        assertThat(correspondent.getAddress3()).isEqualTo(address.getAddress3());
        assertThat(correspondent.getCountry()).isEqualTo(address.getCountry());
        assertThat(correspondent.getTelephone()).isEqualTo(phone);
        assertThat(correspondent.getEmail()).isEqualTo(email);
        assertThat(correspondent.getReference()).isEqualTo(reference);
        assertThat(correspondent.isDeleted()).isFalse();
        assertThat(correspondent.getExternalKey()).isEqualTo(externalKey);

        correspondent.setDeleted(true);

        assertThat(correspondent.getUuid()).isOfAnyClassIn(UUID.randomUUID().getClass());
        assertThat(correspondent.getCreated()).isOfAnyClassIn(LocalDateTime.now().getClass());
        assertThat(correspondent.getCorrespondentType()).isEqualTo(type);
        assertThat(correspondent.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(correspondent.getFullName()).isEqualTo(fullName);
        assertThat(correspondent.getOrganisation()).isEqualTo(organisation);
        assertThat(correspondent.getPostcode()).isEqualTo(address.getPostcode());
        assertThat(correspondent.getAddress1()).isEqualTo(address.getAddress1());
        assertThat(correspondent.getAddress2()).isEqualTo(address.getAddress2());
        assertThat(correspondent.getAddress3()).isEqualTo(address.getAddress3());
        assertThat(correspondent.getCountry()).isEqualTo(address.getCountry());
        assertThat(correspondent.getTelephone()).isEqualTo(phone);
        assertThat(correspondent.getEmail()).isEqualTo(email);
        assertThat(correspondent.getReference()).isEqualTo(reference);
        assertThat(correspondent.isDeleted()).isTrue();

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCorrespondentNullCaseUUID() {

        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        String organisation = "anyOrganisation";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";

        new Correspondent(null, type, fullName, organisation, address, phone, email, reference, externalKey);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void getCorrespondentNullType() {

        UUID caseUUID = UUID.randomUUID();
        String fullName = "anyFullName";
        String organisation = "anyOrganisation";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";

        new Correspondent(caseUUID, null, fullName, organisation, address, phone, email, reference, externalKey);

    }

}