package uk.gov.digital.ho.hocs.casework.security.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetDocumentsAuthFilterServiceTest {

    @Mock
    private UserPermissionsService userPermissionsService;

    private GetDocumentsAuthFilterService getDocumentAuthFilterService;

    @Before
    public void setUp() throws Exception {
        getDocumentAuthFilterService = new GetDocumentsAuthFilterService(userPermissionsService);
    }

    @Test(expected = SecurityExceptions.AuthFilterException.class)
    public void testShouldThrowExceptionIfFilterNotForObjectType() {

        // GIVEN
        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
                GetCaseSummaryResponse.from(
                        new CaseSummary(
                                null, null, null,
                                null, null, null,
                                null, null, null,
                                null, null, null, null)
                ));

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        // WHEN
        getDocumentAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN - Expect Exception
    }

    @Test
    public void testShouldReturnOnlyDocsUploadedByUser() {

        // GIVEN
        UUID userUUID = UUID.randomUUID();

        DocumentDto document1 = new DocumentDto(UUID.randomUUID(), null, null, "doc1", null, null, null,null, false,null, false, false);
        DocumentDto document2 = new DocumentDto(UUID.randomUUID(), null, null, "doc2", null, null, null,userUUID, false,null, false, false);
        DocumentDto document3 = new DocumentDto(UUID.randomUUID(), null, null, "doc3", null, null, null,null, false,null, false, false);
        DocumentDto document4 = new DocumentDto(UUID.randomUUID(), null, null, "doc4", null, null, null,userUUID, false,null, false, false);

        Set<DocumentDto> setOfDocumentDtos = Set.of(document1, document2, document3, document4);
        ResponseEntity<?> responseToFilter = ResponseEntity.ok(
                new GetDocumentsResponse(setOfDocumentDtos, null)
        );

        AccessLevel userAccessLevel = AccessLevel.RESTRICTED_OWNER;

        when(userPermissionsService.getUserId()).thenReturn(userUUID);

        // WHEN
        Object result = getDocumentAuthFilterService.applyFilter(responseToFilter, userAccessLevel, null);

        // THEN
        assertThat(result)
                .isNotNull()
                .isExactlyInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;

        assertThat(resultResponseEntity.getBody()).isInstanceOf(GetDocumentsResponse.class); // is actually protected subclass.

        GetDocumentsResponse getDocumentsResponse = (GetDocumentsResponse) resultResponseEntity.getBody();

        assertThat(getDocumentsResponse.getDocumentDtos().size()).isEqualTo(2);
        assertThat(getDocumentsResponse.getDocumentDtos().stream().map(DocumentDto::getDisplayName)).containsAll(Arrays.asList(document2.getDisplayName(), document4.getDisplayName()));
        assertThat(getDocumentsResponse.getDocumentDtos().stream().map(DocumentDto::getDisplayName)).doesNotContainAnyElementsOf(Arrays.asList(document3.getDisplayName(), document1.getDisplayName()));

    }

}
