package uk.gov.digital.ho.hocs.casework.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequestInterface;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.*;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Aspect
@Component
@Slf4j
public class AuthorisationAspect {

    private final CaseDataService caseService;

    private final UserPermissionsService userService;

    public AuthorisationAspect(@Qualifier("CaseDataService") CaseDataService caseService,
                               UserPermissionsService userService) {
        this.caseService = caseService;
        this.userService = userService;
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {

        AccessLevel userLevel = getUserAccessLevel(joinPoint);

        if (isSufficientLevel(userLevel.getLevel(), authorised)) {
            return joinPoint.proceed();
        }

        throw new SecurityExceptions.PermissionCheckException("User does not have access to the requested resource",
            SECURITY_UNAUTHORISED);
    }

    private boolean isSufficientLevel(int userLevelAsInt, Authorised authorised) {
        return userLevelAsInt >= getRequiredAccessLevel(authorised).getLevel();
    }

    AccessLevel getAccessRequestAccessLevel() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = Objects.requireNonNull(requestAttributes).getRequest().getMethod();
        return switch (method) {
            case "GET" -> AccessLevel.SUMMARY;
            case "POST", "PUT", "DELETE" -> AccessLevel.WRITE;
            default -> throw new SecurityExceptions.PermissionCheckException(
                "Unknown access request type " + method,
                SECURITY_PARSE_ERROR
            );
        };
    }

    private AccessLevel getRequiredAccessLevel(Authorised authorised) {
        if (authorised.accessLevel()!=AccessLevel.UNSET) {
            return authorised.accessLevel();
        } else {
            return getAccessRequestAccessLevel();
        }
    }

    private AccessLevel getUserAccessLevel(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getArgs().length < 1) {
            throw new SecurityExceptions.PermissionCheckException(
                "Unable to check permission of method without case UUID parameters", SECURITY_PARSE_ERROR);
        }

        String caseType;
        UUID caseUUID = null;
        if (joinPoint.getArgs()[0] instanceof UUID) {
            caseUUID = (UUID) joinPoint.getArgs()[0];
            caseType = caseService.getCaseType(caseUUID);
        } else if (joinPoint.getArgs()[0] instanceof CreateCaseRequestInterface createCaseRequest) {
            caseType = createCaseRequest.getType();
        } else if (joinPoint.getArgs()[0] instanceof String maybeReference) {
            try {
                CaseData caseData = caseService.getCaseDataByReference(maybeReference);
                caseUUID = caseData.getUuid();
                caseType = caseData.getType();
            } catch (ApplicationExceptions.EntityNotFoundException e) {
                throw new SecurityExceptions.PermissionCheckException(
                    "Reference is not a valid case: " + maybeReference,
                    SECURITY_PARSE_ERROR);
            }
        }
        else {
            throw new SecurityExceptions.PermissionCheckException(
                "Unable parse method parameters for type " + joinPoint.getArgs()[0].getClass().getName(),
                SECURITY_PARSE_ERROR);
        }

        AccessLevel accessLevel = userService.getMaxAccessLevel(caseType);

        if (caseUUID!=null && accessLevel.equals(AccessLevel.UNSET)) {
            Set<UUID> teams = userService.getUserTeams();
            if (caseService.getCaseTeams(caseUUID).stream().anyMatch(teams::contains)) {
                return AccessLevel.READ;
            } else {
                return AccessLevel.UNSET;
            }
        }
        return accessLevel;
    }

}
