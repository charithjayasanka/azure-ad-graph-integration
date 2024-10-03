
# Azure AD Integration with Microsoft Graph

[![Java](https://img.shields.io/badge/Java-8%2B-brightgreen.svg)](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)
[![Azure AD](https://img.shields.io/badge/Azure%20AD-Integration-orange.svg)](https://azure.microsoft.com/en-us/services/active-directory/)
[![Microsoft Graph](https://img.shields.io/badge/Microsoft%20Graph-Integration-blue.svg)](https://developer.microsoft.com/en-us/graph/)

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Building the Project](#building-the-project)
- [Testing with Curl](#testing-with-curl)
- [Logging and Debugging](#logging-and-debugging)
- [Error Handling](#error-handling)
- [Contributing](#contributing)
- [Contact](#contact)

## Introduction

This Java application demonstrates how to integrate with **Azure Active Directory (Azure AD)** using the **Microsoft Authentication Library for Java (MSAL4J)** and interact with the **Microsoft Graph API**. It provides an example of authenticating against Azure AD using the client credentials flow and then querying user details using Microsoft Graph.

The project is a solid foundation for anyone looking to develop more complex Azure AD integrations, and it can be extended to cover broader use cases with Microsoft Graph.

## Features

- **Azure AD Authentication**: Uses client credentials flow to authenticate with Azure AD.
- **Microsoft Graph API Interaction**: Query user details using `mailNickname`.
- **Comprehensive Logging**: Logs all stages of the application's execution for better traceability and debugging.
- **Configuration File Management**: Easy-to-manage configuration using a properties file.
- **Robust Error Handling**: Provides meaningful error messages and handling mechanisms.

## Prerequisites

Ensure you have the following before building and running the application:

1. **Java 8 or higher**: [Download Java](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)
2. **Maven**: [Download Maven](https://maven.apache.org/download.cgi)
3. **Azure AD Tenant**: Create an Azure AD tenant and register an application.
4. **Azure AD Application Registration**:
   - Application **Client ID**.
   - Application **Client Secret**.
   - The **Tenant ID** of your Azure AD directory.
   - Required Microsoft Graph permissions: `Directory.Read.All` and Admin Consent.
5. **Microsoft Graph SDK**: Added as a dependency in the `pom.xml`.

## Installation

### Clone the Repository

```bash
git clone https://github.com/charithjayasanka/azure-ad-graph-integration.git
cd azure-ad-graph-integration
```

### Build the Project

Use the Maven wrapper (`mvnw`) or Maven installed on your system to build the project:

```bash
mvn clean install
```

This command will compile the project and download all necessary dependencies defined in the `pom.xml`.

## Configuration

### Create a `config.properties` File

Create a `config.properties` file in the `src/main/resources` folder with the following keys:

```properties
clientId=YOUR_CLIENT_ID
tenantId=YOUR_TENANT_ID
clientSecret=YOUR_CLIENT_SECRET
nickname=YOUR_USER_NICKNAME
```

Replace `YOUR_CLIENT_ID`, `YOUR_TENANT_ID`, `YOUR_CLIENT_SECRET`, and `YOUR_USER_NICKNAME` with your actual Azure AD and application values.

### Environment Variables (Optional)

Alternatively, you can set the following environment variables to override the values in the `config.properties` file:

```bash
export AZURE_CLIENT_ID=YOUR_CLIENT_ID
export AZURE_TENANT_ID=YOUR_TENANT_ID
export AZURE_CLIENT_SECRET=YOUR_CLIENT_SECRET
```

### Logging Configuration (Optional)

Modify the logging configuration if necessary by adding a `log4j.properties` or `logback.xml` file in the `src/main/resources` directory.

## Running the Application

After building the project, you can run the application using the following command:

```bash
java -cp target/GraphSample-1.0-SNAPSHOT.jar com.azuread.charith.AzureApp
```

### Expected Output

If the configuration is correct, the application should log the following information:

```
Azure AD Implementation started
Loading OAuth properties from config.properties...
OAuth properties successfully loaded. Initializing Microsoft Graph...
Access Token acquired successfully: <token>
User found: { "id": "12345", "displayName": "John Doe", "mail": "john.doe@domain.com" }
```

This output confirms successful authentication and user querying from Microsoft Graph.

## Building the Project

To build a standalone JAR file with all dependencies included (using `maven-shade-plugin`), run:

```bash
mvn package
```

This command will create a shaded JAR file in the `target` directory named `GraphSample-1.0-SNAPSHOT.jar`, which you can run as an executable JAR.


## Testing with Curl

You can test the authentication and Microsoft Graph API interactions using the following `curl` commands:

### Step 1: Acquire Access Token

```bash
curl -X POST https://login.microsoftonline.com/<tenant-id>/oauth2/v2.0/token \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "client_id=<client-id>" \
-d "scope=https%3A%2F%2Fgraph.microsoft.com%2F.default" \
-d "client_secret=<client-secret>" \
-d "grant_type=client_credentials"
```

### Step 2: Query User by `mailNickname`

```bash
curl -X GET "https://graph.microsoft.com/v1.0/users?\$filter=mailNickname eq '<nickname>'" \
-H "Authorization: Bearer <access_token>"
```

Replace `<tenant-id>`, `<client-id>`, `<client-secret>`, and `<nickname>` with your actual Azure AD values. Use the access token obtained from Step 1 in the `Authorization` header in Step 2.

### Example JSON Responses

- **Access Token Response:**

```json
{
  "token_type": "Bearer",
  "expires_in": 3599,
  "ext_expires_in": 3599,
  "access_token": "eyJ0eXAiOiJKV1QiLCJ..."
}
```

- **User Query Response:**

```json
{
  "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users",
  "value": [
    {
      "businessPhones": [],
      "displayName": "John Doe",
      "givenName": "John",
      "jobTitle": "Software Engineer",
      "mail": "johndoe@example.com",
      "mobilePhone": null,
      "officeLocation": null,
      "preferredLanguage": "en-US",
      "surname": "Doe",
      "userPrincipalName": "johndoe@example.com",
      "id": "12345678-abcd-1234-abcd-1234567890ab"
    }
  ]
}
```

## Logging and Debugging

All logs are printed using `System.out.println` for easy tracing and debugging. Logs include:

- Application start and initialization stages.
- Configuration file loading status.
- Authentication and access token acquisition.
- Microsoft Graph query details.
- Error and exception stack traces.

For a more sophisticated logging solution, consider integrating a logging framework like **SLF4J** with **Logback**.

## Error Handling

Common errors and their handling:

1. **Configuration Errors**: Ensure `config.properties` file exists and is properly formatted. Missing or incorrect values will cause an error during the application initialization.
2. **Authentication Errors**: Incorrect client credentials or tenant ID will result in authentication failures. Check your Azure AD application registration.
3. **HTTP Errors**: Failed HTTP requests to Microsoft Graph API may result from incorrect access tokens or insufficient permissions.

For detailed error descriptions, refer to the stack traces printed by the application.

## Contributing

Contributions are welcome! If you have suggestions or want to add new features, feel free to submit a pull request. Please ensure that your code adheres to the project's coding style and includes appropriate documentation.

### Steps to Contribute

1. Fork the repository.
2. Create a new branch for your feature or bug fix: `git checkout -b feature-name`.
3. Make your changes and commit them with descriptive messages.
4. Push to your forked repository: `git push origin feature-name`.
5. Create a pull request on the main repository.

## Contact

For any questions or issues, feel free to reach out to the project maintainer:

- **Name**: Charith
- **Email**: [Email Me](mailto:charithjayasanka2@gmail.com)
- **GitHub**: [chariths github-repo](https://github.com/charithjayasanka)

