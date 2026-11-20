package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.WorkInProgressData;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.WorkInProgressRow;

import java.util.Optional;

@Service
@Slf4j
@Profile("reporting")
public class WorkInProgressDataMapper {

    private final UserNameValueMapper userNameValueMapper;

    private final TeamNameValueMapper teamNameValueMapper;

    private final StageNameValueMapper stageNameValueMapper;

    public WorkInProgressDataMapper(
        UserNameValueMapper userNameValueMapper,
        TeamNameValueMapper teamNameValueMapper,
        StageNameValueMapper stageNameValueMapper
                              ) {
        this.userNameValueMapper = userNameValueMapper;
        this.teamNameValueMapper = teamNameValueMapper;
        this.stageNameValueMapper = stageNameValueMapper;
    }

    public WorkInProgressRow mapDataToRow(WorkInProgressData data) {
        Optional<String> maybeUser = userNameValueMapper.map(data.getAssignedUserUUID());
        Optional<String> maybeTeam = teamNameValueMapper.map(data.getAssignedTeamUUID());

        return new WorkInProgressRow(
            data.getCaseUUID(),
            data.getCaseReference(),
            data.getCompType(),
            data.getDateCreated(),
            data.getDateReceived(),
            data.getCaseDeadline(),
            data.getOwningCSU(),
            data.getDirectorate(),
            data.getBusinessAreaBasedOnDirectorate(),
            data.getEnquiryReason(),
            data.getPrimaryCorrespondentName(),
            data.getCaseSummary(),
            data.getSeverity(),
            data.getAssignedUserUUID(),
            data.getAssignedTeamUUID(),
            data.getStageUUID(),
            data.getStageType(),
            data.getDueWeek(),
            maybeUser.orElse(null),
            maybeTeam.orElse(null),
            stageNameValueMapper.map(data.getStageType()).orElse(data.getStageType()),
            data.getAssignedUserUUID() != null,
            maybeUser.or(() -> maybeTeam).orElse(null)
        );
    }

}
