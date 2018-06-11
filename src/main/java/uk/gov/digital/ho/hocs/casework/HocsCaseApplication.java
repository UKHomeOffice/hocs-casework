package uk.gov.digital.ho.hocs.casework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

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

	public static boolean isNullOrEmpty(UUID value) {
		return value == null;
	}
}
