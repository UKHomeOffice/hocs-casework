package uk.gov.digital.ho.hocs.casework.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserPermissionResponse {
    String userId;
    boolean allowed;
}
