package clients;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import utils.ConfigReader;
import utils.ScenarioLogger;

import static io.restassured.RestAssured.given;

public abstract class BaseClient {
    protected RequestSpecification requestSpec() {
        int timeout = ConfigReader.getInt("request.timeout.ms");
        RestAssured.baseURI = ConfigReader.get("base.url");
        RestAssured.config = RestAssuredConfig.config().httpClient(
                HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeout)
                        .setParam("http.socket.timeout", timeout)
        );

        return given()
                .relaxedHTTPSValidation()
                .accept("application/json")
                .contentType("application/json")
                .filter(cucumberHttpAttachmentFilter())
                .log().ifValidationFails();
    }

    private Filter cucumberHttpAttachmentFilter() {
        return (FilterableRequestSpecification requestSpec,
                FilterableResponseSpecification responseSpec,
                FilterContext context) -> {
            String requestDetails = formatRequest(requestSpec);
            System.out.println("HTTP request:\n" + requestDetails);
            ScenarioLogger.attach("HTTP request", formatRequest(requestSpec));
            Response response = context.next(requestSpec, responseSpec);
            String responseDetails = formatResponse(response);
            System.out.println("HTTP response:\n" + responseDetails);
            ScenarioLogger.attach("HTTP response", formatResponse(response));
            return response;
        };
    }

    private String formatRequest(FilterableRequestSpecification requestSpec) {
        return """
                %s %s
                Headers: %s
                Cookies: %s
                Query parameters: %s

                Body:
                %s
                """.formatted(
                requestSpec.getMethod(),
                requestSpec.getURI(),
                requestSpec.getHeaders(),
                requestSpec.getCookies(),
                requestSpec.getQueryParams(),
                requestSpec.getBody() == null ? "<empty>" : requestSpec.getBody()
        );
    }

    private String formatResponse(Response response) {
        return """
                Status: %s
                Headers: %s

                Body:
                %s
                """.formatted(
                response.getStatusLine(),
                response.getHeaders(),
                responseBody(response)
        );
    }

    private String responseBody(Response response) {
        try {
            return response.getBody().asPrettyString();
        } catch (RuntimeException exception) {
            return response.getBody().asString();
        }
    }
}
