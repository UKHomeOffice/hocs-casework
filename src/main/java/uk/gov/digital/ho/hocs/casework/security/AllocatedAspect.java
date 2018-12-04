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
        UUID stageUUID;
        UUID caseUUID;
        if (joinPoint.getArgs().length >= 2) {
            if (joinPoint.getArgs()[0] instanceof UUID && joinPoint.getArgs()[1] instanceof UUID) {
                stageUUID = (UUID) joinPoint.getArgs()[1];
                caseUUID = (UUID) joinPoint.getArgs()[0];
            } else {
                throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[1].getClass().getName());
            }
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without stage UUID parameter");
        }

        if (allocated.allocatedTo() == AllocationLevel.USER) {
            UUID userId = userService.getUserId();

            UUID assignedUser = stageService.getStage(caseUUID, stageUUID).getUserUUID();
            if (!userId.equals(assignedUser)) {
                throw new SecurityExceptions.StageNotAssignedToLoggedInUserException("Stage " + stageUUID.toString() + " is assigned to " + assignedUser);
            }
            return joinPoint.proceed();
        } else {
            Set<UUID> teams = userService.getUserTeams();
            UUID assignedTeam = stageService.getStage(caseUUID, stageUUID).getTeamUUID();
            if (!teams.contains(assignedTeam)) {
                throw new SecurityExceptions.StageNotAssignedToUserTeamException("Stage " + stageUUID.toString() + " is assigned to " + assignedTeam);
            }
            return joinPoint.proceed();
        }

    }
}

