package frontend.data.metrics;

import java.util.HashMap;
import java.util.Map;

public class Gauge {
    private final String name;
    private final String help;
    private final Map<String, Double> values = new HashMap<>(); // label --> gauge

    public Gauge(String name, String help) {
        this.name = name;
        this.help = help;
    }

    public synchronized void set(String label, double value) {
        values.put(label, value);
    }

    public synchronized Map<String, Double> snapshot() {
        // locking to prevent inconsistent metrics, consider modifying for bigger applications
        return new HashMap<>(values); // give copy to not modify it
    }

    public String getName() { return name; }
    public String getHelp() { return help; }
}

