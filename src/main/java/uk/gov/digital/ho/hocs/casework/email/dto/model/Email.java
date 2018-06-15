package uk.gov.digital.ho.hocs.casework.email.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class Email {

    private String emailAddress;

    private String templateId;

    private Map<String, String> personalisation;

}
