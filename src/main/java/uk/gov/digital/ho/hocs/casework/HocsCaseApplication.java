package uk.gov.digital.ho.hocs.casework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HocsCaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(HocsCaseApplication.class, args);
	}

	public static boolean isNullOrEmpty(String value) {
		if (value == null) {
			return true;
		} else if (value.equals("")) {
			return true;
		} else return value.trim().equals("");
	}
}
