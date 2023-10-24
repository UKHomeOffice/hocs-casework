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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
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

    /**
     * Convert a stream of objects that can be serialised to JSON by the default ObjectMapper into a json object
     * containing an array field with the serialised contents of the stream as the array's items. The stream will be
     * consumed within a transaction to prevent issues with the database query closing before the stream is closed.
     *
     * @param fieldName The field name attached to the array of streamed items
     * @param streamSupplier A callback that will supply the stream of items to serialise. This will be run within a
     *                       transaction
     * @return StreamingResponseBody producing a root json object with the stream items in an array field
     */
    public ResponseEntity<StreamingResponseBody> jsonWrappedTransactionalStreamingResponseBody(
        String fieldName,
        Supplier<Stream<?>> streamSupplier)
    {
        return jsonWrappedTransactionalStreamingResponseBody(fieldName, streamSupplier, Map.of());
    }

    /**
     * Convert a stream of objects that can be serialised to JSON by the default ObjectMapper into a json object
     * containing an array field with the serialised contents of the stream as the array's items. The stream will be
     * consumed within a transaction to prevent issues with the database query closing before the stream is closed.
     *
     * @param fieldName The field name attached to the array of streamed items
     * @param streamSupplier A callback that will supply the stream of items to serialise. This will be run within a
     *                       transaction
     * @param additionalFields These will be added as fields to the root JSON object. The keys will be used as the field
     *                         names and the values will be serialised to JSON
     * @return StreamingResponseBody producing a root json object with the stream items in an array field
     */
    public ResponseEntity<StreamingResponseBody> jsonWrappedTransactionalStreamingResponseBody(
        String fieldName,
        Supplier<Stream<?>> streamSupplier,
        Map<String, Object> additionalFields)
    {
        return wrapStream(
            fieldName,
            (generator, outputStream) -> status -> {
                    streamSupplier.get().forEach(streamItem -> {
                        try {
                            generator.writeObject(streamItem);
                        } catch (IOException e) {
                            throw new ApplicationExceptions.ReportBodyStreamingException(
                                String.format(
                                    "Failed to write streaming response body for item: %s - %s",
                                    streamItem,
                                    e
                                ),
                                LogEvent.STREAMING_JSON_SERIALISATION_EXCEPTION
                            );
                        }
                    });

                    return null;
            },
            additionalFields
        );
    }

    /**
     * Convert a stream of json strings into a json object containing an array field with the raw strings from the
     * stream as the array's items. The stream will be consumed within a transaction to prevent issues with the database
     * query closing before the stream is closed.
     *
     * @param fieldName The field name attached to the array of streamed items
     * @param streamSupplier A callback that will supply a stream of valid JSON Strings. This will be run within a
     *                       transaction.
     * @return StreamingResponseBody producing a root json object with the stream items in an array field
     */
    public  ResponseEntity<StreamingResponseBody> jsonStringsWrappedTransactionalStreamingResponseBody(
        String fieldName,
        Supplier<Stream<String>> streamSupplier)
    {
        return jsonStringsWrappedTransactionalStreamingResponseBody(fieldName, streamSupplier, Map.of());
    }

    /**
     * Convert a stream of json strings into a json object containing an array field with the raw strings from the
     * stream as the array's items. The stream will be consumed within a transaction to prevent issues with the database
     * query closing before the stream is closed.
     *
     * @param fieldName The field name attached to the array of streamed items
     * @param streamSupplier A callback that will supply a stream of valid JSON Strings. This will be run within a
     *                       transaction.
     * @param additionalFields These will be added as fields to the root JSON object. The keys will be used as the field
     *                         names and the values will be serialised to JSON
     * @return StreamingResponseBody producing a root json object with the stream items in an array field
     */
    public ResponseEntity<StreamingResponseBody> jsonStringsWrappedTransactionalStreamingResponseBody(
        String fieldName,
        Supplier<Stream<String>> streamSupplier,
        Map<String, Object> additionalFields
    ) {
        return wrapStream(
            fieldName,
            (generator, outputStream) -> status -> {
                // Needs to be final to be used in stream.
                AtomicBoolean prefixComma = new AtomicBoolean(false);

                streamSupplier.get().forEach(
                    (streamItem) -> {
                        try {
                            if (prefixComma.get()) {
                                outputStream.write(',');
                            } else {
                                prefixComma.set(true);
                            }

                            outputStream.write(streamItem.getBytes());
                        } catch (IOException e) {
                            log.error("Failed to write {} to json response - {}", streamItem, e);
                        }
                    }
                );

                return null;
            },
            additionalFields
        );
    }

    private ResponseEntity<StreamingResponseBody> wrapStream(
        String fieldName,
        BiFunction<JsonGenerator, OutputStream, TransactionCallback<TransactionStatus>> transactionCallbackSupplier,
        Map<String, Object> additionalFields
    ) {
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
                        log.error("Failed to write {} to json response - {}", nestedFieldName, e);
                    }
                });

                generator.writeArrayFieldStart(fieldName);
                // jsonStringsWrappedTransactionalStreamingResponseBody writes JSON strings directly to the output
                // stream, so flush current json first
                generator.flush();

                transactionTemplate.execute(transactionCallbackSupplier.apply(generator, outputStream));

                generator.writeEndArray();
                generator.writeEndObject();
                generator.close();
            } catch (Exception e) {
                log.error("Failed to write streaming response body - {}", e);
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
