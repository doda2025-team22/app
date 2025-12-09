package frontend.data.metrics;

import java.util.HashMap;
import java.util.Map;

public class Counter {
    private final String name;
    private final String help;
    private final Map<String, Double> values = new HashMap<>(); // label -> counter

    public Counter(String name, String help) {
        this.name = name;
        this.help = help;
    }

    /**
     * Increment the counter, insert a key with this label if it doesn't exist with value 1
     * @param label the label of the metric
     */
    public synchronized void inc(String label) {
        values.merge(label, 1.0, Double::sum);
    }

    public synchronized Map<String, Double> snapshot() {
        return new HashMap<>(values);
    }

    public String getName() { return name; }
    public String getHelp() { return help; }
}
