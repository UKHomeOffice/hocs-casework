package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
public class StageTagsDecoratorImpl implements StageTagsDecorator {

    private static final String HOME_SEC_REPLY_FIELD_NAME = "HomeSecReply";

    private static final String PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME = "PrivateOfficeOverridePOTeamUUID";

    private static final String OVERRIDE_PO_TEAM_UUID_FIELD_NAME = "OverridePOTeamUUID";

    private static final String PO_TEAM_UUID_FIELD_NAME = "POTeamUUID";

    private static final String HOME_SEC_PO_TEAM_UUID = "3d2c7893-92c5-4347-804a-8826f06f0c9d";

    private static final String OVERRIDE_PO_TEAM_NAME = "OverridePOTeamName";

    private static final String PO_TEAM_NAME = "POTeamName";

    @Override
    public ArrayList<String> decorateTags(Map<String, String> data, String stageType) {
        ArrayList<String> tags = new ArrayList<>();
        if (this.addHomeSecReplyTag(data, stageType).equals(Boolean.TRUE)) {
            tags.add(StageTags.HOME_SEC_REPLY_TAG);
        }
        return tags;
    }

    private Boolean hasDataFieldAndValue(Map<String, String> data, String field, String value) {
        if (data.containsKey(field)) {
            return data.get(field).equals(value);
        }

        return false;
    }

    private Boolean addHomeSecReplyTag(Map<String, String> data, String stageType) {
        if (hasDataFieldAndValue(data, HOME_SEC_REPLY_FIELD_NAME, "TRUE") || hasDataFieldAndValue(data,
            OVERRIDE_PO_TEAM_NAME, "Home Secretary") || hasDataFieldAndValue(data, PO_TEAM_NAME, "Home Secretary")) {
            return isAtDcuMarkup(stageType) || isHomeSecReplyNoPoTeam(data) || hasHomeSecPoTeam(data);
        }

        return false;
    }

    private Boolean isAtDcuMarkup(String stageType) {
        return stageType.equals("DCU_MIN_MARKUP");
    }

    private Boolean isHomeSecReplyNoPoTeam(Map<String, String> data) {

        if (data.get(PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME) != null) {
            return false;
        }

        return (hasDataFieldAndValue(data, PO_TEAM_UUID_FIELD_NAME, "") && hasDataFieldAndValue(data,
            OVERRIDE_PO_TEAM_UUID_FIELD_NAME, ""));
    }

    private Boolean hasHomeSecPoTeam(Map<String, String> data) {
        String poTeamUuid = this.highestPrecedentPrivateOfficeTeamUuid(data);
        return poTeamUuid.equals(HOME_SEC_PO_TEAM_UUID);
    }

    private String highestPrecedentPrivateOfficeTeamUuid(Map<String, String> data) {
        String[] homeSecReplyCasePoTeamPrecedence = { PRIVATE_OFFICE_OVERRIDE_PO_TEAM_UUID_FIELD_NAME,
            OVERRIDE_PO_TEAM_UUID_FIELD_NAME, PO_TEAM_UUID_FIELD_NAME };
        for (String poTeamUuid : homeSecReplyCasePoTeamPrecedence) {
            if (data.get(poTeamUuid) != null) {
                if (!data.get(poTeamUuid).equals("")) {
                    return data.get(poTeamUuid);
                }
            }
        }

        return "";
    }

}
