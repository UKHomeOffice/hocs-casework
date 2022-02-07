package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankHolidayResource {
    BankHolidayService bankHolidayService;

    @Autowired
    public BankHolidayResource(BankHolidayService bankHolidayService) {
        this.bankHolidayService = bankHolidayService;
    }

    @GetMapping(value = "/bankHoliday/refresh")
    public ResponseEntity<String> refreshFromApi() {
        int holidaysAdded = bankHolidayService.refreshBankHolidayTable();
        return ResponseEntity.ok(String.format("Success: Added %d bank holidays", holidaysAdded));
    }


}