package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class HocsFormField {

    public String component;

    public List<String> validation;

    public HocsFormProperty props;
}
