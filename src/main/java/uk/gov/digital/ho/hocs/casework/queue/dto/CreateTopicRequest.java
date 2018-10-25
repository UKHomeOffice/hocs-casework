package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.CreateTopicRequest.CREATE_TOPIC_COMMAND;

@Getter
@JsonTypeName(CREATE_TOPIC_COMMAND)
public class CreateTopicRequest extends HocsCommand {

    static final String CREATE_TOPIC_COMMAND = "create_topic_command";

    private UUID caseUUID;

    private UUID topicNameUUID;

    private String topicName;

    @JsonCreator
    public CreateTopicRequest(
            @JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
            @JsonProperty(value = "topicName", required = true) String topicName,
            @JsonProperty(value = "topicNameUUID", required = true) UUID topicNameUUID) {
        super(CREATE_TOPIC_COMMAND);
        this.caseUUID = caseUUID;
        this.topicName = topicName;
        this.topicNameUUID = topicNameUUID;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        topicService.createTopic(caseUUID, topicNameUUID, topicName);
    }

}