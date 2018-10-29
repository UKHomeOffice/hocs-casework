package uk.gov.digital.ho.hocs.casework.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.casedetails.StageDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllocatedAspectTest {

    @Mock
    private RequestData requestData;

    @Mock
    private StageDataService stageService;

    private AllocatedAspect aspect;

    private UUID stageUUID = UUID.randomUUID();

    private String userId = UUID.randomUUID().toString();

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;



    @Before
    public void setup() {
        aspect = new AllocatedAspect(requestData, stageService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void shouldCallCollaborators() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.randomUUID(), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(userId);
        aspect.validateUserAccess(proceedingJoinPoint);
        verify(stageService, times(1)).getStage(stageUUID);
        verify(requestData, times(1)).userId();
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }


    @Test
    public void shouldProceedIfUserIsAllocatedToCase() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.randomUUID(), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(userId);
        aspect.validateUserAccess(proceedingJoinPoint);
        verify(proceedingJoinPoint, times(1)).proceed();

    }

    @Test(expected = StageNotAssignedToLoggedInUserException.class)
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;

        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.randomUUID(), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(UUID.randomUUID().toString());
        aspect.validateUserAccess(proceedingJoinPoint);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = PermissionCheckException.class)
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[2];
        args[1] = "bad UUID";
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        aspect.validateUserAccess(proceedingJoinPoint);
        verify(proceedingJoinPoint, never()).proceed();

    }

}