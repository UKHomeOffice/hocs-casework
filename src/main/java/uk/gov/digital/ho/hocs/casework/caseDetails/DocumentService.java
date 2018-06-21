package uk.gov.digital.ho.hocs.casework.caseDetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.Document;

import javax.transaction.Transactional;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Slf4j
@Service
public class DocumentService {

    private final AuditService auditService;
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(AuditService auditService, DocumentRepository documentRepository) {
        this.auditService = auditService;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public void createDocument(Document document, String userName) throws EntityCreationException {
        log.info("Requesting Create Document: {} for case {}, User: {}", document.getDocumentUUID(), document.getCaseUUID(), userName);
        if (!isNullOrEmpty(document.getCaseUUID()) && !isNullOrEmpty(document.getDocumentUUID())) {
            documentRepository.save(document);
            log.info("Created Document {} for case {}", document.getDocumentUUID(), document.getCaseUUID());
        } else {
            throw new EntityCreationException("Failed to create document details, CaseUUID was null");
        }
    }

    @Transactional
    public Document updateDocument(UUID caseUUID, UUID documentUUID, String s3OrigLink, String s3PdfLink, String status, String userName) throws EntityCreationException, EntityNotFoundException {
        log.info("Requesting Update Case Document: {} for case {}, User: {}", documentUUID, caseUUID, userName);
        if (!isNullOrEmpty(documentUUID) && !isNullOrEmpty(documentUUID)) {
            Document document = documentRepository.findByDocumentUuid(documentUUID);
            if (document != null) {
                document.setS3OrigLink(s3OrigLink);
                document.setS3PdfLink(s3PdfLink);
                document.setStatus(status);
                documentRepository.save(document);
                log.info("Update Document {} for case {}", document.getDocumentUUID(), document.getCaseUUID());
                return document;
            } else {
                throw new EntityNotFoundException("Document not found!");
            }
        } else {
            throw new EntityCreationException("Failed to create document details, CaseUUID was null");
        }

    }

    @Transactional
    public Document DeleteCaseDocument(UUID caseUUID, UUID documentUUID, String userName) throws
            EntityCreationException, EntityNotFoundException {
        log.info("Requesting Delete Document: {} for case {}, User: {}", documentUUID, caseUUID, userName);
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(documentUUID)) {
            Document document = documentRepository.findByDocumentUuid(documentUUID);
            if (document != null) {
                document.setDeleted(Boolean.TRUE);
                documentRepository.save(document);
                log.info("Set Deleted to TRUE for Document {} for case {}", document.getDocumentUUID(), document.getCaseUUID());
                return document;
            } else {
                throw new EntityNotFoundException("Document not found!");
            }
        } else {
            throw new EntityCreationException("Failed to delete document details, CaseUUID or DocumentUUID was null");
        }

    }
}
