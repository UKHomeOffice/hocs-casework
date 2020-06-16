package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor()
@Getter
public class ProfileDto {

    private String profileName;
    private boolean summaryDeadlineEnabled;
    private List<Object> searchFields;

}
