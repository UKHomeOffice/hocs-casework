package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class CaseDocumentService {

    private final CaseDataRepository caseDataRepository;
    private final DocumentClient documentClient;
    protected final InfoClient infoClient;

    @Autowired
    public CaseDocumentService(
            CaseDataRepository caseDataRepository,
            DocumentClient documentClient,
            InfoClient infoClient) {
        this.caseDataRepository = caseDataRepository;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
    }

    public GetDocumentsResponse getDocuments(UUID caseUUID, String type) {
        log.debug("Getting documents for Case: {} with type: {}", caseUUID, type);
        GetDocumentsResponse getDocumentsResponse = documentClient.getDocuments(caseUUID, type);
        String caseType = caseDataRepository.getCaseType(caseUUID);
        getDocumentsResponse.setDocumentTags(infoClient.getDocumentTags(caseType));
        log.info("Got {} documents and {} document tags for Case: {} with type: {}", getDocumentsResponse.getDocumentDtos().size(), getDocumentsResponse.getDocumentTags().size(), caseUUID, value(EVENT, CASE_DOCUMENTS_RETRIEVED));
        return getDocumentsResponse;
    }

    public DocumentDto getDocument(UUID documentUUID) {
        log.debug("Getting document for id: {}", documentUUID);
        DocumentDto documentDto = documentClient.getDocument(documentUUID);
        log.debug("Got document for id: {}", documentUUID);
        return documentDto;
    }

    public void deleteDocument(UUID documentUUID) {
        log.debug("About to delete document for id: {}", documentUUID);
        documentClient.deleteDocument(documentUUID);
        log.debug("Document deleted id: {}", documentUUID);
    }

    public S3Document getDocumentFile(UUID documentUUID) {
        log.debug("Getting case document for id {}", documentUUID);
        S3Document document = documentClient.getDocumentFile(documentUUID);
        log.info("Got document with id {}", documentUUID, value(EVENT, CASE_DOCUMENT_RETRIEVED));
        return document;
    }

    public S3Document getDocumentPdf(UUID documentUUID) {
        log.debug("Getting case document pdf for id {}", documentUUID);
        S3Document document = documentClient.getDocumentPdf(documentUUID);
        log.info("Got document pdf with id {}", documentUUID, value(EVENT, CASE_DOCUMENT_PDF_RETRIEVED));
        return document;
    }

}