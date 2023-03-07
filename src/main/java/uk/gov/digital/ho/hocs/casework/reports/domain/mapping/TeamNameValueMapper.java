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
        this.uuidToTeamNameCache = Caffeine.newBuilder().maximumSize(10_000).build(this::fetchTeamNameByUUID);
        refreshCache();
    }

    private String fetchTeamNameByUUID(UUID userUUID) {
        try {
            TeamDto team = infoServiceClient.getTeamByUUID(userUUID);
            return team.getDisplayName();
        } catch (Exception e) {
            log.warn("Failed to fetch team with UUID {}", userUUID, REPORT_MAPPER_CACHE_USER_ERROR);
            return null;
        }
    }

    @Override
    public void refreshCache() {
        log.info("Refreshing cache", value(EVENT, REPORT_MAPPER_REFRESH_USER_CACHE));
        infoServiceClient.getTeams().forEach(team -> uuidToTeamNameCache.put(team.getUuid(), team.getDisplayName()));
    }

    @Override
    public Optional<String> map(UUID source) {
        return Optional.ofNullable(source).map(uuidToTeamNameCache::get);
    }

}
