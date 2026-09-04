package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.ScenarioLogger;

public class Hooks {
    @Before
    public void setScenario(Scenario scenario) {
        ScenarioLogger.setScenario(scenario);
    }

    @After
    public void clearScenario() {
        ScenarioLogger.clear();
    }
}
