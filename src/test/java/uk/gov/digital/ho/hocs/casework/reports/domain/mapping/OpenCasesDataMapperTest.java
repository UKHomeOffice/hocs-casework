package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesData;
import uk.gov.digital.ho.hocs.casework.reports.domain.reports.OpenCasesRow;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("reports")
public class OpenCasesDataMapperTest {

    private static final UUID SAMPLE_STAGE_UUID = UUID.fromString("ae2008cc-b51c-4811-bf59-9582561a38f6");

    private static final UUID SAMPLE_USER_UUID = UUID.fromString("26c70d22-7921-4700-8cd6-5a55ac2b3ffd");

    private static final String SAMPLE_STAGE_TYPE = "STAGE_TYPE";

    private static final UUID SAMPLE_TEAM_UUID = UUID.fromString("715d6ec2-208c-4e90-b1f7-16ab2d455625");

    public final static OpenCasesData SAMPLE_DATA = new OpenCasesData(
        UUID.fromString("a6a19b12-a8a3-4bfd-878e-248f00585974"),
        "COMP/319244/23",
        "UKVI",
        LocalDate.parse("2022-10-12"),
        123,
        LocalDate.parse("2023-02-26"),
        SAMPLE_STAGE_UUID,
        SAMPLE_STAGE_TYPE,
        SAMPLE_USER_UUID,
        SAMPLE_TEAM_UUID,
        "COMP",
        true
    );

    @Mock
    private UserNameValueMapper userNameValueMapper;

    @Mock
    private TeamNameValueMapper teamNameValueMapper;

    @Mock
    private StageNameValueMapper stageNameValueMapper;

    @InjectMocks
    private OpenCasesDataMapper underTest;

    @Test
    public void whenMappingOpenCasesReportData_theRowIsAugmentedWithInfoServiceData() {
        String mappedUser = "Mapped user";
        String mappedTeam = "Mapped team";
        String mappedStage = "Mapped stage";

        when(userNameValueMapper.map(SAMPLE_USER_UUID)).thenReturn(Optional.of(mappedUser));
        when(teamNameValueMapper.map(SAMPLE_TEAM_UUID)).thenReturn(Optional.of(mappedTeam));
        when(stageNameValueMapper.map(SAMPLE_STAGE_TYPE)).thenReturn(Optional.of(mappedStage));

        assertThat(underTest.mapDataToRow(SAMPLE_DATA)).isEqualTo(getExpectedRow(mappedUser, mappedTeam, mappedStage));
    }

    @Test
    public void whenMappingOpenCasesReportData_unmappableValuesUseDefaults() {
        when(userNameValueMapper.map(SAMPLE_USER_UUID)).thenReturn(Optional.empty());
        when(teamNameValueMapper.map(SAMPLE_TEAM_UUID)).thenReturn(Optional.empty());
        when(stageNameValueMapper.map(SAMPLE_STAGE_TYPE)).thenReturn(Optional.empty());

        assertThat(underTest.mapDataToRow(SAMPLE_DATA)).isEqualTo(getExpectedRow(null, null, SAMPLE_STAGE_TYPE));
    }

    private OpenCasesRow getExpectedRow(String mappedUser, String mappedTeam, String mappedStage) {
        return new OpenCasesRow(
            SAMPLE_DATA.getCaseUUID(),
            SAMPLE_DATA.getCaseReference(),
            SAMPLE_DATA.getBusinessArea(),
            SAMPLE_DATA.getDateCreated(),
            SAMPLE_DATA.getAge(),
            SAMPLE_DATA.getCaseDeadline(),
            SAMPLE_DATA.getStageUUID(),
            SAMPLE_DATA.getStageType(),
            SAMPLE_DATA.getAssignedUserUUID(),
            SAMPLE_DATA.getAssignedTeamUUID(),
            SAMPLE_DATA.getOutsideServiceStandard(),
            mappedUser,
            mappedTeam,
            mappedStage
        );
    }
}
