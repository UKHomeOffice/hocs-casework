package uk.gov.digital.ho.hocs.casework.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequestInterface;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SECURITY_PARSE_ERROR;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SECURITY_UNAUTHORISED;

@Aspect
@Component
@Slf4j
public class AuthorisationAspect {

    private CaseDataService caseService;
    private UserPermissionsService userService;

    public AuthorisationAspect(@Qualifier("CaseDataService") CaseDataService caseService, UserPermissionsService userService) {
        this.caseService = caseService;
        this.userService = userService;
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {
        if (isAllowedToProceed(joinPoint, authorised)) {
            return joinPoint.proceed();
        } else {
            throw new SecurityExceptions.PermissionCheckException("User does not have access to the requested resource", SECURITY_UNAUTHORISED);
        }
    }

    private boolean isAllowedToProceed(ProceedingJoinPoint joinPoint, Authorised authorised) {
        return (getUserAccessLevel(joinPoint).getLevel() >= getRequiredAccessLevel(authorised).getLevel()) || getPermittedSpecificLevels(authorised,getUserAccessLevel(joinPoint).getLevel());
    }

    private boolean getPermittedSpecificLevels(Authorised authorised, int usersLevel) {
        return Arrays.stream(authorised.allowSpecificLevels()).anyMatch(level -> level.getLevel() == usersLevel);
    }

    AccessLevel getAccessRequestAccessLevel() {
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
                throw new SecurityExceptions.PermissionCheckException("Unknown access request type " + method, SECURITY_PARSE_ERROR);
        }
    }

    private AccessLevel getRequiredAccessLevel(Authorised authorised) {
        if (authorised.accessLevel() != AccessLevel.UNSET) {
            return authorised.accessLevel();
        } else {
            return getAccessRequestAccessLevel();
        }
    }

    private AccessLevel getUserAccessLevel(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getArgs().length < 1) {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without case UUID parameters", SECURITY_PARSE_ERROR);
        }

        String caseType;
        UUID caseUUID = null;
        if (joinPoint.getArgs()[0] instanceof UUID) {
            caseUUID = (UUID) joinPoint.getArgs()[0];
            caseType = caseService.getCaseType(caseUUID);
        } else if (joinPoint.getArgs()[0] instanceof CreateCaseRequestInterface) {
            CreateCaseRequestInterface createCaseRequest = (CreateCaseRequestInterface) joinPoint.getArgs()[0];
            caseType = createCaseRequest.getType();
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[0].getClass().getName(), SECURITY_PARSE_ERROR);
        }

        AccessLevel accessLevel = userService.getMaxAccessLevel(caseType);

        if (caseUUID != null && accessLevel.equals(AccessLevel.UNSET)) {
            Set<UUID> teams = userService.getUserTeams();
            if (caseService.getCaseTeams(caseUUID).stream().anyMatch(t -> teams.contains(t))) {
                return AccessLevel.READ;
            } else {
                return AccessLevel.UNSET;
            }
        }
        return accessLevel;
    }
}
