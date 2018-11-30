package uk.gov.digital.ho.hocs.casework.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.HocsCaseUUID;

import java.util.UUID;

@Aspect
@Component
@Slf4j
public class AuthorisationAspect {

    private CaseDataService caseService;
    private UserPermissionsService userService;

    public AuthorisationAspect(CaseDataService caseService, UserPermissionsService userService) {
        this.caseService = caseService;
        this.userService = userService;
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {

        UUID caseUUID;
        CaseDataType caseType;
        AccessLevel requestedAccessLevel;

        if (authorised.accessLevel() != AccessLevel.UNSET) {
            requestedAccessLevel = authorised.accessLevel();
        } else {
            requestedAccessLevel = getAccessRequestAccessLevel();
        }

        if (joinPoint.getArgs().length > 0) {
            if (joinPoint.getArgs()[0] instanceof UUID) {
                caseUUID = (UUID) joinPoint.getArgs()[0];
                caseType = getCaseTypeFromId(caseUUID);
            } else if (joinPoint.getArgs()[0] instanceof CreateCaseRequest) {
                CreateCaseRequest createCaseRequest = (CreateCaseRequest) joinPoint.getArgs()[0];
                caseType = createCaseRequest.getType();
            } else {
                throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[0].getClass().getName());
            }
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without case UUID parameters");
        }

        AccessLevel accessLevel = userService.getMaxAccessLevel(caseType);

        if (accessLevel.getLevel() >= requestedAccessLevel.getLevel()) {
            return joinPoint.proceed();
        } else {
            throw new SecurityExceptions.PermissionCheckException("User does not have access to the requested resource");
        }
    }

    private CaseDataType getCaseTypeFromId(UUID caseUUID) {
        CaseDataType caseDataType = HocsCaseUUID.getCaseDataType(caseUUID);
        if (caseDataType == null) {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID);
            caseDataType = caseService.getCase(caseUUID).getCaseDataType();
        }
        return caseDataType;
    }

    public AccessLevel getAccessRequestAccessLevel() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = requestAttributes.getRequest().getMethod();
        switch (method) {
            case "GET":
                return AccessLevel.SUMMARY;
            case "POST":
            case "PUT":
            case "DELETE":
                return AccessLevel.WRITE;
            default:
                throw new SecurityExceptions.PermissionCheckException("Unknown access request type " + method);
        }
    }

}
