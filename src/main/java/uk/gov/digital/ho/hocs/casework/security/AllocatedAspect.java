package uk.gov.digital.ho.hocs.casework.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.application.NonMigrationEnvCondition;

import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
@Conditional(value = {NonMigrationEnvCondition.class})
public class AllocatedAspect {

    private StageService stageService;
    private UserPermissionsService userService;

    @Around("@annotation(allocated)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Allocated allocated) throws Throwable {
        UUID caseUUID;
        UUID stageUUID;
        if (joinPoint.getArgs().length >= 2) {
            if (joinPoint.getArgs()[0] instanceof UUID && joinPoint.getArgs()[1] instanceof UUID) {
                caseUUID = (UUID) joinPoint.getArgs()[0];
                stageUUID = (UUID) joinPoint.getArgs()[1];
            } else {
                throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[1].getClass().getName(), SECURITY_PARSE_ERROR);
            }
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without stage UUID parameter", SECURITY_PARSE_ERROR);
        }

        switch (allocated.allocatedTo()) {
            case USER:
                UUID userId = userService.getUserId();
                UUID assignedUser = stageService.getStageUser(caseUUID, stageUUID);
                if (!userId.equals(assignedUser)) {
                throw new SecurityExceptions.StageNotAssignedToLoggedInUserException(String.format("Stage %s is assigned to %s", stageUUID.toString(), assignedUser), SECURITY_CASE_NOT_ALLOCATED_TO_USER);
                }
                break;
            case TEAM:
                Set<UUID> teams = userService.getUserTeams();
                UUID assignedTeam = stageService.getStageTeam(caseUUID, stageUUID);
                if (!teams.contains(assignedTeam)) {
                throw new SecurityExceptions.StageNotAssignedToUserTeamException(String.format("Stage %s is assigned to %s", stageUUID.toString(), assignedTeam), SECURITY_CASE_NOT_ALLOCATED_TO_TEAM);
                }
                break;
            default:
                throw new SecurityExceptions.PermissionCheckException("Invalid Allocation type", SECURITY_PARSE_ERROR);
        }

        return joinPoint.proceed();

    }
}
