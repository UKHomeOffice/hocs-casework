package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;

import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
@Profile("reports")
public class TeamNameValueMapper implements ReportValueMapper<UUID, String> {

    private final InfoClient infoServiceClient;

    private final LoadingCache<UUID, String> uuidToTeamNameCache;

    @Autowired
    public TeamNameValueMapper(InfoClient infoServiceClient) {
        this.infoServiceClient = infoServiceClient;
        this.uuidToTeamNameCache = Caffeine.newBuilder().build(this::fetchTeamNameByUUID);
        refreshCache();
    }

    private String fetchTeamNameByUUID(UUID teamUUID) {
        try {
            TeamDto team = infoServiceClient.getTeamByUUID(teamUUID);
            return team.getDisplayName();
        } catch (Exception e) {
            log.warn("Failed to fetch team with UUID {}", teamUUID, REPORT_MAPPER_TEAM_CACHE_ERROR);
            return teamUUID.toString();
        }
    }

    @Override
    public void refreshCache() {
        log.info("Refreshing cache", value(EVENT, REPORT_MAPPER_TEAM_CACHE_REFRESH));
        uuidToTeamNameCache.invalidateAll();
        infoServiceClient.getTeams().forEach(team -> uuidToTeamNameCache.put(team.getUuid(), team.getDisplayName()));
    }

    @Override
    public Optional<String> map(UUID source) {
        return Optional.ofNullable(source).map(uuidToTeamNameCache::get);
    }

}
