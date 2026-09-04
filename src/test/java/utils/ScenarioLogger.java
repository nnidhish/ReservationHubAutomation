package utils;

import io.cucumber.java.Scenario;

public final class ScenarioLogger {
    private static final ThreadLocal<Scenario> CURRENT_SCENARIO = new ThreadLocal<>();

    private ScenarioLogger() {
    }

    public static void setScenario(Scenario scenario) {
        CURRENT_SCENARIO.set(scenario);
    }

    public static void attach(String name, String content) {
        Scenario scenario = CURRENT_SCENARIO.get();
        if (scenario != null) {
            scenario.attach(content, "text/plain", name);
        }
    }

    public static void clear() {
        CURRENT_SCENARIO.remove();
    }
}
