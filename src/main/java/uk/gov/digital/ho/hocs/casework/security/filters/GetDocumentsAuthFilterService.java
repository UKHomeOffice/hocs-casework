package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;

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
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {

        if (userAccessLevel != AccessLevel.RESTRICTED_OWNER) {
            return responseEntityToFilter;
        }

        if (responseEntityToFilter.getBody().getClass() != GetDocumentsResponse.class) {
            String msg = String.format("The wrong filter has been selected for class %s", responseEntityToFilter.getBody().getClass().getSimpleName());
            log.error(msg, value(LogEvent.EXCEPTION, LogEvent.AUTH_FILTER_FAILURE));
            throw new SecurityExceptions.AuthFilterException(msg, LogEvent.AUTH_FILTER_FAILURE);
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
