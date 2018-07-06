package uk.gov.digital.ho.hocs.casework.caseDetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentStatus;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Slf4j
@Service
public class DocumentService {

    public static final String DOCUMENT_DATA_NOT_FOUND = "DocumentData not found!";
    private final AuditService auditService;
    private final DocumentRepository documentRepository;
    private final RequestData requestData;

    @Autowired
    public DocumentService(AuditService auditService, DocumentRepository documentRepository, RequestData requestData) {
        this.auditService = auditService;
        this.documentRepository = documentRepository;
        this.requestData = requestData;
    }

    @Transactional
    public void addDocument(UUID caseUUID, DocumentSummary documentSummary) throws EntityCreationException {
        if (!isNullOrEmpty(caseUUID) && documentSummary != null && documentSummary.getDocumentUUID() !=null) {
            log.info("Requesting Create DocumentData: {} for case {}, User: {}", documentSummary.getDocumentUUID(), caseUUID, requestData.username());
            DocumentData documentData = new DocumentData(caseUUID, documentSummary);
            documentRepository.save(documentData);
            auditService.writeAddDocumentEvent(documentData);
            log.debug("Created DocumentData {} for case {}", documentData.getDocumentUUID(), documentData.getCaseUUID());
        } else {
            throw new EntityCreationException("Failed to create documentData details, CaseUUID or DocumentUUID was null");
        }
    }

    //TODO - handle multiple documents where some are in already in system - should save documents which are not already in system and return 200
    @Transactional
    public void addDocuments(UUID caseUUID, List<DocumentSummary> documentSummaries) throws EntityCreationException {
        if (!isNullOrEmpty(caseUUID) && documentSummaries != null && documentSummaries.stream().allMatch(d-> d.getDocumentUUID() != null)) {
            log.info("Requesting Bulk Create DocumentData for case {}, User: {}", caseUUID, requestData.username());
            List<DocumentData> documentDatums = documentSummaries.stream().map(d -> new DocumentData(caseUUID,d)).collect(Collectors.toList());
            documentRepository.saveAll(documentDatums);
            auditService.writeAddDocumentEvents(documentDatums);
            log.debug("Created Bulk DocumentData for case {}", caseUUID);
        } else {
            throw new EntityCreationException("Failed to create documentData details, CaseUUID or DocumentUUID was null");
        }
    }

    @Transactional
    public void updateDocument(UUID caseUUID, UUID documentUUID, String s3OrigLink, String s3PdfLink, DocumentStatus status) throws EntityCreationException, EntityNotFoundException {
        log.info("Requesting Update Case DocumentData: {} for case {}, User: {}", documentUUID, caseUUID, requestData.username());
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(documentUUID)) {
            DocumentData documentData = documentRepository.findByDocumentUUID(documentUUID);
            if (documentData != null) {
                documentData.setS3OrigLink(s3OrigLink);
                documentData.setS3PdfLink(s3PdfLink);
                documentData.setStatus(status);
                documentRepository.save(documentData);
                auditService.writeUpdateDocumentEvent(documentData);
                log.info("Update DocumentData {} for case {}", documentData.getDocumentUUID(), documentData.getCaseUUID());
            } else {
                throw new EntityNotFoundException(DOCUMENT_DATA_NOT_FOUND);
            }
        } else {
            throw new EntityCreationException("Failed to create document details, CaseUUID or DocumentUUID was null");
        }
    }

    @Transactional
    public void deleteDocument(UUID caseUUID, UUID documentUUID) throws
            EntityCreationException, EntityNotFoundException {
        log.info("Requesting Delete DocumentData: {} for case {}, User: {}", documentUUID, caseUUID, requestData.username());
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(documentUUID)) {
            DocumentData documentData = documentRepository.findByDocumentUUID(documentUUID);
            if (documentData != null) {
                documentData.setDeleted();
                documentRepository.save(documentData);
                auditService.writeDeleteDocumentEvent(documentData);
                log.info("Set Deleted to TRUE for DocumentData {} for case {}", documentData.getDocumentUUID(), documentData.getCaseUUID());
            } else {
                throw new EntityNotFoundException(DOCUMENT_DATA_NOT_FOUND);
            }
        } else {
            throw new EntityCreationException("Failed to delete document details, CaseUUID or DocumentUUID was null");
        }
    }

    @Transactional
    public void unDeleteDocument(UUID caseUUID, UUID documentUUID) throws
            EntityCreationException, EntityNotFoundException {
        log.info("Requesting unDelete DocumentData: {} for case {}, User: {}", documentUUID, caseUUID, requestData.username());
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(documentUUID)) {
            DocumentData documentData = documentRepository.findByDocumentUUID(documentUUID);
            if (documentData != null) {
                documentData.setUnDeleted();
                documentRepository.save(documentData);
                auditService.writeUndeleteDocumentEvent(documentData);
                log.info("Set Deleted to FALSE for DocumentData {} for case {}", documentData.getDocumentUUID(), documentData.getCaseUUID());
            } else {
                throw new EntityNotFoundException(DOCUMENT_DATA_NOT_FOUND);
            }
        } else {
            throw new EntityCreationException("Failed to unDelete document details, CaseUUID or DocumentUUID was null");
        }
    }
}