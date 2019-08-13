package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class CaseDocumentResource {

    private final CaseDocumentService caseDocumentService;

    @Autowired
    public CaseDocumentResource(CaseDocumentService caseDocumentService) {
        this.caseDocumentService = caseDocumentService;
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/document/reference/{caseUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<GetDocumentsResponse> getDocumentsForCase(@PathVariable UUID caseUUID, @RequestParam(name = "type", required = false) String type) {
        return ResponseEntity.ok(caseDocumentService.getDocuments(caseUUID, type));
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @DeleteMapping(value = "/case/{caseUUID}/document/{documentUUID}")
    public ResponseEntity<String> deleteDocument(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID) {
        caseDocumentService.deleteDocument(documentUUID);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/document/{documentUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DocumentDto> getDocumentResourceLocation(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID) {
        return ResponseEntity.ok(caseDocumentService.getDocument(documentUUID));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/document/{documentUUID}/file", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ByteArrayResource> getCaseDocumentFile(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID) {
        S3Document document = caseDocumentService.getDocumentFile(documentUUID);

        ByteArrayResource resource = new ByteArrayResource(document.getData());
        MediaType mediaType = MediaType.valueOf(document.getMimeType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + document.getOriginalFilename())
                .contentType(mediaType)
                .contentLength(document.getData().length)
                .body(resource);
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/document/{documentUUID}/pdf", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ByteArrayResource> getCaseDocumentPdf(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID) {
        S3Document document = caseDocumentService.getDocumentPdf(documentUUID);

        ByteArrayResource resource = new ByteArrayResource(document.getData());
        MediaType mediaType = MediaType.valueOf(document.getMimeType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + document.getOriginalFilename())
                .contentType(mediaType)
                .contentLength(document.getData().length)
                .body(resource);
    }

}
