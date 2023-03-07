package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_CACHE_USER_ERROR;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_REFRESH_USER_CACHE;

@Slf4j
@Service
@Profile("reports")
public class StageNameValueMapper implements ReportValueMapper<String, String> {

    private final InfoClient infoServiceClient;

    private final LoadingCache<String, String> uuidToStageNameCache;

    @Autowired
    public StageNameValueMapper(InfoClient infoServiceClient) {
        this.infoServiceClient = infoServiceClient;
        this.uuidToStageNameCache = Caffeine.newBuilder().maximumSize(10_000).build(this::fetchStageNameByUUID);
        refreshCache();
    }

    private String fetchStageNameByUUID(String stageTypeString) {
        try {
            StageTypeDto stageTypeDto = infoServiceClient.getStageTypeByTypeString(stageTypeString);
            return stageTypeDto.getDisplayName();
        } catch (Exception e) {
            log.warn("Failed to fetch stage type with stageTypeString {}", stageTypeString, REPORT_MAPPER_CACHE_USER_ERROR);
            return null;
        }
    }

    @Override
    public void refreshCache() {
        log.info("Refreshing cache", value(EVENT, REPORT_MAPPER_REFRESH_USER_CACHE));
        infoServiceClient.getAllStageTypes()
                         .forEach(stageType -> uuidToStageNameCache.put(stageType.getType(), stageType.getDisplayName()));
    }

    @Override
    public Optional<String> map(String source) {
        return Optional.ofNullable(source).map(uuidToStageNameCache::get);
    }

}
