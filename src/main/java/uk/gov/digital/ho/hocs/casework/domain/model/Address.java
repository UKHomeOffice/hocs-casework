package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;

@Getter
public class Address {

    private String postcode;

    private String address1;

    private String address2;

    private String address3;

    private String country;

    public Address(String postcode, String address1, String address2, String address3, String country) {

        this.postcode = postcode;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.country = country;
    }
}