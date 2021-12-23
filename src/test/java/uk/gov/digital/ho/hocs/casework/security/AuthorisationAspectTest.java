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
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseDataTypeService;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationAspectTest {

    @Mock
    private UserPermissionsService userService;

    @Mock
    private CaseDataService caseService;

    @Mock
    private CaseDataTypeService caseDataTypeService;

    @Mock
    private Authorised annotation;

    private AuthorisationAspect aspect;

    private final UUID caseUUID = UUID.randomUUID();

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Before
    public void setup() {
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        aspect = new AuthorisationAspect(caseService, caseDataTypeService, userService);
    }

    @Test
    public void shouldCaseServiceLookupWhenExistingCase() throws Throwable {

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);
        verify(userService, times(1)).getMaxAccessLevel(caseDataType.getType());
        verify(proceedingJoinPoint, atLeast(1)).getArgs();

        verifyNoMoreInteractions(caseService);
    }

    @Test
    public void shouldNotCallCaseServiceWhenNewCase() throws Throwable {
        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = new CreateCaseRequest(caseDataType.getType(), new HashMap<>(), LocalDate.now(), null);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(caseService, never()).getCase(caseUUID);
        verify(userService, times(1)).getMaxAccessLevel(caseDataType.getType());
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }


    @Test
    public void shouldProceedIfUserHasPermission() throws Throwable {

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);

        verifyNoMoreInteractions(caseService);
    }

    @Test
    public void shouldProceedIfUserIsInAssignedTeamAndRequiredPermissionIsRead() throws Throwable {

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;
        UUID teamUUID = UUID.randomUUID();

        when(userService.getUserTeams()).thenReturn(Set.of(teamUUID));
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(caseService.getCaseTeams(caseUUID)).thenReturn(Set.of(teamUUID));

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }

    @Test
    public void shouldNotProceedIfUserIsInAssignedTeamAndRequiredPermissionIsWrite() throws Throwable {

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;

        UUID teamUUID =UUID.randomUUID();

        when(userService.getUserTeams()).thenReturn(Set.of(teamUUID));
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.WRITE);

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(SecurityExceptions.PermissionCheckException.class)
                .hasMessageContaining("User does not have access to the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }


    @Test
    public void shouldNotProceedIfUserIsNotInAssignedTeamAndRequiredPermissionIsRead() throws Throwable {

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(userService.getUserTeams()).thenReturn(Set.of(UUID.randomUUID()));
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(SecurityExceptions.PermissionCheckException.class)
                .hasMessageContaining("ser does not have access to the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }

    @Test
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;


        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(SecurityExceptions.PermissionCheckException.class)
                .hasMessageContaining("User does not have access to the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }

    @Test
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[1];
        args[0] = "bad UUID";
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(SecurityExceptions.PermissionCheckException.class)
                .hasMessageContaining("Unable parse method parameters for type java.lang.String");

        verify(proceedingJoinPoint, never()).proceed();
    }


    @Test
    public void shouldGetRequestedPermissionTypeFromRequestWhenAnnotationIsNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertThat(aspect.getAccessRequestAccessLevel()).isEqualTo(AccessLevel.SUMMARY);
    }

    @Test
    public void shouldGetRequestedPermissionTypeFromRequestWhenAnnotationIsSet() throws Throwable {
        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(annotation, times(2)).accessLevel();
        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);

        verifyNoMoreInteractions(caseService);

    }

    @Test
    public void shouldGetRequestedPermissionTypeFromRequestWhenUNSET() throws Throwable {
        CaseDataType caseDataType = CaseDataTypeFactory.from("SOME_CASE_TYPE", "01");
        Object[] args = new Object[1];
        args[0] = caseUUID;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        when(caseDataTypeService.getCaseDataType(caseUUID)).thenReturn(caseDataType);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.UNSET);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(annotation, times(1)).accessLevel();
        verify(caseDataTypeService, times(1)).getCaseDataType(caseUUID);

        verifyNoMoreInteractions(caseService);

    }

}
