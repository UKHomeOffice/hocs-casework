package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public class AdditionalField {

    @Getter
    private String label;

    @Getter
    private String value;

    @Getter
    private String type;

    @Getter
    private Object choices;
}