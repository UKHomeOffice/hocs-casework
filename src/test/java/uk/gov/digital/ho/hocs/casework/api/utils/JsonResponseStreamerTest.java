package uk.gov.digital.ho.hocs.casework.api.utils;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class JsonResponseStreamerTest {

    private JsonResponseStreamer jsonResponseStreamer;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private TransactionStatus transactionStatus;

    record TestObject(@JsonProperty("value") String value) {}

    @Before
    public void setUp() {
        jsonResponseStreamer = new JsonResponseStreamer(new ObjectMapper(), transactionTemplate);

        when(transactionTemplate.execute(any())).thenAnswer(
            invocation -> invocation.<TransactionCallback<TransactionStatus>>getArgument(0)
                                    .doInTransaction(transactionStatus));
    }

    @Test
    public void jsonWrappedTransactionalStreamingResponseBody_transformsStreamIntoStreamingJsonBody() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResponseEntity<StreamingResponseBody> response = jsonResponseStreamer.jsonWrappedTransactionalStreamingResponseBody(
            "field_name",
            () -> Stream.of(new TestObject("One"), new TestObject("Two"))
        );

        Objects.requireNonNull(response.getBody()).writeTo(out);

        assertThat(out.toString()).isEqualTo("{\"field_name\":[{\"value\":\"One\"},{\"value\":\"Two\"}]}");
    }

    @Test
    public void jsonWrappedTransactionalStreamingResponseBody_transformsStreamIntoStreamingJsonBodyWithExtraFields() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResponseEntity<StreamingResponseBody> response = jsonResponseStreamer.jsonWrappedTransactionalStreamingResponseBody(
            "field_name",
            () -> Stream.of(new TestObject("One"), new TestObject("Two")),
            Map.of(
                "extra_string", "A String",
                "extra_object", new TestObject("Extra")
            )
        );

        Objects.requireNonNull(response.getBody()).writeTo(out);

        assertThat(out.toString()).isEqualTo(
            "{" +
            "\"extra_string\":\"A String\"," +
            "\"extra_object\":{\"value\":\"Extra\"}," +
            "\"field_name\":[{\"value\":\"One\"},{\"value\":\"Two\"}]}"
        );
    }

    @Test
    public void jsonStringWrappedTransactionalStreamingResponseBody_transformsStreamIntoStreamingJsonBody() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResponseEntity<StreamingResponseBody> response = jsonResponseStreamer.jsonStringsWrappedTransactionalStreamingResponseBody(
            "field_name",
            () -> Stream.of("{\"value\":\"One\"}","{\"value\":\"Two\"}")
        );

        Objects.requireNonNull(response.getBody()).writeTo(out);

        assertThat(out.toString()).isEqualTo("{\"field_name\":[{\"value\":\"One\"},{\"value\":\"Two\"}]}");
    }

    @Test
    public void jsonStringWrappedTransactionalStreamingResponseBody_transformsStreamIntoStreamingJsonBodyWithExtraFields() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResponseEntity<StreamingResponseBody> response = jsonResponseStreamer.jsonWrappedTransactionalStreamingResponseBody(
            "field_name",
            () -> Stream.of("{\"value\":\"One\"}","{\"value\":\"Two\"}"),
            Map.of(
                "extra_string", "A String",
                "extra_object", new TestObject("Extra")
            )
        );

        Objects.requireNonNull(response.getBody()).writeTo(out);

        assertThat(out.toString()).isEqualTo(
            "{" +
            "\"extra_string\":\"A String\"," +
            "\"extra_object\":{\"value\":\"Extra\"}," +
            "\"field_name\":[{\"value\":\"One\"},{\"value\":\"Two\"}]}"
        );
    }

}
