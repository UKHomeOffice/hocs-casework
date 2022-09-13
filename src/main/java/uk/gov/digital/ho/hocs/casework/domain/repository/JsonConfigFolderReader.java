package uk.gov.digital.ho.hocs.casework.domain.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public abstract class JsonConfigFolderReader {

    private final ObjectMapper objectMapper;

    protected JsonConfigFolderReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected <T extends CaseTypeObject<V>, V> Map<String, V> readValueFromFolder(TypeReference<T> reference) {
        var folder = Thread.currentThread().getContextClassLoader().getResource(getFolderPath());

        if (folder==null) {
            throw new ApplicationExceptions.ConfigFolderReadException(
                String.format("Configuration folder \"%s\" could not be found.", getFolderPath()),
                LogEvent.CONFIG_FOLDER_NOT_FOUND_FAILURE);
        }

        try (Stream<Path> paths = Files.walk(Paths.get(folder.toURI()))) {
            return paths.filter(Files::isRegularFile).map(path -> readValueFromFile(reference, path)).collect(
                Collectors.toMap(T::getType, T::getValue));
        } catch (IOException | URISyntaxException e) {
            throw new ApplicationExceptions.ConfigFolderReadException(
                String.format("Unable to read files from folder: %s for type %s", getFolderName(), reference.getType()),
                LogEvent.CONFIG_PARSE_FAILURE);
        }
    }

    private <T> T readValueFromFile(TypeReference<T> reference, Path path) {
        try (InputStream in = new FileInputStream(path.toString())) {
            return objectMapper.readValue(in, reference);
        } catch (IOException e) {
            throw new ApplicationExceptions.ConfigFileReadException(
                String.format("Unable to read file: %s into type %s", path.getFileName(), reference.getType()),
                LogEvent.CONFIG_PARSE_FAILURE);
        }
    }

    abstract String getFolderName();

    private String getFolderPath() {
        return String.format("config/%s", getFolderName());
    }

    public interface CaseTypeObject<V> {

        String getType();

        V getValue();

    }

}
