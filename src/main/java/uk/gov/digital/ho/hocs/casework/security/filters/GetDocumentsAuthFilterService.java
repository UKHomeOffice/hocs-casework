package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUTH_FILTER_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

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
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter,
                              AccessLevel userAccessLevel,
                              Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {

        GetDocumentsResponse getDocumentsResponse = verifyAndReturnAsObjectType(responseEntityToFilter,
            GetDocumentsResponse.class);
        UUID userId = userPermissionsService.getUserId();

        log.debug("Filtering response by userId {} for list of documents.", userId);

        Set<DocumentDto> docsToReturn = new HashSet<>();
        if (getDocumentsResponse.getDocumentDtos()!=null) {
            getDocumentsResponse.getDocumentDtos().forEach((DocumentDto documentDto) -> {
                if (documentDto.getUploadOwnerUUID()!=null && documentDto.getUploadOwnerUUID().equals(userId)) {
                    docsToReturn.add(documentDto);
                }
            });
        }

        SettableDocumentDtosSetGetDocumentsResponse response = new SettableDocumentDtosSetGetDocumentsResponse(
            getDocumentsResponse);
        response.setDocumentDtos(docsToReturn);

        log.info("Issuing filtered GetDocumentsResponse for userId: {}", userPermissionsService.getUserId(),
            value(EVENT, AUTH_FILTER_SUCCESS));
        return new ResponseEntity<GetDocumentsResponse>(response, responseEntityToFilter.getStatusCode());
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
