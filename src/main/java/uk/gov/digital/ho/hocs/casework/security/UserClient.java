package uk.gov.digital.ho.hocs.casework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;



@Slf4j
@Component
public class UserClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;


    @Autowired
    public UserClient(RestHelper restHelper,
                      @Value("${hocs.user-service}") String userService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = userService;
    }

    public boolean getUserAccess(String userId, CaseType caseType, String accessType) {
        ResponseEntity<UserPermissionResponse> response = restHelper.get(serviceBaseURL, String.format("/user/%s/%s/%s", userId, caseType, accessType), UserPermissionResponse.class);
        return response.getBody().allowed;

    }
}
