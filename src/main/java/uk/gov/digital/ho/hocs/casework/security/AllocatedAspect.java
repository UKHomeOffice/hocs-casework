package uk.gov.digital.ho.hocs.casework.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.StageService;

import java.util.Set;
import java.util.UUID;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
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
                throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[1].getClass().getName());
            }
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without stage UUID parameter");
        }

        switch (allocated.allocatedTo()) {
            case USER:
                UUID userId = userService.getUserId();
                UUID assignedUser = stageService.getStageUser(caseUUID, stageUUID);
                if (!userId.equals(assignedUser)) {
                    throw new SecurityExceptions.StageNotAssignedToLoggedInUserException("Stage " + stageUUID.toString() + " is assigned to " + assignedUser);
                }
                break;
            case TEAM:
                Set<UUID> teams = userService.getUserTeams();
                UUID assignedTeam = stageService.getStageTeam(caseUUID, stageUUID);
                if (!teams.contains(assignedTeam)) {
                    throw new SecurityExceptions.StageNotAssignedToUserTeamException("Stage " + stageUUID.toString() + " is assigned to " + assignedTeam);
                }
                break;
            default:
                throw new SecurityExceptions.PermissionCheckException("Invalid Allocation type");
        }

        return joinPoint.proceed();

    }
}

