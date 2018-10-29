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

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllocatedAspectTest {

    @Mock
    private RequestData requestData;

    @Mock
    private StageDataService stageService;

    @Mock
    private UserPermissionsService userService;

    @Mock
    Allocated annotation;

    private AllocatedAspect aspect;

    private UUID stageUUID = UUID.randomUUID();

    private String userId = UUID.randomUUID().toString();
    private String teamId = UUID.randomUUID().toString();



    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Before
    public void setup() {


        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void shouldCallCollaboratorsForUser() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.fromString(teamId), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(userId);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);

        aspect = new AllocatedAspect(requestData, stageService,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(stageService, times(1)).getStage(stageUUID);
        verify(requestData, times(1)).userId();
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldCallCollaboratorsForTeam() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.fromString(teamId), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getUserTeams()).thenReturn(new HashSet<>(Arrays.asList(teamId)));

        aspect = new AllocatedAspect(requestData, stageService,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(userService, times(1)).getUserTeams();
        verify(stageService, times(1)).getStage(stageUUID);
        verify(requestData, never()).userId();
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
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);
        aspect = new AllocatedAspect(requestData, stageService,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, times(1)).proceed();

    }

    @Test
    public void shouldProceedIfTeamIsAllocatedToCase() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.fromString(teamId), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getUserTeams()).thenReturn(new HashSet<>(Arrays.asList(teamId)));
        aspect = new AllocatedAspect(requestData, stageService,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, times(1)).proceed();

    }

    @Test(expected = SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;

        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.randomUUID(), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(UUID.randomUUID().toString());
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);
        aspect = new AllocatedAspect(requestData, stageService,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.StageNotAssignedToUserTeamException.class)
    public void shouldNotProceedIfTeamDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;

        when(stageService.getStage(stageUUID)).thenReturn(new StageData(UUID.randomUUID(),
                StageType.DCU_DTEN_DATA_INPUT, UUID.fromString(teamId), UUID.fromString(userId)));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getUserTeams()).thenReturn(new HashSet<String>(){{UUID.randomUUID().toString();}});
        aspect = new AllocatedAspect(requestData,stageService,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[2];
        args[1] = "bad UUID";
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        aspect = new AllocatedAspect(requestData, stageService,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }


}