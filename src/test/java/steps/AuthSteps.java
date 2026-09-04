package steps;

import clients.AuthClient;
import context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import utils.ConfigReader;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {
    private final TestContext context;
    private final AuthClient authClient;

    public AuthSteps(TestContext context) {
        this.context = context;
        this.authClient = new AuthClient();
    }

    @When("I request an auth token with valid credentials")
    public void requestAuthTokenWithValidCredentials() {
        context.setResponse(authClient.createToken(
                ConfigReader.get("auth.username"),
                ConfigReader.get("auth.password")
        ));
        context.setToken(context.getResponse().jsonPath().getString("token"));
    }

    @When("I request an auth token with username {string} and password {string}")
    public void requestAuthTokenWithCredentials(String username, String password) {
        context.setResponse(authClient.createToken(username, password));
        context.setToken(context.getResponse().jsonPath().getString("token"));
    }

    @Then("the response should contain an auth token")
    public void responseShouldContainAuthToken() {
        assertThat(context.getToken()).isNotBlank();
    }

    @Then("the auth token should not be created")
    public void authTokenShouldNotBeCreated() {
        assertThat(context.getResponse().jsonPath().getString("token")).isNull();
        assertThat(context.getResponse().jsonPath().getString("reason")).isEqualTo("Bad credentials");
    }
}
