package uk.gov.digital.ho.hocs.casework.application.aws;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class SqsConfigurationTest {


    private SqsConfiguration config;

    @Before
    public void setup() {
        config = new SqsConfiguration();
    }

    @Test
    public void shouldThrowExceptionWhenNullAccessKey() {
        assertThatThrownBy(() -> config.auditSqsClient(null, "some secret key", "some region")).
                isInstanceOf(BeanCreationException.class);
    }

    @Test
    public void shouldThrowExceptionWhenNullSecretKey() {
        assertThatThrownBy(() -> config.auditSqsClient("some access key", null, "some region")).
                isInstanceOf(BeanCreationException.class);
    }

    @Test
    public void shouldThrowExceptionWhenNullRegion() {
        assertThatThrownBy(() -> config.auditSqsClient("some access key", "some secret key", null)).
                isInstanceOf(BeanCreationException.class);
    }


}