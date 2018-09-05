package uk.gov.digital.ho.hocs.casework.casedetails.queuedto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.casedetails.queuedto.UpdateDocumentRequest.UPDATE_DOCUMENT_COMMAND;

@Getter
@JsonTypeName(UPDATE_DOCUMENT_COMMAND)
public class UpdateDocumentRequest extends HocsCommand {

    static final String UPDATE_DOCUMENT_COMMAND = "update_document_command";

    private UUID uuid;

    private UUID caseUUID;

    private String fileLink;

    private String pdfLink;

    private DocumentStatus status;

    @JsonCreator
    public UpdateDocumentRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                 @JsonProperty(value = "uuid", required = true) UUID uuid,
                                 @JsonProperty(value = "fileLink", required = true) String fileLink,
                                 @JsonProperty(value = "pdfLink", required = true) String pdfLink,
                                 @JsonProperty(value = "status", required = true) DocumentStatus status) {
        super(UPDATE_DOCUMENT_COMMAND);
        this.uuid = uuid;
        this.caseUUID = caseUUID;
        this.fileLink = fileLink;
        this.pdfLink = pdfLink;
        this.status = status;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        documentDataService.updateDocument(uuid, status, fileLink, pdfLink);
    }
}