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
import uk.gov.digital.ho.hocs.casework.casedetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import java.util.UUID;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationAspectTest {

    @Mock
    private RequestData requestData;

    @Mock
    private UserClient userClient;

    @Mock
    private CaseDataService caseService;

    private AuthorisationAspect aspect;

    private UUID caseUUID = UUID.randomUUID();

    private String userId = UUID.randomUUID().toString();

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;



    @Before
    public void setup() {

        aspect = new AuthorisationAspect(requestData, userClient, caseService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }



    @Test
    public void shouldCallCollaboratorsWhenExistingCase() throws Throwable {

        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(caseService.getCase(caseUUID)).thenReturn(new CaseData(CaseType.MIN, 123456789L));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(userId);
        when(userClient.getUserAccess(userId, CaseType.MIN, "READ")).thenReturn(true);

        aspect.validateUserAccess(proceedingJoinPoint);

        verify(caseService, times(1)).getCase(caseUUID);
        verify(requestData, times(1)).userId();
        verify(userClient, times(1)).getUserAccess(userId, CaseType.MIN, "READ");
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldCallCollaboratorsWhenNewCase() throws Throwable {

        Object[] args = new Object[1];
        args[0] = new CaseData(CaseType.MIN, 123456789L);

          when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(userId);
        when(userClient.getUserAccess(userId, CaseType.MIN, "READ")).thenReturn(true);

        aspect.validateUserAccess(proceedingJoinPoint);

        verify(caseService, never()).getCase(caseUUID);
        verify(requestData, times(1)).userId();
        verify(userClient, times(1)).getUserAccess(userId, CaseType.MIN, "READ");
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }


    @Test
    public void shouldProceedIfUserHasPermission() throws Throwable {

        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(caseService.getCase(caseUUID)).thenReturn(new CaseData(CaseType.MIN, 123456789L));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(userId);
        when(userClient.getUserAccess(userId, CaseType.MIN, "READ")).thenReturn(true);

        aspect.validateUserAccess(proceedingJoinPoint);
        verify(proceedingJoinPoint, times(1)).proceed();

    }

    @Test(expected = PermissionCheckException.class)
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(caseService.getCase(caseUUID)).thenReturn(new CaseData(CaseType.MIN, 123456789L));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(requestData.userId()).thenReturn(userId);
        when(userClient.getUserAccess(userId, CaseType.MIN, "READ")).thenReturn(false);

        aspect.validateUserAccess(proceedingJoinPoint);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = PermissionCheckException.class)
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[1];
        args[0] = "bad UUID";
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        aspect.validateUserAccess(proceedingJoinPoint);
        verify(proceedingJoinPoint, never()).proceed();

    }

}