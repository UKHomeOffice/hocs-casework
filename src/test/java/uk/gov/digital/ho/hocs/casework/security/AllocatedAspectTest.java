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
import uk.gov.digital.ho.hocs.casework.api.CaseDataTypeService;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllocatedAspectTest {

    @Mock
    private StageService stageService;

    @Mock
    private UserPermissionsService userService;

    @Mock
    private CaseDataTypeService caseDataTypeService;

    @Mock
    Allocated annotation;

    private AllocatedAspect aspect;

    private final UUID stageUUID = UUID.randomUUID();
    private final UUID caseUUID = UUID.randomUUID();

    private final UUID userId = UUID.randomUUID();
    private final UUID teamId = UUID.randomUUID();
    private final UUID transitionNoteUUID = UUID.randomUUID();

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

        Stage stage = new Stage(UUID.randomUUID(), "DCU_DTEN_DATA_INPUT", teamId, null, transitionNoteUUID);
        stage.setUserUUID(userId);

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;
        when(stageService.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getUserId()).thenReturn(userId);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(CaseDataTypeFactory.from("ANY", "01"));

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(stageService).getStageUser(caseUUID, stageUUID);
        verify(userService).getUserId();
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldCallCollaboratorsForTeam() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;
        when(stageService.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getExpandedUserTeams()).thenReturn(new HashSet<>(List.of(teamId)));

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(userService).getExpandedUserTeams();
        verify(stageService).getStageTeam(caseUUID, stageUUID);
        verify(userService, never()).getUserId();
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldProceedIfUserIsAllocatedToCase() throws Throwable {

        Stage stage = new Stage(UUID.randomUUID(), "DCU_DTEN_DATA_INPUT", teamId, null, transitionNoteUUID);
        stage.setUuid(userId);

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;
        when(stageService.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getUserId()).thenReturn(userId);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(CaseDataTypeFactory.from("ANY", "01"));

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint).proceed();

    }

    @Test
    public void shouldProceedIfTeamIsAllocatedToCase() throws Throwable {

        Object[] args = new Object[2];
        args[0] = caseUUID;
        args[1] = stageUUID;

        when(stageService.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getExpandedUserTeams()).thenReturn(new HashSet<>(List.of(teamId)));
        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint).proceed();

    }

    @Test(expected = SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getUserId()).thenReturn(UUID.randomUUID());
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(CaseDataTypeFactory.from("ANY", "01"));

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.StageNotAssignedToUserTeamException.class)
    public void shouldNotProceedIfTeamDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getExpandedUserTeams()).thenReturn(Set.of(UUID.randomUUID()));
        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[2];
        args[1] = "bad UUID";
        args[0] = caseUUID;
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test
    public void shouldProceedIfAdminUser() throws Throwable {

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(userService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(Set.of(caseDataType.getDisplayCode()));

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint).proceed();
    }

    @Test(expected = SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public void shouldNotProceedIfNotAdminUserAndStageNotAssignedToLoggedInUser() throws Throwable {
        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        String someOtherCaseType = "SOME_OTHER_CASE_TYPE";
        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(userService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(Set.of(someOtherCaseType));

        when(userService.getUserId()).thenReturn(UUID.randomUUID());
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();
    }

    @Test
    public void shouldProceedIfNotAdminUserAndTeamIsAllocatedToCase() throws Throwable {
        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        String someOtherCaseType = "SOME_OTHER_CASE_TYPE";
        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(userService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(Set.of(someOtherCaseType));

        when(stageService.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getExpandedUserTeams()).thenReturn(new HashSet<>(List.of(teamId)));

        aspect = new AllocatedAspect(stageService, userService, caseDataTypeService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint).proceed();
    }
}
