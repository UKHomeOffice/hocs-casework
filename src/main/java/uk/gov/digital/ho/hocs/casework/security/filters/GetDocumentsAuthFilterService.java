package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class GetDocumentsAuthFilterService implements AuthFilter {

    private final UserPermissionsService userPermissionsService;

    @Autowired
    public GetDocumentsAuthFilterService(UserPermissionsService userPermissionsService) {
        this.userPermissionsService = userPermissionsService;
    }

    @Override
    public String getKey() {
        return GetDocumentsResponse.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, int userAccessLevelAsInt, Object[] collectionAsArray) throws Exception {

        if (userAccessLevelAsInt != AccessLevel.RESTRICTED_READ.getLevel()) {
            return responseEntityToFilter;
        }

        if (responseEntityToFilter.getBody() != null && responseEntityToFilter.getBody().getClass() != GetDocumentsResponse.class) {
            throw new Exception("There is something wrong with the GetDocumentsResponse Auth Filter");
        }

        GetDocumentsResponse getDocumentsResponse  = (GetDocumentsResponse) responseEntityToFilter.getBody();
        assert getDocumentsResponse != null;
        SettableDocumentDtosSetGetDocumentsResponse response  = new SettableDocumentDtosSetGetDocumentsResponse(getDocumentsResponse);

        Set<DocumentDto> docsToReturn = new HashSet<>();
        UUID userUUID = userPermissionsService.getUserId();

        if (getDocumentsResponse.getDocumentDtos() != null) {
            getDocumentsResponse.getDocumentDtos().forEach((DocumentDto documentDto) -> {
                if (documentDto.getUploadOwnerUUID().equals(userUUID)) {
                    docsToReturn.add(documentDto);
                }
            });
        }
        response.setDocumentDtos(docsToReturn);

        return new ResponseEntity<>(response, responseEntityToFilter.getStatusCode());
    }

    public static class SettableDocumentDtosSetGetDocumentsResponse extends GetDocumentsResponse {

        public SettableDocumentDtosSetGetDocumentsResponse(GetDocumentsResponse response) {
            super(response.getDocumentDtos(), response.getDocumentTags());
        }

        public void setDocumentDtos(Set<DocumentDto> documentDtos) {
            this.replaceDocumentDtos(documentDtos);
        }
    }

}
