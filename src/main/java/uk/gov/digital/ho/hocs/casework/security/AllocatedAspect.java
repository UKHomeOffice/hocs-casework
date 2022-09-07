package uk.gov.digital.ho.hocs.casework.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.StageService;

import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SECURITY_CASE_NOT_ALLOCATED_TO_TEAM;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SECURITY_CASE_NOT_ALLOCATED_TO_USER;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SECURITY_PARSE_ERROR;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class AllocatedAspect {

    private final StageService stageService;

    private final UserPermissionsService userService;

    private final CaseDataService caseDataService;

    @Around("@annotation(allocated)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Allocated allocated) throws Throwable {
        UUID caseUUID;
        UUID stageUUID;
        if (joinPoint.getArgs().length >= 2) {
            if (joinPoint.getArgs()[0] instanceof UUID && joinPoint.getArgs()[1] instanceof UUID) {
                caseUUID = (UUID) joinPoint.getArgs()[0];
                stageUUID = (UUID) joinPoint.getArgs()[1];
            } else {
                throw new SecurityExceptions.PermissionCheckException(
                    "Unable parse method parameters for type " + joinPoint.getArgs()[1].getClass().getName(),
                    SECURITY_PARSE_ERROR);
            }
        } else {
            throw new SecurityExceptions.PermissionCheckException(
                "Unable to check permission of method without stage UUID parameter", SECURITY_PARSE_ERROR);
        }

        if (proceedIfUserTeamIsAdminForCaseType(caseUUID)) {
            return joinPoint.proceed();
        }

        switch (allocated.allocatedTo()) {
            case USER:
                checkIfStageIsAssignedToLoggedInUser(caseUUID, stageUUID);
                break;
            case TEAM:
                checkIfStageIsAssignedToUserTeam(caseUUID, stageUUID);
                break;
            case USER_OR_TEAM:
                try {
                    checkIfStageIsAssignedToLoggedInUser(caseUUID, stageUUID);
                } catch (SecurityExceptions.StageNotAssignedToLoggedInUserException e) {
                    checkIfStageIsAssignedToUserTeam(caseUUID, stageUUID);
                }
                break;
            default:
                throw new SecurityExceptions.PermissionCheckException("Invalid Allocation type", SECURITY_PARSE_ERROR);
        }

        return joinPoint.proceed();

    }

    private void checkIfStageIsAssignedToLoggedInUser(UUID caseUUID, UUID stageUUID) {
        UUID userId = userService.getUserId();
        UUID assignedUser = stageService.getStageUser(caseUUID, stageUUID);
        if (!userId.equals(assignedUser)) {
            throw new SecurityExceptions.StageNotAssignedToLoggedInUserException(
                String.format("Stage %s is assigned to %s", stageUUID.toString(), assignedUser),
                SECURITY_CASE_NOT_ALLOCATED_TO_USER);
        }
    }

    private void checkIfStageIsAssignedToUserTeam(UUID caseUUID, UUID stageUUID) {
        Set<UUID> teams = userService.getExpandedUserTeams();
        UUID assignedTeam = stageService.getStageTeam(caseUUID, stageUUID);

        if (assignedTeam == null || !teams.contains(assignedTeam)) {
            throw new SecurityExceptions.StageNotAssignedToUserTeamException(
                String.format("Stage %s is assigned to %s", stageUUID.toString(), assignedTeam),
                SECURITY_CASE_NOT_ALLOCATED_TO_TEAM);
        }
    }

    private boolean proceedIfUserTeamIsAdminForCaseType(UUID caseUUID) {
        String stageType = caseDataService.getCaseType(caseUUID);
        Set<String> caseTypesForCaseTypeAdmin = userService.getCaseTypesIfUserTeamIsCaseTypeAdmin();
        return caseTypesForCaseTypeAdmin.contains(stageType);
    }

}
