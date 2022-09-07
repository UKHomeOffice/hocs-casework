package uk.gov.digital.ho.hocs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes a CSV file and is used to update the case values for specific cases.
 * <p>
 * Format of the csvs must be as follows: {caseUUID, [fields]+}, with the first
 * row being the column headers.
 * <p>
 * Fields that hold no value within the CSV are ignored when it comes to updating.
 * <p>
 * To use this file you first need to add the CSV file to the K8 hocs-casework
 * container through {@code kubectl cp [CSVFilePath] [PodName]:. -c hocs-casework}.
 * You can then invoke this java file through the containers command line by running
 * {@code java CsvCaseDataUpdater.java [AbsoluteCSVFilePath]}.
 */
public class CsvCaseDataUpdater {

    /*
     * We inject this variable through deployment, so that the spring boot application knows what to run on.
     * Default to the spring boot 8080, incase it's not present.
     */
    private static final String SERVER_PORT = getEnvironmentVariableOrDefault("SERVER_PORT", "8080");

    private static final String BASE_URL_FORMAT = "http://localhost:" + SERVER_PORT + "/case/%s/data/%s";

    private static final Logger LOGGER = Logger.getLogger(CsvCaseDataUpdater.class.getName());

    private static boolean shouldUpdate = false;

    private static String csvFilePath = "";

    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        parseArgs(args);

