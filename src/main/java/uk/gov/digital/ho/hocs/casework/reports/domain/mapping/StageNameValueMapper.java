package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_STAGE_CACHE_ERROR;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_STAGE_CACHE_REFRESH;

@Slf4j
@Service
@Profile("reports")
public class StageNameValueMapper
    implements ReportValueMapper<String, String>, ApplicationListener<ContextRefreshedEvent> {

    private final InfoClient infoServiceClient;

    private final LoadingCache<String, String> uuidToStageNameCache;

    public StageNameValueMapper(InfoClient infoServiceClient) {
        this.infoServiceClient = infoServiceClient;
        this.uuidToStageNameCache = Caffeine.newBuilder().build(this::fetchStageNameByUUID);
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        refreshCache();
    }

    private String fetchStageNameByUUID(String stageTypeString) {
        try {
            StageTypeDto stageTypeDto = infoServiceClient.getStageTypeByTypeString(stageTypeString);
            return stageTypeDto.getDisplayName();
        } catch (Exception e) {
            log.warn("Failed to fetch stage type with stageTypeString {}", stageTypeString,
                REPORT_MAPPER_STAGE_CACHE_ERROR
                    );
            return stageTypeString;
        }
    }

    @Override
    public void refreshCache() {
        log.info("Refreshing cache", value(EVENT, REPORT_MAPPER_STAGE_CACHE_REFRESH));
        uuidToStageNameCache.invalidateAll();
        infoServiceClient
            .getAllStageTypes()
            .forEach(stageType -> uuidToStageNameCache.put(stageType.getType(), stageType.getDisplayName()));
    }

    @Override
    public Optional<String> map(String source) {
        return Optional.ofNullable(source).map(uuidToStageNameCache::get);
    }

}
