package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CaseTab {

    @Getter
    private final String name;

    @Getter
    private final String label;

    @Getter
    private final String screen;

}
