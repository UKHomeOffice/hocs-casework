package uk.gov.digital.ho.hocs.casework.client.notifiyclient;

import lombok.Getter;

public enum NotifyType {

    ALLOCATE_INDIVIDUAL("3dfbd276-2bcc-4b08-81b1-d4f0583cdf39"),
    UNALLOCATE_INDIVIDUAL("6c76fa5b-9bf4-4e39-8ac3-452d49f919b2");

    @Getter
    private String displayValue;

    NotifyType(String value) {
        displayValue = value;
    }
}
