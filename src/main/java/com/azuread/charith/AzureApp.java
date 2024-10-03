package com.azuread.charith;

import com.azure.identity.DeviceCodeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

public class AzureApp {

    public static void main(String[] args) {
        System.out.println("Azure AD Implementation started");

        // Load OAuth properties from resources folder
        System.out.println("Loading OAuth properties from config.properties...");
        final Properties oAuthProperties = loadPropertiesFromResources("config.properties");

        if (oAuthProperties == null) {
            System.out.println("Unable to read OAuth configuration. Make sure you have a properly formatted config.properties file in the resources folder.");
            return;
        }

        System.out.println("OAuth properties successfully loaded. Initializing Microsoft Graph...");
        // Initialize Graph and perform query
        initializeGraph(oAuthProperties);
    }

    private static Properties loadPropertiesFromResources(String fileName) {
        Properties properties = new Properties();
        try (InputStream inputStream = AzureApp.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream != null) {
                System.out.println("Properties file '" + fileName + "' found. Loading properties...");
                properties.load(inputStream);
                System.out.println("Properties successfully loaded from file.");
            } else {
                System.out.println("Properties file '" + fileName + "' not found in the classpath.");
            }
        } catch (IOException e) {
            System.out.println("Unable to read properties file: " + fileName);
            e.printStackTrace();
        }
        return properties;
    }

    private static void initializeGraph(Properties properties) {
        try {
            System.out.println("Initializing Azure AD authentication...");
            initializeGraphForUserAuth(properties, challenge -> System.out.println("Device code challenge: " + challenge.getMessage()));
            System.out.println("Azure AD authentication initialized successfully.");
        } catch (Exception e) {
            System.out.println("Error initializing Graph for user auth");
            e.printStackTrace();
        }
    }

    public static void initializeGraphForUserAuth(Properties properties, Consumer<DeviceCodeInfo> challenge) throws Exception {
        if (properties == null) {
            throw new Exception("Properties cannot be null");
        }

        // Read properties from the config file
        final String clientId = properties.getProperty("clientId");
        final String tenantID = properties.getProperty("tenantId");
        final String clientSecret = properties.getProperty("clientSecret");
        final String mailNickname = properties.getProperty("nickname");
        final String graphApiURL = "https://graph.microsoft.com/.default";

        System.out.println("Reading configuration properties...");
        System.out.println("clientId: " + clientId);
        System.out.println("tenantId: " + tenantID);
        System.out.println("clientSecret: [HIDDEN]");
        System.out.println("nickname: " + mailNickname);

        if (clientId == null || tenantID == null || clientSecret == null || mailNickname == null) {
            System.out.println("One or more configuration properties are missing in the config file. Please ensure clientId, tenantId, clientSecret, and nickname are set.");
            throw new IllegalArgumentException("Missing required configuration properties.");
        }

        try {
            System.out.println("Creating ConfidentialClientApplication for clientId: " + clientId);

            // Initialize the confidential client application
            ConfidentialClientApplication app = ConfidentialClientApplication.builder(clientId,
                            ClientCredentialFactory.createFromSecret(clientSecret))
                    .authority("https://login.microsoftonline.com/" + tenantID)
                    .build();

            System.out.println("ConfidentialClientApplication created successfully.");

            // Define the required scopes for Microsoft Graph
            Set<String> scopes = Collections.singleton(graphApiURL);
            ClientCredentialParameters clientCredentialParams = ClientCredentialParameters.builder(scopes).build();

            // Acquire the access token
            System.out.println("Acquiring access token...");
            IAuthenticationResult authResult = app.acquireToken(clientCredentialParams).join();
            String accessToken = authResult.accessToken();
            System.out.println("Access Token acquired successfully: " + accessToken);

            // Perform the query using the access token
            queryUserByMailNickname(accessToken, mailNickname);

        } catch (Exception e) {
            System.out.println("Error initializing Graph for user auth or retrieving data");
            e.printStackTrace();
        }
    }

    private static void queryUserByMailNickname(String accessToken, String mailNickname) {
        System.out.println("Querying user by mail nickname: " + mailNickname);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/users?$filter=mailNickname eq '" + mailNickname + "'")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("HTTP request executed. Checking response...");
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("Response received: " + responseBody);

                // Handle the response and check if the user is found
                handleUserResponse(responseBody);
            } else {
                System.out.println("Failed to retrieve user. HTTP response code: " + response.code());
                System.out.println("Response message: " + response.message());
            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL Exception while querying user");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception while querying user");
            e.printStackTrace();
        }
    }

    private static void handleUserResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Get the value array node
            JsonNode valueNode = rootNode.path("value");

            if (valueNode.isArray() && valueNode.size() == 0) {
                System.out.println("User not found.");
            } else if (valueNode.isArray() && valueNode.size() > 0) {
                System.out.println("User found. Number of users: " + valueNode.size());
                for (JsonNode userNode : valueNode) {
                    System.out.println("User: " + userNode.path("displayName").asText() +
                            ", UserPrincipalName: " + userNode.path("userPrincipalName").asText());
                }
            } else {
                System.out.println("Unexpected response format.");
            }
        } catch (IOException e) {
            System.out.println("Failed to parse JSON response.");
            e.printStackTrace();
        }
    }
}
