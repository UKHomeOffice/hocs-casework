package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@java.lang.SuppressWarnings("squid:S1068")
@Builder
@AllArgsConstructor
@Getter
public class Address {

    private String postcode;

    private String address1;

    private String address2;

    private String address3;

    private String country;

}