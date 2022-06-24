package uk.gov.digital.ho.hocs.casework.client.documentclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.CopyDocumentsRequest;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Component
public class DocumentClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public DocumentClient(RestHelper restHelper,
                          @Value("${hocs.document-service}") String documentService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = documentService;
    }

    public GetDocumentsResponse getDocuments(UUID caseUUID, String type) {
        String url = type == null ? String.format("/document/reference/%s", caseUUID) : String.format("/document/reference/%s/?type=%s", caseUUID, type);

        GetDocumentsResponse documents = restHelper.get(serviceBaseURL, url, GetDocumentsResponse.class);
        log.info("Got Documents {} for Case {}", documents.getDocumentDtos(), caseUUID, value(EVENT, DOCUMENT_CLIENT_GET_DOCUMENTS_SUCCESS));

        return documents;
    }

    public DocumentDto getDocument(UUID documentUUID) {
        DocumentDto documentDto = restHelper.get(serviceBaseURL, String.format("/document/%s", documentUUID), DocumentDto.class);
        log.info("Got documentDto with id {}", documentDto.getUuid(), value(EVENT, DOCUMENT_CLIENT_GET_DOCUMENT_DTO_SUCCESS));
        return documentDto;
    }

    public void deleteDocument(UUID documentUUID) {
        restHelper.delete(serviceBaseURL, String.format("/document/%s", documentUUID));
        log.info("Document deleted {}", documentUUID, value(EVENT, DOCUMENT_CLIENT_DELETE_DOCUMENT_SUCCESS));
    }

    public S3Document getDocumentFile(UUID documentUUID) {
        S3Document document = restHelper.getFile(serviceBaseURL, String.format("/document/%s/file", documentUUID));
        log.info("Got document with length {} for id {}", document.getData().length, documentUUID, value(EVENT, DOCUMENT_CLIENT_GET_DOCUMENT_SUCCESS));
        return document;
    }

    public S3Document getDocumentPdf(UUID documentUUID) {
        S3Document document = restHelper.getFile(serviceBaseURL, String.format("/document/%s/pdf", documentUUID));
        log.info("Got document with length {} for id {}", document.getData().length, documentUUID, value(EVENT, DOCUMENT_CLIENT_GET_DOCUMENT_PDF_SUCCESS));
        return document;
    }

    public void copyDocuments(CopyDocumentsRequest copyDocumentRequest) {
        restHelper.post(serviceBaseURL, "/documents/copy", copyDocumentRequest, Void.class);
    }
}
