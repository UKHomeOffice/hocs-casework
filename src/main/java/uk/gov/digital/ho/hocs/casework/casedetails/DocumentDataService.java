package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDocumentFromQueueRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DocumentRepository;

import javax.transaction.Transactional;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

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
    public DocumentData createDocument(UUID caseUUID, String displayName, DocumentType type) {
        if (!isNullOrEmpty(caseUUID) && displayName != null && type != null) {
            log.info("Requesting Create {} for case {}", displayName, caseUUID);
            DocumentData documentData = new DocumentData(caseUUID, displayName, type);
            documentRepository.save(documentData);
            auditService.writeAddDocumentEvent(documentData);
            log.debug("Created Document {} - {} for case {}", documentData.getName(), documentData.getUuid(), documentData.getCaseUUID());
            return documentData;
        } else {
            throw new EntityCreationException("Failed to create documentData details, CaseUUID or DisplayName or type was null!");
        }
    }

    @Transactional
    public void updateDocumentFromQueue(UpdateDocumentFromQueueRequest request) {
        updateDocument(request.getCaseUUID(), request.getUuid(), request.getStatus(), request.getFileLink(), request.getPdfLink());
    }

    @Transactional
    public DocumentData updateDocument(UUID caseUUID, UUID documentUUID, DocumentStatus status, String fileLink, String pdfLink) {
        log.info("Requesting Update Case DocumentData: {} for case {}", documentUUID, caseUUID);
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(documentUUID)) {
            DocumentData documentData = documentRepository.findByUuid(documentUUID);
            if (documentData != null) {
                documentData.setStatus(status);
                documentData.setFileLink(fileLink);
                documentData.setPdfLink(pdfLink);
                documentRepository.save(documentData);
                auditService.writeUpdateDocumentEvent(documentData);
                log.info("Updated DocumentData {} for case {}", documentData.getUuid(), documentData.getCaseUUID());
                return documentData;
            } else {
                throw new EntityNotFoundException("Update Case DocumentData Failed, DocumentData not Found!");
            }
        } else {
            throw new EntityCreationException("Failed to create document details, CaseUUID or DocumentUUID was null!");
        }
    }

}