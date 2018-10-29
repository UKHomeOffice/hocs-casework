package uk.gov.digital.ho.hocs.casework.security;

import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.casedetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import java.util.UUID;

@Aspect
@Component
@AllArgsConstructor
public class AuthorisationAspect {

    private RequestData requestData;
    private UserClient userClient;
    private CaseDataService caseService;


    @Around("@annotation(Allocated)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint) throws Throwable {

        UUID caseUUID;
        CaseData caseData;
        CaseType caseType;
        String accessType = getAccessRequestType();

        if (joinPoint.getArgs().length > 0) {
            if (joinPoint.getArgs()[0] instanceof UUID) {
                caseUUID = (UUID) joinPoint.getArgs()[0];
                caseType = getCaseTypeFromId(caseUUID);
            } else if (joinPoint.getArgs()[0] instanceof CaseData) {
                caseData = (CaseData) joinPoint.getArgs()[0];
                caseType = caseData.getType();
            } else {
                throw new PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[0].getClass().getName());
            }
        }
        else {
            throw new PermissionCheckException("Unable to check permission of method with no case parameters");
        }

        String userId = requestData.userId();

        if(!userClient.getUserAccess(userId, caseType, accessType)) {
            throw new PermissionCheckException("User does not have access to the requested resource");
        }

        return joinPoint.proceed();
    }

    private CaseType getCaseTypeFromId(UUID caseUUID) {
            return caseService.getCase(caseUUID).getType();
    }

    private String getAccessRequestType() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = sra.getRequest().getMethod();
        switch (method) {
            case "GET":
                return "READ";
            case "POST":
                return "CREATE";
            case "PUT":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                throw new PermissionCheckException("Unknown access request type " + method);
        }
    }
}
