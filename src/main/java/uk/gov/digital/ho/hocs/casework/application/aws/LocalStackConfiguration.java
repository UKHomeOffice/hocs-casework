package uk.gov.digital.ho.hocs.casework.application.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
@Profile({ "local" })
public class LocalStackConfiguration {
    private final AwsCredentialsProvider awsCredentialsProvider;

    public LocalStackConfiguration() {
        this.awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));
    }

    @Primary
    @Bean
    public SqsAsyncClient sqsClient(@Value("${localstack.base-url}") String baseUrl, @Value("${localstack.config.region}") String region) {
        return SqsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(awsCredentialsProvider)
            .endpointOverride(URI.create(baseUrl))
            .build();
    }

    @Primary
    @Bean
    public SnsAsyncClient snsClient(@Value("${localstack.base-url}") String baseUrl, @Value("${localstack.config.region}") String region) {
        return SnsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(awsCredentialsProvider)
            .endpointOverride(URI.create(baseUrl))
            .build();
    }

}
