package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CopyDocumentsRequest;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseTypeDocumentTagRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DOCUMENTS_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DOCUMENT_PDF_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DOCUMENT_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Service
@Slf4j
public class CaseDocumentService {

    private final CaseDataRepository caseDataRepository;

    private final DocumentClient documentClient;

    private final CaseTypeDocumentTagRepository caseTypeDocumentTagRepository;

    private static final String DRAFT_DOCUMENT_FIELD_NAME = "DraftDocuments";

    private static final String PRIMARY_DRAFT_LABEL = "Primary Draft";

    public CaseDocumentService(CaseDataRepository caseDataRepository,
                               CaseTypeDocumentTagRepository caseTypeDocumentTagRepository,
                               DocumentClient documentClient) {
        this.caseDataRepository = caseDataRepository;
        this.caseTypeDocumentTagRepository = caseTypeDocumentTagRepository;
        this.documentClient = documentClient;
    }

    public GetDocumentsResponse getDocuments(UUID caseUUID, String type) {
        log.debug("Getting documents for Case: {} with type: {}", caseUUID, type);
        GetDocumentsResponse getDocumentsResponse = documentClient.getDocuments(caseUUID, type);

        CaseData caseData = caseDataRepository.findAnyByUuid(caseUUID);
        enrichDocumentsResponse(getDocumentsResponse, caseData.getData(DRAFT_DOCUMENT_FIELD_NAME), caseData.getType());

        log.info("Got {} documents and {} document tags for Case: {} with type: {}",
            getDocumentsResponse.getDocumentDtos().size(), getDocumentsResponse.getDocumentTags().size(), caseUUID,
            type, value(EVENT, CASE_DOCUMENTS_RETRIEVED));
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

    private void enrichDocumentsResponse(GetDocumentsResponse getDocumentsResponse, String fieldName, String caseType) {
        for (DocumentDto documentDto : getDocumentsResponse.getDocumentDtos()) {
            if (documentDto.getUuid().toString().equals(fieldName)) {
                documentDto.addLabel(PRIMARY_DRAFT_LABEL);
            }
        }
        getDocumentsResponse.setDocumentTags(caseTypeDocumentTagRepository.getTagsByType(caseType));
    }

    public void copyDocuments(UUID fromUUID, UUID toUUID, String[] types) {
        CopyDocumentsRequest copyDocumentsRequest = new CopyDocumentsRequest(fromUUID, toUUID, Set.of(types));
        documentClient.copyDocuments(copyDocumentsRequest);
    }

    public List<String> getDocumentTags(UUID caseUUID) {
        String caseType = caseDataRepository.getCaseType(caseUUID);
        return caseTypeDocumentTagRepository.getTagsByType(caseType);
    }
}
