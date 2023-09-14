package uk.gov.digital.ho.hocs.casework.api.utils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
@Slf4j
public class JsonResponseStreamer {

    private final ObjectMapper objectMapper;

    private final TransactionTemplate transactionTemplate;

    public JsonResponseStreamer(ObjectMapper objectMapper, TransactionTemplate transactionTemplate) {
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public <T> ResponseEntity<StreamingResponseBody> jsonWrappedTransactionalStreamingResponseBody(
        String fieldName,
        Supplier<Stream<T>> streamSupplier)
    {
        return jsonWrappedTransactionalStreamingResponseBody(fieldName, streamSupplier, Map.of());
    }

    public <T> ResponseEntity<StreamingResponseBody> jsonWrappedTransactionalStreamingResponseBody(
        String fieldName,
        Supplier<Stream<T>> streamSupplier,
        Map<String, Object> additionalFields)
    {
        StreamingResponseBody body = outputStream -> {
            try {
                JsonFactory factory = new JsonFactory();

                JsonGenerator generator = factory.createGenerator(outputStream, JsonEncoding.UTF8);
                generator.setCodec(objectMapper);

                generator.writeStartObject();

                additionalFields.forEach((nestedFieldName, object) -> {
                    try {
                        generator.writeObjectField(nestedFieldName, object);
                    } catch (IOException e) {
                        log.error("Failed to write {} to json response: {}", nestedFieldName, e.getMessage());
                    }
                });

                generator.writeArrayFieldStart(fieldName);

                transactionTemplate.execute(status -> {
                    streamSupplier.get().forEach(streamItem -> {
                        try {
                            generator.writeObject(streamItem);
                        } catch (IOException e) {
                            throw new ApplicationExceptions.ReportBodyStreamingException("Failed to write streaming response body for item: {}", LogEvent.CORRESPONDENT_SERIALISATION_EXCEPTION, streamItem);
                        }
                    });

                    return null;
                });

                generator.writeEndArray();
                generator.writeEndObject();
                generator.close();
            }
            catch (Exception e) {
                log.error("Failed to write streaming response body");
            }
        };

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        return new ResponseEntity<>(
            body,
            responseHeaders,
            HttpStatus.OK
        );
    }
}
