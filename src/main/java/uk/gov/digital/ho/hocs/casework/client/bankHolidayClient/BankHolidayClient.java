package uk.gov.digital.ho.hocs.casework.client.bankHolidayClient;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public BankHolidayClient(final RestHelper restHelper) {
        this.restHelper = restHelper;
    }

    public Map<String, BankHolidaysByRegionDto> getBankHolidays() {
        final Map<String, BankHolidaysByRegionDto> bankHolidaysByRegion =
                restHelper.get("https://www.gov.uk", "/bank-holidays.json",
                        new ParameterizedTypeReference<>() {
                        });

        log.info("Got bank holidays for {} regions", bankHolidaysByRegion.size(),
                value(EVENT, REFRESH_BANK_HOLIDAYS));

        return bankHolidaysByRegion;
    }

}
