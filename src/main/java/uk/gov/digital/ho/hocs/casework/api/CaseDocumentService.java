package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class CaseDocumentService {

    private final CaseDataRepository caseDataRepository;
    private final DocumentClient documentClient;
    protected final InfoClient infoClient;
    protected final ObjectMapper objectMapper;

    private static final String DRAFT_DOCUMENT_FIELD_NAME = "DraftDocuments";
    private static final String PRIMARY_DRAFT_LABEL = "Primary Draft";

    @Autowired
    public CaseDocumentService(
            CaseDataRepository caseDataRepository,
            DocumentClient documentClient,
            InfoClient infoClient,
            ObjectMapper objectMapper) {
        this.caseDataRepository = caseDataRepository;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.objectMapper = objectMapper;
    }

    public GetDocumentsResponse getDocuments(UUID caseUUID, String type) {
        log.debug("Getting documents for Case: {} with type: {}", caseUUID, type);
        GetDocumentsResponse getDocumentsResponse = documentClient.getDocuments(caseUUID, type);

        CaseData caseData = caseDataRepository.findAnyByUuid(caseUUID);

        enrichDocumentsResponse(getDocumentsResponse, caseData);

        getDocumentsResponse.setDocumentTags(infoClient.getDocumentTags(caseData.getType()));

        log.info("Got {} documents and {} document tags for Case: {} with type: {}", getDocumentsResponse.getDocumentDtos().size(), getDocumentsResponse.getDocumentTags().size(), caseUUID, value(EVENT, CASE_DOCUMENTS_RETRIEVED));
        return getDocumentsResponse;
    }

    public GetDocumentsResponse getDocumentsForAction(UUID caseUUID, UUID actionDataUuid, String type) {
        log.debug("Getting documents for Case: {}, type {}, and action: {}", caseUUID, type, actionDataUuid);
        GetDocumentsResponse getDocumentsResponse =
                documentClient.getDocumentsForAction(caseUUID, actionDataUuid, type);

        CaseData caseData = caseDataRepository.findAnyByUuid(caseUUID);

        enrichDocumentsResponse(getDocumentsResponse, caseData);

        getDocumentsResponse.setDocumentTags(List.of(type));

        log.info("Got {} documents and {} document tags for Case: {}, type: {}, and action: {}",
                getDocumentsResponse.getDocumentDtos().size(),
                getDocumentsResponse.getDocumentTags().size(),
                caseUUID,
                type,
                actionDataUuid,
                value(EVENT, CASE_DOCUMENTS_RETRIEVED));
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

    private void enrichDocumentsResponse(GetDocumentsResponse getDocumentsResponse, CaseData caseData) {
        String caseType = caseData.getType();
        Map<String, String> data = caseData.getDataMap(objectMapper);

        for (DocumentDto documentDto : getDocumentsResponse.getDocumentDtos()) {
            updateDocumentLabels(documentDto, data);
        }
    }

    private void updateDocumentLabels(DocumentDto documentDto, Map<String, String> caseData) {
        if (!CollectionUtils.isEmpty(caseData) && caseData.containsKey(DRAFT_DOCUMENT_FIELD_NAME)) {
            if (documentDto.getUuid().toString().equals(caseData.get(DRAFT_DOCUMENT_FIELD_NAME))) {
                documentDto.addLabel(PRIMARY_DRAFT_LABEL);
            }
        }
    }

}