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
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;

import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_USER_CACHE_ERROR;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_USER_CACHE_REFRESH;

@Slf4j
@Service
@Profile("reporting")
public class UserNameValueMapper
    implements ReportValueMapper<UUID, String>, ApplicationListener<ContextRefreshedEvent> {

    private final InfoClient infoServiceClient;

    private final LoadingCache<UUID, String> uuidToUserNameCache;

    public UserNameValueMapper(InfoClient infoServiceClient) {
        this.infoServiceClient = infoServiceClient;
        this.uuidToUserNameCache = Caffeine.newBuilder().build(this::fetchUserNameByUUID);
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        refreshCache();
    }

    private String fetchUserNameByUUID(UUID userUUID) {
        try {
            UserDto user = infoServiceClient.getUser(userUUID);
            return formatUsername(user);
        } catch (Exception e) {
            log.warn("Failed to fetch user with UUID {}", userUUID, REPORT_MAPPER_USER_CACHE_ERROR);
            return userUUID.toString();
        }
    }

    private static String formatUsername(UserDto user) {
        return user.getFirstName() == null && user.getLastName() == null
            ? user.getUsername()
            : String.format("%s %s", user.getFirstName(), user.getLastName()).trim();
    }

    @Override
    public void refreshCache() {
        log.info("Refreshing cache", value(EVENT, REPORT_MAPPER_USER_CACHE_REFRESH));
        uuidToUserNameCache.invalidateAll();
        infoServiceClient
            .getAllUsers()
            .forEach(user -> uuidToUserNameCache.put(UUID.fromString(user.getId()), formatUsername(user)));
    }

    @Override
    public Optional<String> map(UUID source) {
        return Optional.ofNullable(source).map(uuidToUserNameCache::get);
    }

}
