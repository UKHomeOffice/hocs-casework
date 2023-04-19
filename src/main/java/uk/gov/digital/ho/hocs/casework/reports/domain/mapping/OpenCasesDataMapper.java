package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesData;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;

@Service
@Slf4j
@Profile("reporting")
public class OpenCasesDataMapper {

    private final UserNameValueMapper userNameValueMapper;

    private final TeamNameValueMapper teamNameValueMapper;

    private final StageNameValueMapper stageNameValueMapper;

    private final ExemptionDatesAgeAdjustmentLookup exemptionDatesAgeAdjustmentLookup;

    public OpenCasesDataMapper(
        UserNameValueMapper userNameValueMapper,
        TeamNameValueMapper teamNameValueMapper,
        StageNameValueMapper stageNameValueMapper,
        ExemptionDatesAgeAdjustmentLookup exemptionDatesAgeAdjustmentLookup
                              ) {
        this.userNameValueMapper = userNameValueMapper;
        this.teamNameValueMapper = teamNameValueMapper;
        this.stageNameValueMapper = stageNameValueMapper;
        this.exemptionDatesAgeAdjustmentLookup = exemptionDatesAgeAdjustmentLookup;
    }

    public OpenCasesRow mapDataToRow(OpenCasesData data) {
        return new OpenCasesRow(data.getCaseUUID(), data.getCaseReference(), data.getBusinessArea(),
            data.getDateCreated(), adjustAge(data), data.getCaseDeadline(), data.getStageUUID(), data.getStageType(),
            data.getAssignedUserUUID(), data.getAssignedTeamUUID(),
            data.getOutsideServiceStandard(), userNameValueMapper.map(data.getAssignedUserUUID()).orElse(null),
            teamNameValueMapper.map(data.getAssignedTeamUUID()).orElse(null),
            stageNameValueMapper.map(data.getStageType()).orElse(data.getStageType())
        );
    }

    private int adjustAge(OpenCasesData data) {
        int toAdjust = exemptionDatesAgeAdjustmentLookup.getExemptionDatesForCaseTypeSince(data.getCaseType(), data.getDateCreated());

        return data.getAge() - toAdjust;
    }

}
