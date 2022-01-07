package uk.gov.digital.ho.hocs.casework.client.bankHolidayClient;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.BankHolidaysByRegionDto;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;

import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REFRESH_BANK_HOLIDAYS;

@Slf4j
@Component
public class BankHolidayClient {
    private final RestHelper restHelper;
    private String bankHolidaysApiUrl;

    @Autowired
    public BankHolidayClient(final RestHelper restHelper,
                             final @Value("${bank-holidays-api-url}") String bankHolidaysApiUrl) {
        this.restHelper = restHelper;
        this.bankHolidaysApiUrl = bankHolidaysApiUrl;
    }

    public Map<String, BankHolidaysByRegionDto> getBankHolidays() {
        final Map<String, BankHolidaysByRegionDto> bankHolidaysByRegion =
                restHelper.get("", bankHolidaysApiUrl,
                        new ParameterizedTypeReference<>() {
                        });

        log.info("Got bank holidays for {} regions", bankHolidaysByRegion.size(),
                value(EVENT, REFRESH_BANK_HOLIDAYS));

        return bankHolidaysByRegion;
    }

}
