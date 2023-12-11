package xyz.digitalbank.demo.Activity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.Arrays;


public class TestDataGenerator {

    private static String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    private static void writeToCsvFile(String csvFilePath, String[] fieldNames, Object[][] data) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath))) {
            // Write header
            writer.println(String.join(",", fieldNames));

            // Write data
            for (Object[] row : data) {
                // Convert each element to String before joining
                String[] stringRow = Arrays.stream(row)
                        .map(Object::toString)
                        .toArray(String[]::new);

                writer.println(String.join(",", stringRow));
            }
        }
    }

    public static void generateTestData() {
        String apiKeyId = System.getenv("BLAZEMETER_API_KEY_ID");
        String apiKeySecret = System.getenv("BLAZEMETER_API_KEY_SECRET");
        String csvFileName = "generated_data.csv";
        String modelId = "a8755151-66b7-43f1-babd-01425bc9179c";
        String workspaceId = "348607";

        // Prepare the authentication string
        String userpass = apiKeyId + ':' + apiKeySecret;
        String encodedU = Base64.getEncoder().encodeToString(userpass.getBytes(StandardCharsets.UTF_8));

        // Prepare the headers for the API request
        String authorizationHeader = "Basic " + encodedU;
        String contentTypeHeader = "application/json";
        String cacheControlHeader = "no-cache";
        String acceptHeader = "*/*";
        String acceptEncodingHeader = "gzip, deflate, br";

        // API endpoint URL to get the signed URL
        String signUrl = "https://a.blazemeter.com/api/v4/folders/" + workspaceId + "/s3/sign?fileName=" + csvFileName;

        // Make the API request to get the signed URL
        try {
            URL url = new URL(signUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", authorizationHeader);
            connection.setRequestProperty("Content-Type", contentTypeHeader);
            connection.setRequestProperty("Cache-Control", cacheControlHeader);
            connection.setRequestProperty("Accept", acceptHeader);
            connection.setRequestProperty("Accept-Encoding", acceptEncodingHeader);

            // Check if the request was successful
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error: Failed to get signed URL. Response Code: " + responseCode);
                System.exit(1);
            }

            // Load the JSON response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Extract the signed URL from the JSON response
                String signedUrl = response.toString();

                // Make the API request to generate test data
                String generateTestDataUrl = "https://tdm.blazemeter.com/api/v1/workspaces/" + workspaceId + "/testdata/publish";
                String[] fieldNames = {"field1", "field2", "field3"};  // Replace with your field names
                Object[][] testData = {{"value1", "value2", "value3"}, {"value4", "value5", "value6"}};  // Replace with your test data

                // Request data for the API call
                String sessionId = generateSessionId();
                String model = modelId;

                // Make the API request to generate test data
                url = new URL(generateTestDataUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", authorizationHeader);
                connection.setRequestProperty("Content-Type", contentTypeHeader);
                connection.setRequestProperty("Cache-Control", cacheControlHeader);
                connection.setRequestProperty("Accept", acceptHeader);
                connection.setRequestProperty("Accept-Encoding", acceptEncodingHeader);

                // Set request body
                String requestBody = String.format("{\"type\": \"generic-from-ar\", \"sessionId\": \"%s\", \"model\": \"%s\"}", sessionId, model);
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Check if the request was successful
                responseCode = connection.getResponseCode();
                if (responseCode != 201) {
                    System.out.println("Error: Failed to generate test data. Response Code: " + responseCode);
                    System.exit(1);
                }

                // Load JSON data from the API response
                try (BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder responseData = new StringBuilder();
                    String responseLine;
                    while ((responseLine = responseReader.readLine()) != null) {
                        responseData.append(responseLine);
                    }

                    // Parse the JSON response
                    // (You may need to adjust this part based on the actual structure of the response)
                    // JSONObject data = new JSONObject(responseData.toString());
                    // JSONArray generatedData = data.getJSONObject("result").getJSONObject("data").getJSONObject("data").getJSONObject("entities").getJSONObject("userlogin").getJSONArray("generatedData");

                    // Write 'generatedData' to the CSV file
                    // (You may need to adjust this part based on the actual structure of the response)
                    // writeToCsvFile(csvFileName, generatedData);
                }

                System.out.println("CSV file '" + csvFileName + "' successfully created.");

            } catch (IOException e) {
                System.out.println("Error reading signed URL response: " + e.getMessage());
                System.exit(1);
            }

        } catch (IOException e) {
            System.out.println("Error making signed URL request: " + e.getMessage());
            System.exit(1);
        }
    }
}
