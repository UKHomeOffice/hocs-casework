package uk.gov.digital.ho.hocs.casework.application.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@Profile({ "sqs" })
public class SqsConfiguration {

    @Bean
    public SqsAsyncClient sqsAsyncClient(@Value("${aws.sqs.notify.account.access-key}") String accessKey,
                                         @Value("${aws.sqs.notify.account.secret-key}") String secretKey,
                                         @Value("${aws.sqs.config.region}") String region) {
        return SqsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey))).build();
    }
}
