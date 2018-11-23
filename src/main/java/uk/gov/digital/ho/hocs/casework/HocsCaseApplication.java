package uk.gov.digital.ho.hocs.casework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HocsCaseApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(HocsCaseApplication.class, args);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