        List<CaseData> csvData = parseCsvFile(csvFilePath);
        updateCases(csvData);
    }

    /**
     * Used to parse the arguments for the script.
     * Index 0 is the csvFile, 1 is whether the script should run in dry mode or not.
     *
     * @param args array of arguments provided
     */
    private static void parseArgs(final String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Absolute file path to the CSV required.");
        }

        csvFilePath = args[0];

        // ignore any args passed first 2, just confirming this is the path to take
        if (args.length >= 2) {
            shouldUpdate = parseUpdate(args[1]);
        } else {
            LOGGER.log(Level.INFO, "Running dry run, add second argument of 'update' to apply.");
        }
    }

    /**
     * Used to check whether an argument is equal to the word 'update'.
     * This check is case-insensitive.
     *
     * @param updateArg the string you want to compare
     *
     * @return true if the text matches case-insentive 'update'
     */
    private static boolean parseUpdate(final String updateArg) {
        return "update".equalsIgnoreCase(updateArg);
    }

    /**
     * Parses a csv file based on the file path provided, initially checking to make sure that
     * a value is present, the file has a CSV extension and also exists as a file.
     *
     * @param filePath the absolute path to the CSV file
     *
     * @return A list of parsed case data
     *
     * @throws IOException if the reading of the file doesn't succeed.
     */
    private static List<CaseData> parseCsvFile(final String filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        if (!fileHasExtension(filePath, "csv")) {
            throw new IllegalArgumentException("File must have the csv extension.");
        }

        File csvFile = new File(filePath);
        if (!fileExists(csvFile)) {
            throw new IllegalArgumentException("File path must exist and not be a directory.");
        }

        return readFile(csvFile);
    }

    /**
     * Checks to see if the file path provided has a specified extension.
     *
     * @param filePath          the filepath to check the extension on
     * @param requiredExtension the extension that the file should have
     *
     * @return true if the extension matches, else false.
     */
    private static boolean fileHasExtension(final String filePath, final String requiredExtension) {
        Optional<String> extension = Optional.ofNullable(filePath).filter(path -> path.contains(".")).map(
            path -> path.substring(filePath.lastIndexOf(".") + 1));

        if (extension.isEmpty()) {
            throw new IllegalArgumentException("File path must have an extension.");
        }

        String fileExtension = extension.get().toUpperCase(Locale.ROOT);
        return fileExtension.equals(requiredExtension.toUpperCase(Locale.ROOT));
    }

    /**
     * Checks whether an inputted file exists and if so isn't a directory.
     *
     * @param file the file to check if it exists
     *
     * @return true if the file exists and is not an directory, else false
     */
    private static boolean fileExists(final File file) {
        return (file.exists() && !file.isDirectory());
    }

    /**
     * Reads the CSV and parses into a list of CaseData's, each representing a change
     * to a specific case.
     *
     * @param csvFile the CSV file that will be read
     *
     * @return a list of CaseData's containing the required changes
     *
     * @throws IOException if the reading of the file doesn't succeed.
     */
    private static List<CaseData> readFile(final File csvFile) throws IOException {
        List<CaseData> caseData = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            final String[] headers = parseHeaders(br.readLine());

            String line = br.readLine();
            while (line != null) {
                String[] values = line.split(",");

                if (values.length >= 1) {
                    // [0] is the case UUID
                    CaseData data = new CaseData(values[0]);

                    for (int i = 1; i < values.length; i++) {
                        String value = values[i];
                        if (!value.equals("")) {
                            // Header and the value will correspond to the same index
                            data.addToMap(headers[i], value);
                        }
                    }

                    caseData.add(data);
                }
                line = br.readLine();
            }
        }

        return caseData;
    }

    /**
     * Returns the headers of a CSV file, used for calculating what fields should be updated.
     * Splits by the comma delimiter.
     *
     * @param headerLine the first line in the CSV that represents the fields
     *
     * @return String[] of the headers
     */
    private static String[] parseHeaders(final String headerLine) {
        if (headerLine == null) {
            throw new IllegalArgumentException("Inputted header line cannot be null.");
        }

        String[] headers = headerLine.split(",");
        if (headers.length == 0) {
            throw new IllegalArgumentException("No headers have been found, please ensure these are in the CSV.");
        }

        return headers;
    }

    /**
     * Updates the inputted cases with the new values for the values supplied.
     *
     * @param allCaseData a list of CaseData objects that contain the fields that
     *                    should be changed.
     */
    private static void updateCases(List<CaseData> allCaseData) {
        final HttpClient client = HttpClient.newHttpClient();

        for (CaseData caseData : allCaseData) {
            final List<HttpRequest> requests = generateUpdateRequestForCase(caseData);

            // Loop round each request and send to the casework service
            requests.forEach(request -> {
                try {
                    if (!shouldUpdate) {
                        return;
                    }

                    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    // Report if the service does not return a 200 code
                    if (response.statusCode() != 200) {
                        LOGGER.log(Level.SEVERE, "FAILED: " + caseData.uuid + " with url: " + request.uri());
                    } else {
                        LOGGER.log(Level.INFO, "SUCCESS: " + request.uri().toString());
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Could not send request: " + request.uri(), e.getCause());
                }
            });
        }
    }

    /**
     * Generate a list of HttpRequests with the correct url and PUT data that can be
     * sent to update the data.
     *
     * @param caseData the individual cases data that we are going to be updating the
     *                 fields for
     *
     * @return a list of HttpRequests that represent a specific cases updates
     */
    private static List<HttpRequest> generateUpdateRequestForCase(CaseData caseData) {
        final List<HttpRequest> updateLinks = new ArrayList<>();

        // Each entry in the fields to change represents a change
        for (Map.Entry<String, String> entry : caseData.fieldsToChange.entrySet()) {
            String url = String.format(BASE_URL_FORMAT, caseData.uuid.toString(), entry.getKey());

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).PUT(
                variableValue(entry.getValue())).build();

            LOGGER.log(Level.INFO, "Request: " + request.uri().toString() + " Value: " + entry.getValue());

            updateLinks.add(request);
        }

        return updateLinks;
    }

    /**
     * Helper for the creating a Body for the PUT request.
     *
     * @param value the value to put into the database
     *
     * @return the body publisher for the HttpRequest
     */
    private static HttpRequest.BodyPublisher variableValue(final String value) {
        return HttpRequest.BodyPublishers.ofString(value);
    }

    /**
     * Helper to retrieve a variable from the system environment variables, defaulting to a user
     * specified value if it's not found.
     *
     * @param environmentVariable the variable name you are looking for
     * @param defaultValue        the default value you want if the variable isn't found
     *
     * @return the environent variable value if found, else the default specified
     */
    private static String getEnvironmentVariableOrDefault(final String environmentVariable, final String defaultValue) {
        String variableValue = System.getenv(environmentVariable);

        return variableValue == null ? defaultValue : variableValue;
    }

    static class CaseData {

        private final UUID uuid;

        private final Map<String, String> fieldsToChange;

        public CaseData(final String caseUuid) {
            this.uuid = UUID.fromString(caseUuid);
            this.fieldsToChange = new HashMap<>();
        }

        public void addToMap(final String key, final String value) {
            this.fieldsToChange.put(key, value);
        }

    }

}
