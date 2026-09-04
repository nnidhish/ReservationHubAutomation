package clients;

import io.restassured.response.Response;

import java.util.Map;

public class AuthClient extends BaseClient {
    //token retrieval
    public Response createToken(String username, String password) {
        return requestSpec()
                .body(Map.of("username", username, "password", password))
                .when()
                .post("/auth");
    }
}
