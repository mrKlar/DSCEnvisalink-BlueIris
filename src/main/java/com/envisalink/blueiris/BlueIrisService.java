package com.envisalink.blueiris;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BlueIrisService {

    private static final Logger logger = LogManager.getLogger(BlueIrisService.class);
    private final String blueirisUrl;
    private final String username;
    private final String password;
    private final CloseableHttpClient httpClient;
    private final Gson gson = new Gson();
    private String sessionId;

    public BlueIrisService(Config config) {
        String host = config.getProperty("blueiris.host");
        int port = config.getIntProperty("blueiris.port", 81);
        this.blueirisUrl = "http://" + host + ":" + port + "/json";
        this.username = config.getProperty("blueiris.username");
        this.password = config.getProperty("blueiris.password");
        this.httpClient = HttpClients.createDefault();
    }

    public void connect() throws IOException {
        logger.info("Attempting to connect to Blue Iris at {}", blueirisUrl);

        // Step 1: Initial login request to get a session token
        JsonObject loginRequest = new JsonObject();
        loginRequest.addProperty("cmd", "login");
        JsonObject loginResponse = sendRequest(loginRequest);

        if (loginResponse == null || !loginResponse.has("session")) {
            logger.error("Blue Iris login failed: Did not receive a session ID.");
            throw new IOException("Blue Iris login failed: No session ID received.");
        }
        this.sessionId = loginResponse.get("session").getAsString();
        logger.info("Received session ID from Blue Iris.");

        // Step 2: Respond with hashed password
        String responseHash = DigestUtils.md5Hex(this.username + ":" + this.sessionId + ":" + this.password);

        JsonObject authRequest = new JsonObject();
        authRequest.addProperty("cmd", "login");
        authRequest.addProperty("session", this.sessionId);
        authRequest.addProperty("response", responseHash);
        JsonObject authResponse = sendRequest(authRequest);

        if (authResponse != null && "success".equals(authResponse.get("result").getAsString())) {
            logger.info("Successfully authenticated with Blue Iris. Session ID: {}", this.sessionId);
        } else {
            logger.error("Blue Iris authentication failed.");
            this.sessionId = null; // Clear invalid session ID
            throw new IOException("Blue Iris authentication failed.");
        }
    }

    public boolean setProfile(int profileId) throws IOException {
        if (this.sessionId == null) {
            logger.warn("Not connected to Blue Iris. Attempting to reconnect.");
            connect(); // Try to re-establish connection
        }

        logger.info("Setting Blue Iris profile to {}", profileId);
        JsonObject profileRequest = new JsonObject();
        profileRequest.addProperty("cmd", "profile");
        profileRequest.addProperty("profile", profileId);
        profileRequest.addProperty("session", this.sessionId);

        JsonObject response = sendRequest(profileRequest);

        if (response != null && "success".equals(response.get("result").getAsString())) {
            logger.info("Successfully set Blue Iris profile to {}.", profileId);
            return true;
        } else {
            logger.error("Failed to set Blue Iris profile. Response: {}", response);
            // Invalidate session, forcing re-login on next attempt
            this.sessionId = null;
            return false;
        }
    }

    private JsonObject sendRequest(JsonObject requestBody) throws IOException {
        HttpPost post = new HttpPost(blueirisUrl);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(gson.toJson(requestBody)));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                logger.error("Received empty response from Blue Iris.");
                return null;
            }
            return JsonParser.parseString(jsonResponse).getAsJsonObject();
        }
    }

    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("Error closing HttpClient", e);
        }
    }
}
