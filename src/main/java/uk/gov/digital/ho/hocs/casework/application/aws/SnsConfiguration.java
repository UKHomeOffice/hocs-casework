package uk.gov.digital.ho.hocs.casework.application.aws;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;

@Configuration
@Profile({ "sns" })
public class SnsConfiguration {

    @Primary
    @Bean
    public SnsAsyncClient snsClient(@Value("${aws.sns.audit-search.account.access-key}") String accessKey,
                                     @Value("${aws.sns.audit-search.account.secret-key}") String secretKey,
                                     @Value("${aws.sns.config.region}") String region) {
        return SnsAsyncClient.builder().region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    }

}
