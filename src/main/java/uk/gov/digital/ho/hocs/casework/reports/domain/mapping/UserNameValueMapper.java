package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;

import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_CACHE_USER_ERROR;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_REFRESH_USER_CACHE;

@Slf4j
@Service
@Profile("reports")
public class UserNameValueMapper implements ReportValueMapper<UUID, String> {

    private final InfoClient infoServiceClient;

    private final LoadingCache<UUID, String> uuidToUserNameCache;

    @Autowired
    public UserNameValueMapper(InfoClient infoServiceClient) {
        this.infoServiceClient = infoServiceClient;
        this.uuidToUserNameCache = Caffeine.newBuilder().build(this::fetchUserNameByUUID);
        refreshCache();
    }

    private String fetchUserNameByUUID(UUID userUUID) {
        try {
            UserDto user = infoServiceClient.getUser(userUUID);
            return formatUsername(user);
        } catch (Exception e) {
            log.warn("Failed to fetch user with UUID {}", userUUID, REPORT_MAPPER_CACHE_USER_ERROR);
            return null;
        }
    }

    private static String formatUsername(UserDto user) {
        return user.getFirstName() == null && user.getLastName() == null
            ? user.getUsername()
            : String.format("%s %s", user.getFirstName(), user.getLastName()).trim();
    }

    @Override
    public void refreshCache() {
        log.info("Refreshing cache", value(EVENT, REPORT_MAPPER_REFRESH_USER_CACHE));
        uuidToUserNameCache.invalidateAll();
        infoServiceClient.getAllUsers()
                         .forEach(user -> uuidToUserNameCache.put(UUID.fromString(user.getId()), formatUsername(user)));
    }

    @Override
    public Optional<String> map(UUID source) {
        return Optional.ofNullable(source).map(uuidToUserNameCache::get);
    }

}
