package uk.gov.digital.ho.hocs.casework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.util.UUID;

@SpringBootApplication
public class HocsCaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(HocsCaseApplication.class, args);
	}

	public static boolean isNullOrEmpty(String value) {
		if (value == null) {
			return true;
        } else {
            return value.trim().equals("");
        }
	}

	public static boolean isNullOrEmpty(UUID value) {
		return value == null;
	}

	public static boolean isNullOrEmpty(CaseType value) {
		return value == null;
	}
	public static boolean isNullOrEmpty(StageType value) {
		return value == null;
	}

}
