package uk.gov.digital.ho.hocs.casework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import jakarta.annotation.PreDestroy;

@Slf4j
@SpringBootApplication
@EnableCaching
public class HocsCaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(HocsCaseApplication.class, args);
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping gracefully");
    }

}
