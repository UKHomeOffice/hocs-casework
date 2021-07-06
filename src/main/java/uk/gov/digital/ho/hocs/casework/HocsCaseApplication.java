package uk.gov.digital.ho.hocs.casework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PreDestroy;

@Slf4j
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableRetry
@EnableAsync
public class HocsCaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(HocsCaseApplication.class, args);
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping gracefully");
    }

}
