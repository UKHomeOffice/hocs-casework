package uk.gov.digital.ho.hocs.casework.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.casedetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.StageDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;

import java.util.UUID;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class AllocatedAspect {

    private RequestData requestData;
    private StageDataService stageService;

    @Around("@annotation(Secure)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID stageUUID;
        if (joinPoint.getArgs().length > 1) {
            if (joinPoint.getArgs()[1] instanceof UUID) {
                stageUUID = (UUID) joinPoint.getArgs()[1];
            } else {
                throw new PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[1].getClass().getName());
            }
        }
        else {
            throw new PermissionCheckException("Unable to check permission of method without stage UUID parameter");
        }
            UUID userId = UUID.fromString(requestData.userId());
            UUID assignedUser = stageService.getStage(stageUUID).getUserUUID();

            if (!userId.equals(assignedUser)) {
                throw new StageNotAssignedToLoggedInUserException("Stage " + stageUUID.toString() + " is assigned to " + assignedUser);
            }
            return joinPoint.proceed();
    }
}

