package org.example.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.util.Optional;

/**
 * Azure Functions with HTTP trigger.
 * Handles both GET and POST requests.
 * Test locally: GET http://localhost:7071/api/HttpTriggerJava?name=World
 */
public class HttpTriggerJava {

    @FunctionName("HttpTriggerJava")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "hello"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger function executed.");

        // Parse query parameter or request body
        String name = request.getQueryParameters().get("name");
        if (name == null) {
            name = request.getBody().orElse("World");
        }

        if (name.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please pass a 'name' parameter in the query string or request body.")
                    .build();
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .body("Hello, " + name + "!")
                .build();
    }
}

