package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class StageTagsDecoratorImpl implements StageTagsDecorator {

    private ObjectMapper objectMapper;

    private static final String HOME_SEC_REPLY_FIELD_NAME = "HomeSecReply";
    private static final String PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME = "PrivateOfficeOverridePOTeamUUID";
    private static final String OVERRIDE_PO_TEAM_UUID_FIELD_NAME = "OverridePOTeamUUID";
    private static final String PO_TEAM_UUID_FIELD_NAME = "POTeamUUID";
    private static final String HOME_SEC_PO_TEAM_UUID = "3d2c7893-92c5-4347-804a-8826f06f0c9d";

    @Autowired
    public StageTagsDecoratorImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void decorateTags(Stage stage) {
        ArrayList<String> tags = new ArrayList<>();
        if (this.addHomeSecReplyTag(stage)) {
            tags.add(StageTags.HOME_SEC_REPLY_TAG);
        }
        stage.setTag(tags);
    }

    private Boolean hasDataFieldAndValue(Stage stage, String field, String value) {
        Map<String, String> data = new HashMap<>(stage.getDataMap(objectMapper));

        if (data.get(field) != null) {
            return data.get(field).equals(value);
        }

        return false;
    }

    private Boolean addHomeSecReplyTag(Stage stage) {
        if(hasDataFieldAndValue(stage, HOME_SEC_REPLY_FIELD_NAME, "TRUE")) {
            return  isAtDcuMarkup(stage) ||
                    isHomeSecReplyNoPoTeam(stage) ||
                    hasHomeSecPoTeam(stage);
        }

        return false;
    }

    private Boolean isAtDcuMarkup(Stage stage) {
        return stage.getStageType().equals("DCU_MIN_MARKUP");
    }

    private Boolean isHomeSecReplyNoPoTeam(Stage stage) {
        Map<String, String> data = new HashMap<>(stage.getDataMap(objectMapper));

        if (data.get(PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME) != null) {
            return false;
        }

        return (
                hasDataFieldAndValue(stage, PO_TEAM_UUID_FIELD_NAME, "") &&
                hasDataFieldAndValue(stage, OVERRIDE_PO_TEAM_UUID_FIELD_NAME, "")
        );
    }

    private Boolean hasHomeSecPoTeam(Stage stage) {
        String poTeamUuid = this.highestPrecedentPrivateOfficeTeamUuid(stage);

        return poTeamUuid.equals(HOME_SEC_PO_TEAM_UUID);
    }

    private String highestPrecedentPrivateOfficeTeamUuid(Stage stage) {
        Map<String, String> data = new HashMap<>(stage.getDataMap(objectMapper));

        String[] homeSecReplyCasePoTeamPrecedence = {
                PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME,
                OVERRIDE_PO_TEAM_UUID_FIELD_NAME,
                PO_TEAM_UUID_FIELD_NAME
        };
        for (String poTeamUuid: homeSecReplyCasePoTeamPrecedence) {
            if(data.get(poTeamUuid) != null) {
                if (!data.get(poTeamUuid).equals("")) {
                    return data.get(poTeamUuid);
                }
            }
        }

        return "";
    }
}
