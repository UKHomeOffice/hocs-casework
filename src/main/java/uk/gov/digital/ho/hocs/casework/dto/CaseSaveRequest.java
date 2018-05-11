package uk.gov.digital.ho.hocs.casework.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CaseSaveRequest {

    private String userName;

    private String notifyEmail;

    private String data;
}
