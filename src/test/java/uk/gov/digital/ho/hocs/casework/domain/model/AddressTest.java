package uk.gov.digital.ho.hocs.casework.domain.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest {

    @Test
    public void getAddress() {
        String postCode = "anyPostcode";
        String address1 = "any1";
        String address2 = "any2";
        String address3 = "any3";
        String country = "anyCountry";

        Address address = new Address(postCode, address1, address2, address3, country);

        assertThat(address.getPostcode()).isEqualTo(postCode);
        assertThat(address.getAddress1()).isEqualTo(address1);
        assertThat(address.getAddress2()).isEqualTo(address2);
        assertThat(address.getAddress3()).isEqualTo(address3);
        assertThat(address.getCountry()).isEqualTo(country);

    }

    @Test
    public void getAddressNull() {
        String postCode = null;
        String address1 = null;
        String address2 = null;
        String address3 = null;
        String country = null;

        Address address = new Address(postCode, address1, address2, address3, country);

        assertThat(address.getPostcode()).isEqualTo(postCode);
        assertThat(address.getAddress1()).isEqualTo(address1);
        assertThat(address.getAddress2()).isEqualTo(address2);
        assertThat(address.getAddress3()).isEqualTo(address3);
        assertThat(address.getCountry()).isEqualTo(country);

    }

}