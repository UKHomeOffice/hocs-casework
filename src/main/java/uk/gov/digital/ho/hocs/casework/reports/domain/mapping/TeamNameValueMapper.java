package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;

import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_TEAM_CACHE_ERROR;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_TEAM_CACHE_REFRESH;

@Slf4j
@Service
@Profile("reports")
public class TeamNameValueMapper implements ReportValueMapper<UUID, String>, ApplicationListener<ContextRefreshedEvent> {

    private final InfoClient infoServiceClient;

    private final LoadingCache<UUID, String> uuidToTeamNameCache;

    public TeamNameValueMapper(InfoClient infoServiceClient) {
        this.infoServiceClient = infoServiceClient;
        this.uuidToTeamNameCache = Caffeine.newBuilder().build(this::fetchTeamNameByUUID);
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
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
