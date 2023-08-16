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
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_FOUND;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationAspectTest {

    @Mock
    private UserPermissionsService userService;

    @Mock
    private CaseDataService caseService;

    @Mock
    private Authorised annotation;

    private AuthorisationAspect aspect;

    private final UUID caseUUID = UUID.randomUUID();

    private final String caseReference = "COMP/12345678/23";

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Before
    public void setup() {

        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        aspect = new AuthorisationAspect(caseService, userService);
    }

    @Test
    public void shouldCaseServiceLookupWhenExistingCase() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(caseService.getCaseType(caseUUID)).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(caseService, times(1)).getCaseType(caseUUID);
        verify(userService, times(1)).getMaxAccessLevel(type);
        verify(proceedingJoinPoint, atLeast(1)).getArgs();

        verifyNoMoreInteractions(caseService);
    }

    @Test
    public void shouldNotCallCaseServiceWhenNewCase() throws Throwable {
        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Object[] args = new Object[1];
        args[0] = new CreateCaseRequest(type.getDisplayCode(), new HashMap<>(), LocalDate.now(), null);

        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(caseService, never()).getCase(caseUUID);
        verify(userService, times(1)).getMaxAccessLevel(type.getDisplayCode());
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldProceedIfUserHasPermission() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(caseService, times(1)).getCaseType(caseUUID);

        verifyNoMoreInteractions(caseService);
    }

    @Test
    public void shouldProceedIfUserHasPermissionBasedOnCaseReference() throws Throwable {
        String type = "COMP";
        Object[] args = new Object[1];
        args[0] = caseReference;
        CaseData caseData = mock(CaseData.class);

        when(caseService.getCaseDataByReference(caseReference)).thenReturn(caseData);
        when(caseData.getType()).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(caseService, times(1)).getCaseDataByReference(caseReference);
        verify(userService, times(1)).getMaxAccessLevel(type);

        verifyNoMoreInteractions(caseService);
    }

    @Test
    public void shouldNotProceedIfCaseReferenceIsNotValid() throws Throwable {
        Object[] args = new Object[1];
        args[0] = caseReference;

        ApplicationExceptions.EntityNotFoundException exception =
            new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseReference), CASE_NOT_FOUND);
        when(caseService.getCaseDataByReference(caseReference)).thenThrow(exception);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint, annotation))
            .isInstanceOf(SecurityExceptions.PermissionCheckException.class)
            .hasMessageContaining("Reference is not a valid case: %s".formatted(caseReference));

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseService, times(1)).getCaseDataByReference(caseReference);

        verifyNoMoreInteractions(caseService);
    }

    @Test
    public void shouldProceedIfUserIsInAssignedTeamAndRequiredPermissionIsRead() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        UUID teamUUID = UUID.randomUUID();
        CaseData caseData = mock(CaseData.class);

        when(userService.getUserTeams()).thenReturn(Set.of(teamUUID));
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(caseService.getCaseTeams(caseUUID)).thenReturn(Set.of(teamUUID));
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(caseService, times(1)).getCaseType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }

    @Test
    public void shouldNotProceedIfUserIsInAssignedTeamAndRequiredPermissionIsWrite() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        CaseData caseData = mock(CaseData.class);

        UUID teamUUID = UUID.randomUUID();

        when(userService.getUserTeams()).thenReturn(Set.of(teamUUID));
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.WRITE);

        assertThatThrownBy(() -> {aspect.validateUserAccess(proceedingJoinPoint, annotation);}).isInstanceOf(
            SecurityExceptions.PermissionCheckException.class).hasMessageContaining(
            "User does not have access to the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseService, times(1)).getCaseType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }

    @Test
    public void shouldNotProceedIfUserIsNotInAssignedTeamAndRequiredPermissionIsRead() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        CaseData caseData = mock(CaseData.class);

        UUID teamUUID = UUID.randomUUID();
        ActiveStage activeStage = new ActiveStage(1L, UUID.randomUUID(), LocalDateTime.now(), "MIN", LocalDate.now(),
            LocalDate.now(), UUID.randomUUID(), caseUUID, teamUUID, UUID.randomUUID());

        when(userService.getUserTeams()).thenReturn(Set.of(UUID.randomUUID()));
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint, annotation)).isInstanceOf(
            SecurityExceptions.PermissionCheckException.class).hasMessageContaining(
            "ser does not have access to the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseService, times(1)).getCaseType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }

    @Test
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        CaseData caseData = mock(CaseData.class);

        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.UNSET);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint, annotation)).isInstanceOf(
            SecurityExceptions.PermissionCheckException.class).hasMessageContaining(
            "User does not have access to the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseService, times(1)).getCaseType(caseUUID);
        verify(caseService, times(1)).getCaseTeams(caseUUID);
    }

    @Test
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[1];
        args[0] = 1;
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        assertThatThrownBy(() -> {aspect.validateUserAccess(proceedingJoinPoint, annotation);}).isInstanceOf(
            SecurityExceptions.PermissionCheckException.class).hasMessageContaining(
            "Unable parse method parameters for type java.lang.Integer");

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
        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(annotation, times(2)).accessLevel();
        verify(caseService, times(1)).getCaseType(caseUUID);

        verifyNoMoreInteractions(caseService);

    }

    @Test
    public void shouldGetRequestedPermissionTypeFromRequestWhenUNSET() throws Throwable {
        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.UNSET);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(annotation, times(1)).accessLevel();
        verify(caseService, times(1)).getCaseType(caseUUID);

        verifyNoMoreInteractions(caseService);

    }
}
