package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesData;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;

@Service
@Profile("reports")
public class OpenCasesDataMapper {

    private final UserNameValueMapper userNameValueMapper;

    private final TeamNameValueMapper teamNameValueMapper;

    private final StageNameValueMapper stageNameValueMapper;

    @Autowired
    public OpenCasesDataMapper(
        UserNameValueMapper userNameValueMapper,
        TeamNameValueMapper teamNameValueMapper,
        StageNameValueMapper stageNameValueMapper
                              ) {
        this.userNameValueMapper = userNameValueMapper;
        this.teamNameValueMapper = teamNameValueMapper;
        this.stageNameValueMapper = stageNameValueMapper;
    }

    public OpenCasesRow mapDataToRow(OpenCasesData data) {
        return new OpenCasesRow(data.getCaseUUID(), data.getCaseReference(), data.getBusinessArea(), data.getAge(),
            data.getCaseDeadline(), data.getStageUUID(), data.getStageType(), data.getAssignedUserUUID(),
            data.getAssignedTeamUUID(), data.getUserGroup(), data.getOutsideServiceStandard(),
            userNameValueMapper.map(data.getAssignedUserUUID()).orElse(null),
            teamNameValueMapper.map(data.getAssignedTeamUUID()).orElse(null),
            stageNameValueMapper.map(data.getStageType()).orElse(data.getStageType())
        );
    }

}
