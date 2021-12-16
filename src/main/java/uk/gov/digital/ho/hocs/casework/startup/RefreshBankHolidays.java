package uk.gov.digital.ho.hocs.casework.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.BankHolidayService;

@Service
@Slf4j
public class RefreshBankHolidays {
    private final BankHolidayService bankHolidayService;

    @Autowired
    public RefreshBankHolidays(final BankHolidayService bankHolidayService) {
        this.bankHolidayService = bankHolidayService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshBankHolidays() {
        bankHolidayService.refreshBankHolidayTable();
    }
}
