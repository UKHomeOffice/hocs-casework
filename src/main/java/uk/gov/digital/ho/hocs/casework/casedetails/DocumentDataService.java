package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDocumentFromQueueRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DocumentRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class DocumentDataService {

    private final AuditService auditService;
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentDataService(DocumentRepository documentRepository,
                               AuditService auditService) {
        this.documentRepository = documentRepository;
        this.auditService = auditService;
    }

    @Transactional
    DocumentData createDocument(UUID caseUUID, String displayName, DocumentType type) {
        log.debug("Creating Document: {}, Case UUID: {}", displayName, caseUUID);
        DocumentData documentData = new DocumentData(caseUUID, type, displayName);
        documentRepository.save(documentData);
        auditService.writeCreateDocumentEvent(documentData);
        log.info("Created Document: {}, Case UUID: {}", documentData.getUuid(), documentData.getCaseUUID());
        return documentData;
    }

    @Transactional
    public void updateDocument(UUID documentUUID, DocumentStatus status, String fileLink, String pdfLink) {
        log.debug("Updating Document: {}", documentUUID);
        DocumentData documentData = documentRepository.findByUuid(documentUUID);
        if (documentData != null) {
            documentData.update(fileLink, pdfLink, status);
            documentRepository.save(documentData);
            auditService.writeUpdateDocumentEvent(documentData);
            log.info("Updated Document: {}", documentData.getUuid());
        } else {
            throw new EntityNotFoundException("Document UUID: %s, not found!", documentUUID);
        }
    }

    @Transactional
    public void updateDocumentFromQueue(UpdateDocumentFromQueueRequest request) {
        updateDocument(request.getUuid(), request.getStatus(), request.getFileLink(), request.getPdfLink());
    }
}